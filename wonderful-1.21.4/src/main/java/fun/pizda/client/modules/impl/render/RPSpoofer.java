package fun.pizda.client.modules.impl.render;

import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import fun.pizda.api.events.EventLink;
import fun.pizda.api.events.implement.EventPacket;
import fun.pizda.api.utils.bot.BotSessionManager;
import fun.pizda.client.modules.Module;

import java.util.UUID;

public class RPSpoofer extends Module {

    public static RPSpoofer INSTANCE = new RPSpoofer();
    public RPSpoofer() {
        super("RPSpoofer", "Убирает ресурс-пак сервера", ModuleCategory.PLAYER);
    }

    @EventLink
    public void onReceivePacket(EventPacket e) {
        if (e.getPacket() instanceof ResourcePackSendS2CPacket packet && (isEnable() || BotSessionManager.shouldBypassResourcePacks())) {
            UUID packId = packet.id();
            mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(packId, ResourcePackStatusC2SPacket.Status.ACCEPTED));
            mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(packId, ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            e.setCancelled(true);
        }
    }
}

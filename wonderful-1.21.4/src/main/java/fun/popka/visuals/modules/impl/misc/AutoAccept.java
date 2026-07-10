package fun.popka.visuals.modules.impl.misc;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import fun.popka.Popka;
import fun.popka.api.events.EventLink;
import fun.popka.api.events.implement.EventPacket;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.implement.BooleanSetting;

import java.util.Locale;

public class AutoAccept extends Module {

    public static AutoAccept INSTANCE = new AutoAccept();

    private final BooleanSetting onlyFriend = new BooleanSetting("Только друзья", false);

    public AutoAccept() {
        super("AutoAccept", "Автоматически принимает телепорт", ModuleCategory.MISC);
        addSettings(onlyFriend);
    }

    @EventLink
    public void onEvent(final EventPacket event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getType() != EventPacket.Type.RECEIVE) return;

        Packet<?> packet = event.getPacket();
        if (packet instanceof GameMessageS2CPacket messagePacket) {
            String raw = messagePacket.content().getString().toLowerCase(Locale.ROOT);

            if (raw.contains("телепортироваться") || raw.contains("has requested teleport") || raw.contains("просит к вам телепортироваться")) {
                if (onlyFriend.isState()) {
                    boolean isFriend = false;

                    if (Popka.INSTANCE.friendStorage != null) {
                        for (String friend : Popka.INSTANCE.friendStorage.getFriends()) {
                            if (raw.contains(friend.toLowerCase(Locale.ROOT))) {
                                isFriend = true;
                                break;
                            }
                        }
                    }

                    if (!isFriend) {
                        return;
                    }
                }

                mc.player.networkHandler.sendChatCommand("tpaccept");
            }
        }
    }
}

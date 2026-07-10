package fun.popka.visuals.modules.impl.combat;

import net.minecraft.block.Blocks;
import fun.popka.api.events.EventLink;
import fun.popka.api.events.implement.EventBlockCollide;
import fun.popka.visuals.modules.Module;

public class NoControllerWeb extends Module {

    public static NoControllerWeb INSTANCE = new NoControllerWeb();

    public NoControllerWeb() {
        super("NoControllerWeb", "Позволяет ломать и бить сквозь паутину", ModuleCategory.COMBAT);
    }

    @EventLink
    public void onBlockCollide(final EventBlockCollide e) {
        if (mc.world == null || e.getPos() == null) return;
        if (mc.world.getBlockState(e.getPos()).getBlock() == Blocks.COBWEB) {
            e.setCancelled(true);
        }
    }
}

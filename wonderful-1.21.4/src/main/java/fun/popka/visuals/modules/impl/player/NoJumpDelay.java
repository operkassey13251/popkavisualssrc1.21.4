package fun.popka.visuals.modules.impl.player;

import fun.popka.api.events.EventLink;
import fun.popka.api.events.implement.EventUpdate;
import fun.popka.visuals.modules.Module;
import fun.popka.mixin.ILivingEntity;

public class NoJumpDelay extends Module {

    public static NoJumpDelay INSTANCE = new NoJumpDelay();

    public NoJumpDelay() {
        super("NoJumpDelay", "Убирает задержку на прыжок", ModuleCategory.PLAYER);
    }

    @EventLink
    public void onEvent(final EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        ((ILivingEntity) mc.player).setJumpingCooldown(0);
    }
}

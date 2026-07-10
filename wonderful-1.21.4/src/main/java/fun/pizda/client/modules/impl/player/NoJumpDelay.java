package fun.pizda.client.modules.impl.player;

import fun.pizda.api.events.EventLink;
import fun.pizda.api.events.implement.EventUpdate;
import fun.pizda.client.modules.Module;
import fun.pizda.mixin.ILivingEntity;

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

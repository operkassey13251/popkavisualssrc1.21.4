package fun.popka.mixin;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import fun.popka.visuals.modules.impl.render.SeeInvisiblesRenderState;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements SeeInvisiblesRenderState {

    @Unique
    private boolean Popka$seeInvisiblesTarget;

    @Override
    public boolean Popka$isSeeInvisiblesTarget() {
        return Popka$seeInvisiblesTarget;
    }

    @Override
    public void Popka$setSeeInvisiblesTarget(boolean value) {
        Popka$seeInvisiblesTarget = value;
    }
}

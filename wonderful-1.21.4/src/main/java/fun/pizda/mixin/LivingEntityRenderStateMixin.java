package fun.pizda.mixin;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import fun.pizda.client.modules.impl.render.SeeInvisiblesRenderState;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements SeeInvisiblesRenderState {

    @Unique
    private boolean pizda$seeInvisiblesTarget;

    @Override
    public boolean pizda$isSeeInvisiblesTarget() {
        return pizda$seeInvisiblesTarget;
    }

    @Override
    public void pizda$setSeeInvisiblesTarget(boolean value) {
        pizda$seeInvisiblesTarget = value;
    }
}

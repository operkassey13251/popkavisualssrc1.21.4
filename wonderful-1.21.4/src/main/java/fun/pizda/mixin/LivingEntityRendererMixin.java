package fun.pizda.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fun.pizda.api.QClient;
import fun.pizda.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.pizda.client.modules.impl.render.SeeInvisibles;
import fun.pizda.client.modules.impl.render.SeeInvisiblesRenderState;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> implements QClient {

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void pizda$updateSeeInvisiblesState(T entity, S state, float tickDelta, CallbackInfo ci) {
        boolean shouldRenderInvisible = pizda$shouldRenderInvisible(entity);
        ((SeeInvisiblesRenderState) state).pizda$setSeeInvisiblesTarget(shouldRenderInvisible);
        if (shouldRenderInvisible) {
            state.invisible = true;
            state.invisibleToPlayer = false;
        }
    }

    @ModifyConstant(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            constant = @Constant(intValue = 654311423)
    )
    private int pizda$changeInvisibleAlpha(int original, S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        return ((SeeInvisiblesRenderState) state).pizda$isSeeInvisiblesTarget()
                ? SeeInvisibles.INVISIBLE_COLOR
                : original;
    }

    @Unique
    private boolean pizda$shouldRenderInvisible(T entity) {
        if (!(entity instanceof PlayerEntity player) || ModuleClass.INSTANCE == null) {
            return false;
        }

        SeeInvisibles seeInvisibles = ModuleClass.seeInvisibles;
        return seeInvisibles != null && seeInvisibles.shouldRenderInvisible(player);
    }

    @Unique
    private PlayerEntity pizda$resolvePlayer(S state) {
        if (!(state instanceof PlayerEntityRenderState playerState) || mc.world == null) {
            return null;
        }

        Entity entity = mc.world.getEntityById(playerState.id);
        return entity instanceof PlayerEntity player ? player : null;
    }
}

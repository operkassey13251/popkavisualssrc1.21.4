package fun.popka.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.visuals.modules.impl.render.Removals;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void Popka$hideTotemAnimation(ItemStack stack, CallbackInfo ci) {
        if (ModuleClass.INSTANCE == null || stack == null || !stack.isOf(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        Removals removals = ModuleClass.removals;
        if (removals != null && removals.isTotemAnimationDisabled()) {
            ci.cancel();
        }
    }
}

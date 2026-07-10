package fun.pizda.mixin;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.pizda.Pizda;
import fun.pizda.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.pizda.client.modules.impl.render.WorldTweaks;

@Mixin(ClientWorld.Properties.class)
public class ClientWorldPropertiesMixin {

    @Shadow private long timeOfDay;

    @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void pizda$setTimeOfDay(long timeOfDay, CallbackInfo ci) {
        if (ModuleClass.INSTANCE == null) return;

        WorldTweaks tweaks = ModuleClass.INSTANCE.worldTweaks;
        if (tweaks == null || !tweaks.isTimeEnabled()) return;

        this.timeOfDay = tweaks.getForcedTime();
        ci.cancel();
    }
}

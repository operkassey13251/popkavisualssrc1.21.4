package fun.pizda.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fun.pizda.Pizda;
import fun.pizda.api.storages.implement.DragStorage;
import fun.pizda.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.pizda.api.utils.draggable.Draggable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Unique
    private boolean pizda$leftPressed;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleClass.interfaceModule.handleHudContextClick(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }

        for (Draggable draggable : DragStorage.draggables.values()) {
            if (draggable.getModule().isEnable() && draggable.onClick(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();

        boolean leftPressed = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (pizda$leftPressed && !leftPressed) {
            for (Draggable draggable : DragStorage.draggables.values()) {
                draggable.onRelease(0);
            }
        }
        pizda$leftPressed = leftPressed;

        for (Draggable draggable : DragStorage.draggables.values()) {
            if (draggable.getModule().isEnable()) {
                draggable.onDraw(mouseX, mouseY, window, context.getMatrices());
            }
        }

        ModuleClass.interfaceModule.renderHudContextMenu(context, mouseX, mouseY);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        pizda$leftPressed = false;
        for (Draggable draggable : DragStorage.draggables.values()) {
            draggable.onRelease(0);
        }
        try {
            Pizda.INSTANCE.configStorage.saveConfig(Pizda.INSTANCE.configStorage.currentConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

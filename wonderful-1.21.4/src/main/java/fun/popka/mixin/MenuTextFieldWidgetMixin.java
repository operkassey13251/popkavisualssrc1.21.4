package fun.popka.mixin;

import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public abstract class MenuTextFieldWidgetMixin {

    @Shadow
    private boolean drawsBackground;

    private static final int BG_COLOR = ColorUtils.rgba(0, 0, 0, 200);
    private static final int BG_FOCUSED_COLOR = ColorUtils.rgba(20, 20, 20, 220);
    private static final int BORDER_COLOR = ColorUtils.rgba(60, 60, 60, 255);
    private static final int BORDER_FOCUSED_COLOR = ColorUtils.rgba(120, 120, 120, 255);
    private static final float CORNER_RADIUS = 4f;

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void popka$renderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world != null) {
            return;
        }

        TextFieldWidget self = (TextFieldWidget) (Object) this;
        if (!self.visible) {
            return;
        }

        if (!this.drawsBackground) {
            this.drawsBackground = true;
        }

        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();

        boolean focused = self.isFocused();
        int bgColor = focused ? BG_FOCUSED_COLOR : BG_COLOR;
        int borderColor = focused ? BORDER_FOCUSED_COLOR : BORDER_COLOR;

        RenderUtils.drawRoundedRect(context.getMatrices(), x, y, w, h, CORNER_RADIUS, borderColor);
        RenderUtils.drawRoundedRect(context.getMatrices(), x + 1, y + 1, w - 2, h - 2, CORNER_RADIUS - 1, bgColor);
    }

    @Redirect(
            method = "renderWidget",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;drawsBackground()Z", ordinal = 0),
            require = 0
    )
    private boolean popka$redirectDrawsBackground(TextFieldWidget instance) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.world == null) {
            return false;
        }
        return this.drawsBackground;
    }
}

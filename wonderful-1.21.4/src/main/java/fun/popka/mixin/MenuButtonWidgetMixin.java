package fun.popka.mixin;

import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PressableWidget.class)
public abstract class MenuButtonWidgetMixin {

    private static final int BG_COLOR = ColorUtils.rgba(0, 0, 0, 200);
    private static final int BG_HOVER_COLOR = ColorUtils.rgba(40, 40, 40, 220);
    private static final int BG_DISABLED_COLOR = ColorUtils.rgba(10, 10, 10, 150);
    private static final int TEXT_COLOR = ColorUtils.rgb(255, 255, 255);
    private static final int TEXT_DISABLED_COLOR = ColorUtils.rgb(130, 130, 130);
    private static final float CORNER_RADIUS = 6f;
    private static final int FONT_SIZE = 14;
    private static final float VERTICAL_CENTER_OFFSET = 5f;

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void popka$renderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world != null) {
            return;
        }

        ClickableWidget self = (ClickableWidget) (Object) this;
        if (!self.visible) {
            return;
        }

        int bgColor;
        int textColor;
        if (!self.active) {
            bgColor = BG_DISABLED_COLOR;
            textColor = TEXT_DISABLED_COLOR;
        } else if (self.isHovered()) {
            bgColor = BG_HOVER_COLOR;
            textColor = TEXT_COLOR;
        } else {
            bgColor = BG_COLOR;
            textColor = TEXT_COLOR;
        }

        RenderUtils.drawRoundedRect(context.getMatrices(), self.getX(), self.getY(), self.getWidth(), self.getHeight(), CORNER_RADIUS, bgColor);

        Font font = Fonts.getFont("suisse", FONT_SIZE);
        if (font != null) {
            String text = self.getMessage().getString();
            float textX = self.getX() + self.getWidth() / 2f;
            float textY = self.getY() + (self.getHeight() - FONT_SIZE) / 2f + VERTICAL_CENTER_OFFSET;
            font.drawCenteredString(context.getMatrices(), text, textX, textY, textColor);
        }

        ci.cancel();
    }
}

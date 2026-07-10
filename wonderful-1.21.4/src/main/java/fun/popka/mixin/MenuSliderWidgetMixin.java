package fun.popka.mixin;

import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SliderWidget.class)
public abstract class MenuSliderWidgetMixin {

    private static final int BG_COLOR = ColorUtils.rgba(0, 0, 0, 200);
    private static final int BG_HOVER_COLOR = ColorUtils.rgba(40, 40, 40, 220);
    private static final int BG_DISABLED_COLOR = ColorUtils.rgba(10, 10, 10, 150);
    private static final int FILL_COLOR = ColorUtils.rgba(60, 60, 60, 220);
    private static final int HANDLE_COLOR = ColorUtils.rgb(220, 220, 220);
    private static final int HANDLE_HOVER_COLOR = ColorUtils.rgb(255, 255, 255);
    private static final int TEXT_COLOR = ColorUtils.rgb(255, 255, 255);
    private static final int TEXT_DISABLED_COLOR = ColorUtils.rgb(130, 130, 130);
    private static final float CORNER_RADIUS = 6f;
    private static final int FONT_SIZE = 13;
    private static final float VERTICAL_CENTER_OFFSET = 4.5f;
    private static final int HANDLE_WIDTH = 8;

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

        double value = ((SliderWidgetAccessor) this).getValue();
        int x = self.getX();
        int y = self.getY();
        int w = self.getWidth();
        int h = self.getHeight();

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

        RenderUtils.drawRoundedRect(context.getMatrices(), x, y, w, h, CORNER_RADIUS, bgColor);

        if (self.active) {
            float fillWidth = (float) (w * value);
            if (fillWidth > 1f) {
                RenderUtils.drawRoundedRect(context.getMatrices(), x, y, fillWidth, h, CORNER_RADIUS, FILL_COLOR);
            }
            int handleColor = self.isHovered() ? HANDLE_HOVER_COLOR : HANDLE_COLOR;
            float handleX = x + (float) (w * value) - HANDLE_WIDTH / 2f;
            if (handleX < x) handleX = x;
            if (handleX + HANDLE_WIDTH > x + w) handleX = x + w - HANDLE_WIDTH;
            float handleHeight = h - 4;
            RenderUtils.drawRoundedRect(context.getMatrices(), handleX, y + 2, HANDLE_WIDTH, handleHeight, 3f, handleColor);
        }

        Font font = Fonts.getFont("suisse", FONT_SIZE);
        if (font != null) {
            String text = self.getMessage().getString();
            float textX = x + w / 2f;
            float textY = y + (h - FONT_SIZE) / 2f + VERTICAL_CENTER_OFFSET;
            font.drawCenteredString(context.getMatrices(), text, textX, textY, textColor);
        }

        ci.cancel();
    }
}

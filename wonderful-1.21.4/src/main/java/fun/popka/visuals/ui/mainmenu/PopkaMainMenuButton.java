package fun.popka.visuals.ui.mainmenu;

import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class PopkaMainMenuButton extends ButtonWidget {

    private static final int BG_COLOR = ColorUtils.rgb(0, 0, 0);
    private static final int BG_HOVER_COLOR = ColorUtils.rgb(40, 40, 40);
    private static final int TEXT_COLOR = ColorUtils.rgb(255, 255, 255);
    private static final int TEXT_HOVER_COLOR = ColorUtils.rgb(255, 255, 255);
    private static final float CORNER_RADIUS = 6f;
    private static final int FONT_SIZE = 14;
    private static final float VERTICAL_CENTER_OFFSET = 6.5f;

    public PopkaMainMenuButton(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int bgColor = this.isHovered() ? BG_HOVER_COLOR : BG_COLOR;
        RenderUtils.drawRoundedRect(context.getMatrices(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), CORNER_RADIUS, bgColor);

        Font font = Fonts.getFont("suisse", FONT_SIZE);
        if (font == null) return;
        String text = this.getMessage().getString();
        int textColor = this.isHovered() ? TEXT_HOVER_COLOR : TEXT_COLOR;
        float textX = this.getX() + this.getWidth() / 2f;
        float textY = this.getY() + (this.getHeight() - FONT_SIZE) / 2f + VERTICAL_CENTER_OFFSET;
        font.drawCenteredString(context.getMatrices(), text, textX, textY, textColor);
    }

}

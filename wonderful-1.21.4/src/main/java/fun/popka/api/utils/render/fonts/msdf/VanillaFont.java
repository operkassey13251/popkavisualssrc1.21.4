package fun.popka.api.utils.render.fonts.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class VanillaFont extends Font {

    private static final float SCALE_DIVISOR = 16.0f;
    private static final float VANILLA_BASELINE_RATIO = 0.875f;
    private static final BufferAllocator SHARED_ALLOCATOR = new BufferAllocator(262144);

    public VanillaFont(float size) {
        super((MsdfFont) null, size);
    }

    private float getScale() {
        return getSize() / SCALE_DIVISOR;
    }

    private int fixColor(int color) {
        int a = (color >> 24) & 0xFF;
        if (a == 0) return color | 0xFF000000;
        return color;
    }

    @Override
    public void draw(MatrixStack stack, String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;
        if (mc == null || mc.textRenderer == null) return;

        int fixedColor = fixColor(color);
        float scale = getScale();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        stack.push();
        stack.translate(x, y - 1.5f, 0);
        stack.scale(scale, scale, 1.0f);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(SHARED_ALLOCATOR);
        mc.textRenderer.draw(text, 0, 0, fixedColor, false, matrix, immediate, TextRenderer.TextLayerType.NORMAL, 0, 255);
        immediate.draw();

        stack.pop();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    @Override
    public void drawStringWithFade(MatrixStack stack, String text, float x, float y, float maxWidth, int color) {
        if (text == null || text.isEmpty()) return;
        if (maxWidth <= 1f) return;

        int originalAlpha = (color >>> 24) & 0xFF;
        if (originalAlpha == 0) originalAlpha = 255;
        if (originalAlpha <= 4) return;

        float currentX = x;
        float fadeZoneWidth = 25f;
        float fadeStartX = x + maxWidth - fadeZoneWidth;

        for (int i = 0; i < text.length(); i++) {
            String charStr = String.valueOf(text.charAt(i));
            float charWidth = getStringWidth(charStr);

            if (currentX > x + maxWidth && i > 0) break;

            int finalColor = color;
            if (currentX > fadeStartX) {
                float progressIntoFade = (currentX - fadeStartX) / fadeZoneWidth;
                progressIntoFade = Math.max(0.0f, Math.min(1.0f, progressIntoFade));
                float fadeFactor = (float) Math.cos(progressIntoFade * Math.PI / 2.0);
                int newAlpha = (int) (originalAlpha * fadeFactor);
                finalColor = (color & 0x00FFFFFF) | (newAlpha << 24);
            }

            if (((finalColor >>> 24) & 0xFF) > 4) {
                draw(stack, charStr, currentX, y, finalColor);
            }

            currentX += charWidth;
        }
    }

    @Override
    public void drawParagraph(MatrixStack stack, String text, float x, float y, int defaultColor) {
        if (text == null || text.isEmpty()) return;
        draw(stack, text, x, y, defaultColor);
    }

    @Override
    public float getStringWidth(String text) {
        if (text == null) return 0;
        if (mc == null || mc.textRenderer == null) return 0;
        return mc.textRenderer.getWidth(text) * getScale();
    }

    @Override
    public float getWidth(String text) {
        return getStringWidth(text);
    }

    @Override
    public float getHeight() {
        return getSize();
    }

    @Override
    public float getFontHeight() {
        return getSize();
    }

    @Override
    public MsdfFont getFont() {
        return null;
    }

    @Override
    public float getBaselineHeight() {
        return VANILLA_BASELINE_RATIO;
    }
}

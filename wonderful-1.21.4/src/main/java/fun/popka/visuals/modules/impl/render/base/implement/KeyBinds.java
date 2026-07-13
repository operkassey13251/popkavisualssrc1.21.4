package fun.popka.visuals.modules.impl.render.base.implement;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import fun.popka.Popka;
import fun.popka.api.events.implement.EventRender;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.api.utils.animation.AnimationUtils;
import fun.popka.api.utils.animation.Easings;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.draggable.Draggable;
import fun.popka.api.utils.input.KeyBoardUtils;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.api.utils.scissor.ScissorUtils;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.impl.render.base.InterfaceProcessing;

import java.util.HashMap;
import java.util.Map;

public class KeyBinds extends InterfaceProcessing {
    private static final Identifier BINDS_ICON_TEXTURE = Identifier.of("popka", "textures/hud/binds.png");

    private final Map<Module, AnimationUtils> animations = new HashMap<>();
    private final AnimationUtils widthAnimation = new AnimationUtils(60, 10.5f, Easings.QUAD_OUT);

    private static final Map<Character, Character> RU_TO_EN = new HashMap<>();
    static {
        String ru = "йцукенгшщзхъфывапролджэячсмитьбюЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ";
        String en = "qwertyuiop[]asdfghjkl;'zxcvbnm,.QWERTYUIOP[]ASDFGHJKL;'ZXCVBNM,.";
        int length = Math.min(ru.length(), en.length());
        for (int i = 0; i < length; i++) {
            RU_TO_EN.put(ru.charAt(i), en.charAt(i));
        }
    }

    public KeyBinds(Draggable draggable) {
        super(draggable);
    }

    private Font issue(int size) { return Fonts.getFont("suisse", size); }
    private Font icon(int size) { return Fonts.getFont("icon1", size); }

    private AnimationUtils getAnimation(Module module) {
        return animations.computeIfAbsent(module, m -> new AnimationUtils(0, 10.5f, Easings.QUAD_OUT));
    }

    private String toEnglish(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            result.append(RU_TO_EN.getOrDefault(c, c));
        }
        return result.toString();
    }

    private int getStaticThemeColor() {
        int[] colors = Popka.INSTANCE.themeStorage.getThemes().getTheme().getColor();
        if (colors == null || colors.length == 0) {
            return 0xFFFFFFFF;
        }

        int color = colors[0];
        if (((color >> 24) & 0xFF) == 0) {
            color = (color & 0x00FFFFFF) | 0xFF000000;
        }
        return color;
    }

    @Override
    public void onRender(EventRender.Default eventRender) {
        DefaultStyle(eventRender);
        super.onRender(eventRender);
    }

    public void DefaultStyle(EventRender.Default eventRender) {
        float baseX = draggable.getX(), y = draggable.getY();
        int colorTheme;
        if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            colorTheme = Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } else {
            colorTheme = ColorUtils.getThemeColor();
        }
        int staticAccentColor = getStaticThemeColor();

        float targetWidth = 64;
        float targetHeight = 16;
        int visibleCount = 0;

        for (Module module : ModuleClass.INSTANCE.getObject()) {
            if (module.getKey() != -1) {
                AnimationUtils anim = getAnimation(module);
                anim.update(module.isEnable() ? 1 : 0);
            }
        }

        for (Module module : ModuleClass.INSTANCE.getObject()) {
            if (module.getKey() != -1) {
                AnimationUtils anim = getAnimation(module);
                float animValue = anim.getValue();

                if (animValue > 0.01f) {
                    visibleCount++;
                    String keyName = toEnglish(KeyBoardUtils.getKeyName(module.getKey()));
                    float keyWidth = issue(10).getWidth(keyName);
                    float moduleWidth = issue(12).getWidth(module.getDisplayName()) + keyWidth + 25;
                    if (moduleWidth > targetWidth) targetWidth = moduleWidth;
                    targetHeight += 12 * animValue;
                }
            }
        }

        if (visibleCount > 0) targetHeight += 2;

        widthAnimation.update(targetWidth);
        float width = widthAnimation.getValue() + 7;
        float height = targetHeight;
        float rightEdge = baseX + 60;
        float x = rightEdge - width;

        MatrixStack matrices = eventRender.getContext().getMatrices();
        if (isUnusualRectType()) {
            RenderUtils.drawLiquidGlassPanel(matrices, x, y, width, height, 3.0f, 3.5f, colorTheme);
            RenderUtils.drawHudSquarePattern(matrices, x, y, width, height, colorTheme);
            RenderUtils.drawRoundedRect(matrices, x + width - 14.5f, y + 3.0f, 10.0f, 10.0f, 2.0f, ColorUtils.darken(colorTheme, 0.4f));
        } else {
            RenderUtils.drawDefaultHudElementRects(matrices, x, y, width, height, colorTheme, false);
            int iridescentTL = ColorUtils.getThemeColor(0);
            int iridescentTR = ColorUtils.getThemeColor(90);
            int iridescentBR = ColorUtils.getThemeColor(180);
            int iridescentBL = ColorUtils.getThemeColor(270);
            RenderUtils.drawRoundedRectOutline(matrices, x, y, width, height, 3.5f, 1.0f,
                    iridescentTL, iridescentTR, iridescentBL, iridescentBR);
        }
        issue(14).draw(matrices, "Binds", x + 5, y + 6f, -1);
        RenderUtils.drawImage(matrices, BINDS_ICON_TEXTURE, rightEdge - 13.5f, y + 4f, 8f, 8f, colorTheme);

        float offsetY = 18;
        for (Module module : ModuleClass.INSTANCE.getObject()) {
            if (module.getKey() != -1) {
                AnimationUtils anim = getAnimation(module);
                float animValue = anim.getValue();

                if (animValue > 0.01f) {
                    ScissorUtils.push();
                    ScissorUtils.setFromComponentCoordinates(x, y, width, height);
                    String keyName = toEnglish(KeyBoardUtils.getBindName(module.getKey()));
                    float keyBoxWidth = Math.max(issue(10).getWidth(keyName) + 4, 9f);

                    int alpha = (int) (255 * animValue);
                    int textColor = ColorUtils.rgba(255, 255, 255, alpha);
                    int accentColor = ColorUtils.setAlphaColor(getStableThemeColor(), alpha);
                    int grayColor = ColorUtils.rgba(55, 55, 55, alpha);
                    int darkColor = ColorUtils.rgba(35, 35, 35, alpha);

                    issue(12).draw(eventRender.getContext().getMatrices(), module.getDisplayName(), x + 12, y + 2 + offsetY, textColor);
                    RenderUtils.drawRoundedRect(eventRender.getContext().getMatrices(), x + 5.2f, y + offsetY + 0.3f, 2.55f, 5.7f, 0.15f, accentColor);

                    float keyBoxX = rightEdge - keyBoxWidth - 5;
                    RenderUtils.drawDefaultHudInfoBox(eventRender.getContext().getMatrices(), keyBoxX, y + offsetY, keyBoxWidth, grayColor, darkColor);
                    issue(10).drawCenteredString(eventRender.getContext().getMatrices(), keyName, keyBoxX + keyBoxWidth / 2, y + offsetY + 2.8f, textColor);

                    offsetY += 12 * animValue;
                    ScissorUtils.pop();
                    ScissorUtils.unset();
                }
            }
        }

        draggable.setWidth(60);
        draggable.setHeight(height);
    }
    private int getStableThemeColor() {
        if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            return Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        }
        return ColorUtils.getThemeColor();
    }
}

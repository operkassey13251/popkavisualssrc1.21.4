package fun.popka.visuals.ui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import fun.popka.Popka;
import fun.popka.api.QClient;
import fun.popka.api.storages.implement.ThemeStorage;
import fun.popka.api.utils.animation.AnimationUtils;
import fun.popka.api.utils.animation.Easings;
import fun.popka.api.utils.chat.ChatUtils;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.math.HoveringUtils;
import fun.popka.api.utils.notification.NotificationManager;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.api.utils.scissor.ScissorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiThemePanel implements QClient {
    private static final float SLIDE_DISTANCE = 220f;
    private static final float HEADER_HEIGHT = 24f;
    private static final float SEPARATOR_Y = 23f;
    private static final float CONTENT_TOP = 25f;
    private static final float CONTENT_BOTTOM_PADDING = 4f;
    private static final float THEME_ROW_HEIGHT = 14f;
    private static final float THEME_ROW_GAP = 2f;
    private static final float THEME_NAME_LEFT = 6f;
    private static final float THEME_DOT_X = 94f;
    private static final float THEME_DOT_RADIUS = 3.5f;
    private static final long DOUBLE_CLICK_MS = 350L;
    private static final String HEADER_NAME = "Themes";
    private static final String HEADER_ICON = "g";
    private static final int DOT_RED = ColorUtils.rgb(205, 65, 65);
    private static final int DOT_GREEN = ColorUtils.rgb(70, 200, 110);

    private final ClickGuiState state;
    private final AnimationUtils slideAnimation = new AnimationUtils(0f, 6.5f, Easings.CUBIC_OUT);
    private final AnimationUtils scrollAnimation = new AnimationUtils(0f, 8f, Easings.CUBIC_OUT);
    private final Map<String, AnimationUtils> themeHoverAnimations = new HashMap<>();

    private float scrollTarget = 0f;
    private List<ThemeStorage.Themes> cachedThemes = new ArrayList<>();
    private ThemeStorage.Themes lastClickTheme = null;
    private long lastClickTime = 0L;

    public ClickGuiThemePanel(ClickGuiState state) {
        this.state = state;
        refreshThemes();
    }

    public void update(boolean closing) {
        if (closing) {
            slideAnimation.setEasing(Easings.CUBIC_IN);
            slideAnimation.update(0f);
        } else {
            slideAnimation.setEasing(Easings.CUBIC_OUT);
            slideAnimation.update(1f);
        }
    }

    public float getSlideProgress() {
        return MathHelper.clamp(slideAnimation.getValue(), 0.0f, 1.0f);
    }

    private Window getWindow() {
        return mw == null ? (mc == null ? null : mc.getWindow()) : mw;
    }

    public float getRestingX() {
        Window window = getWindow();
        if (window == null) {
            return 6f;
        }
        return window.getScaledWidth() - 6f - ClickGuiLayout.WIDTH;
    }

    public float getPanelX() {
        return getRestingX() + (1.0f - getSlideProgress()) * SLIDE_DISTANCE;
    }

    public float getPanelY() {
        return state.getY() + state.getRenderOffsetY();
    }

    public void render(DrawContext context, int mouseX, int mouseY, Window window, float openProgress) {
        float slide = getSlideProgress();
        if (slide <= 0.001f) {
            return;
        }

        int colorTheme = getThemeColor();
        float alphaMul = MathHelper.clamp(openProgress, 0.0f, 1.0f);
        int shadeColor = getFadeShadeColor(alphaMul, 120);

        float panelX = getPanelX();
        float panelY = getPanelY();

        RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY, ClickGuiLayout.WIDTH, ClickGuiLayout.HEIGHT, ClickGuiLayout.PANEL_RADIUS, alpha(ClickGuiLayout.getPanelBg(colorTheme), alphaMul));
        RenderUtils.drawGradientRect(context.getMatrices(), panelX, panelY, ClickGuiLayout.WIDTH, HEADER_HEIGHT, ClickGuiLayout.PANEL_RADIUS, alpha(ClickGuiLayout.getPanelHeaderBg(colorTheme), alphaMul), alpha(ClickGuiLayout.getPanelBg(colorTheme), alphaMul));
        RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY + SEPARATOR_Y, ClickGuiLayout.WIDTH, 0.5F, 0, alpha(ClickGuiLayout.getSeparator(colorTheme), alphaMul));
        if (((shadeColor >> 24) & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY, ClickGuiLayout.WIDTH, ClickGuiLayout.HEIGHT, ClickGuiLayout.PANEL_RADIUS, shadeColor);
        }

        float headerIconX = panelX + ClickGuiLayout.HEADER_CENTER_ICON - (issue(15).getWidth(HEADER_NAME) / 2F) - 2;
        icons(14).drawCenteredString(context.getMatrices(), HEADER_ICON, headerIconX, panelY + 10, alpha(colorTheme, alphaMul));
        issue(15).drawCenteredString(context.getMatrices(), HEADER_NAME, panelX + ClickGuiLayout.HEADER_CENTER_TEXT, panelY + 9, alpha(ClickGuiLayout.TEXT_PRIMARY, alphaMul));

        refreshThemes();

        float listTop = panelY + CONTENT_TOP;
        float listBottom = panelY + ClickGuiLayout.HEIGHT - CONTENT_BOTTOM_PADDING;
        float listHeight = listBottom - listTop;

        clampScroll(listHeight);
        scrollAnimation.update(scrollTarget);
        float scroll = scrollAnimation.getValue();

        int count = cachedThemes.size();
        float[] hovers = new float[count];
        float[] rowYs = new float[count];

        float rowY = listTop + scroll;
        for (int i = 0; i < count; i++) {
            rowYs[i] = rowY;
            rowY += THEME_ROW_HEIGHT + THEME_ROW_GAP;
        }

        boolean canHover = slide > 0.98f;
        ThemeStorage.Themes currentTheme = Popka.INSTANCE.themeStorage.getThemes();
        for (int i = 0; i < count; i++) {
            String key = cachedThemes.get(i).name();
            boolean isHovered = canHover && HoveringUtils.isHovered(mouseX, mouseY, panelX + ClickGuiLayout.MODULE_PADDING, rowYs[i] - 0.5f, ClickGuiLayout.MODULE_INNER_WIDTH, THEME_ROW_HEIGHT + 1f);
            AnimationUtils anim = getThemeHoverAnimation(key, isHovered);
            anim.update(isHovered ? 1f : 0f);
            hovers[i] = anim.getValue();
        }

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(panelX, listTop, ClickGuiLayout.WIDTH, listHeight);

        for (int i = 0; i < count; i++) {
            if (rowYs[i] + THEME_ROW_HEIGHT >= listTop && rowYs[i] <= listBottom) {
                ThemeStorage.Themes theme = cachedThemes.get(i);
                boolean isCurrent = theme == currentTheme;
                renderThemeRow(context, panelX, rowYs[i], hovers[i], theme, isCurrent, colorTheme, alphaMul);
            }
        }

        ScissorUtils.pop();
    }

    private void renderThemeRow(DrawContext context, float panelX, float rowY, float hover, ThemeStorage.Themes theme, boolean isCurrent, int colorTheme, float alphaMul) {
        float intensity = isCurrent ? 1.0f : hover;

        if (intensity > 0.01f) {
            int bgTop = ColorUtils.applyAlpha(ClickGuiLayout.getModuleEnabledBgTop(colorTheme), alphaMul * intensity);
            int bgBottom = ColorUtils.applyAlpha(ClickGuiLayout.getModuleEnabledBgBottom(colorTheme), alphaMul * intensity);
            RenderUtils.drawRoundedRect(context.getMatrices(), panelX + ClickGuiLayout.MODULE_PADDING, rowY - 0.5f, ClickGuiLayout.MODULE_INNER_WIDTH, THEME_ROW_HEIGHT + 1, 4, bgBottom);
            RenderUtils.drawGradientRect(context.getMatrices(), panelX + ClickGuiLayout.MODULE_PADDING + 0.5f, rowY, ClickGuiLayout.MODULE_INNER_WIDTH - 1f, THEME_ROW_HEIGHT, 4, bgTop, bgBottom, false);
        }

        int nameColor = isCurrent ? alpha(colorTheme, alphaMul) : alpha(ClickGuiLayout.TEXT_PRIMARY, alphaMul);
        String displayName = truncateName(theme.name(), THEME_DOT_X - 2f - THEME_NAME_LEFT);
        issue(13).draw(context.getMatrices(), displayName, panelX + THEME_NAME_LEFT, rowY + 5f, nameColor);

        int dotColor = isCurrent ? DOT_GREEN : DOT_RED;
        RenderUtils.drawRoundCircle(context.getMatrices(), panelX + THEME_DOT_X, rowY + THEME_ROW_HEIGHT / 2f, THEME_DOT_RADIUS, alpha(dotColor, alphaMul));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, Window window) {
        if (getSlideProgress() <= 0.01f) {
            return false;
        }
        if (button != 0) {
            return false;
        }

        float panelX = getPanelX();
        float panelY = getPanelY();

        boolean onPanel = HoveringUtils.isHovered(mouseX, mouseY, panelX, panelY, ClickGuiLayout.WIDTH, ClickGuiLayout.HEIGHT);
        if (!onPanel) {
            return false;
        }

        float listTop = panelY + CONTENT_TOP;
        float listBottom = panelY + ClickGuiLayout.HEIGHT - CONTENT_BOTTOM_PADDING;
        float listHeight = listBottom - listTop;
        if (HoveringUtils.isHovered(mouseX, mouseY, panelX, listTop, ClickGuiLayout.WIDTH, listHeight)) {
            scrollAnimation.update(scrollTarget);
            float scroll = scrollAnimation.getValue();
            float rowY = listTop + scroll;
            for (ThemeStorage.Themes theme : cachedThemes) {
                if (HoveringUtils.isHovered(mouseX, mouseY, panelX + ClickGuiLayout.MODULE_PADDING, rowY - 0.5f, ClickGuiLayout.MODULE_INNER_WIDTH, THEME_ROW_HEIGHT + 1f)) {
                    handleRowClick(theme);
                    return true;
                }
                rowY += THEME_ROW_HEIGHT + THEME_ROW_GAP;
            }
        }

        return true;
    }

    private void handleRowClick(ThemeStorage.Themes theme) {
        long now = System.currentTimeMillis();
        if (lastClickTheme != null && lastClickTheme == theme && (now - lastClickTime) <= DOUBLE_CLICK_MS) {
            lastClickTheme = null;
            lastClickTime = 0L;
            applyTheme(theme);
        } else {
            lastClickTheme = theme;
            lastClickTime = now;
        }
    }

    private void applyTheme(ThemeStorage.Themes theme) {
        try {
            Popka.INSTANCE.themeStorage.setThemes(theme);
            NotificationManager.pushCustom("Theme applied: " + theme.name(), HEADER_ICON);
            ChatUtils.sendMessage("Тема " + theme.name() + " успешно применена!");
        } catch (Exception e) {
            NotificationManager.pushCustom("Error: " + e.getMessage(), HEADER_ICON);
            ChatUtils.sendMessage("Ошибка при применении темы " + theme.name() + "!");
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        if (getSlideProgress() <= 0.01f) {
            return false;
        }

        float panelX = getPanelX();
        float panelY = getPanelY();
        float listTop = panelY + CONTENT_TOP;
        float listBottom = panelY + ClickGuiLayout.HEIGHT - CONTENT_BOTTOM_PADDING;
        float listHeight = listBottom - listTop;

        if (HoveringUtils.isHovered(mouseX, mouseY, panelX, listTop, ClickGuiLayout.WIDTH, listHeight)) {
            scrollTarget += (float) (verticalAmount * 20);
            clampScroll(listHeight);
            return true;
        }
        return false;
    }

    private void refreshThemes() {
        cachedThemes = new ArrayList<>(Popka.INSTANCE.themeStorage.getThemeList());
    }

    private void clampScroll(float listHeight) {
        float totalHeight = cachedThemes.size() * (THEME_ROW_HEIGHT + THEME_ROW_GAP);
        float maxScroll = Math.min(0f, listHeight - totalHeight);
        scrollTarget = Math.max(maxScroll, Math.min(0f, scrollTarget));
    }

    private String truncateName(String name, float maxWidth) {
        if (name == null) {
            return "";
        }
        Font font = issue(13);
        if (font.getWidth(name) <= maxWidth) {
            return name;
        }
        String ellipsis = "..";
        float ellipsisWidth = font.getWidth(ellipsis);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            String candidate = builder.toString() + name.charAt(i);
            if (font.getWidth(candidate) + ellipsisWidth > maxWidth) {
                break;
            }
            builder.append(name.charAt(i));
        }
        return builder.toString() + ellipsis;
    }

    private AnimationUtils getThemeHoverAnimation(String key, boolean hovered) {
        return themeHoverAnimations.computeIfAbsent(key, k -> new AnimationUtils(hovered ? 1f : 0f, 9f, Easings.CUBIC_OUT));
    }

    private int getThemeColor() {
        if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            return Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        }
        return ColorUtils.getThemeColor();
    }

    private int alpha(int color, float alphaMul) {
        return ColorUtils.applyAlpha(color, alphaMul);
    }

    private int getFadeShadeColor(float alphaMul, int maxAlpha) {
        int alpha = MathHelper.clamp((int) ((1.0f - alphaMul) * maxAlpha), 0, 255);
        return ColorUtils.rgba(0, 0, 0, alpha);
    }

    private Font issue(int size) {
        return Fonts.getFont("suisse", size);
    }

    private Font icons(int size) {
        return Fonts.getFont("icon", size);
    }
}

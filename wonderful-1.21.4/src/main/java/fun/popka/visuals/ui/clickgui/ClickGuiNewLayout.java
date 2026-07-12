package fun.popka.visuals.ui.clickgui;

import fun.popka.api.utils.color.ColorUtils;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.Setting;
import fun.popka.visuals.modules.settings.implement.BindSetting;
import fun.popka.visuals.modules.settings.implement.BooleanSetting;
import fun.popka.visuals.modules.settings.implement.FloatSetting;
import fun.popka.visuals.modules.settings.implement.ListSetting;
import fun.popka.visuals.modules.settings.implement.ModeSetting;
import fun.popka.visuals.modules.settings.implement.TextSetting;

import java.util.List;

public final class ClickGuiNewLayout {
    public static final float WIDTH = 104f;
    public static final float HEIGHT = 320f;
    public static final float CATEGORY_PANEL_STEP = 114f;
    public static final float PANEL_RADIUS = 6f;

    public static final float THEME_PANEL_Y = 100f;

    public static final float MODULE_PADDING = 3f;
    public static final float MODULE_GAP = 4f;
    public static final float MODULE_HEADER_HEIGHT = 20f;
    public static final float MODULE_INNER_WIDTH = 97.5f;
    public static final float SETTING_START_Y = 20f;
    public static final float SETTING_PADDING = 4f;
    public static final float SETTING_BOTTOM_PADDING = 3f;
    public static final float SETTING_LEFT = 9f;
    public static final float SETTING_RIGHT = 94f;
    public static final float SLIDER_WIDTH = 85f;
    public static final float TEXT_SETTING_WIDTH = 38f;
    public static final float CLICKABLE_WIDTH = 85f;
    public static final int SEARCH_MAX_CHARS = 24;
    public static final float SEARCH_WIDTH = 70f;
    public static final float SEARCH_HEIGHT = 18f;
    public static final float SEARCH_GAP = 8f;
    public static final float SEARCH_ICON_X = 3.5f;
    public static final float SEARCH_TEXT_X = 19f;
    public static final float SEARCH_RIGHT_PADDING = 8f;

    public static final float HEADER_CENTER_ICON = 52f;
    public static final float HEADER_CENTER_TEXT = 54f;
    public static final float SETTING_TEXT_MAX_RIGHT = 79f;
    public static final float BOOLEAN_TOGGLE_X = 81f;
    public static final float BOOLEAN_TOGGLE_W = 15f;
    public static final float BOOLEAN_CIRCLE_START = 85.5f;
    public static final float BOOLEAN_CIRCLE_TRAVEL = 5.7f;
    public static final float MODE_CIRCLE_X = 91f;
    public static final float LIST_CIRCLE_X = 91f;
    public static final float TEXT_BOX_X = 57f;
    public static final float DOTS_X = 92.5f;

    public static final int BG_COLOR = ColorUtils.rgb(10, 19, 31);
    public static final int PANEL_BG = ColorUtils.rgb(18, 24, 38);
    public static final int PANEL_HEADER_BG = ColorUtils.rgb(24, 32, 50);
    public static final int SEPARATOR_COLOR = ColorUtils.rgb(40, 50, 70);
    public static final int MODULE_ENABLED_BG = ColorUtils.rgb(35, 50, 80);
    public static final int MODULE_DISABLED_BG = ColorUtils.rgb(22, 30, 45);
    public static final int MODULE_HOVER_BG = ColorUtils.rgb(30, 42, 65);

    public static final int TEXT_PRIMARY = ColorUtils.rgb(220, 230, 245);
    public static final int TEXT_SECONDARY = ColorUtils.rgb(130, 145, 170);
    public static final int TEXT_ENABLED = ColorUtils.rgb(240, 248, 255);
    public static final int TEXT_DISABLED = ColorUtils.rgba(220, 230, 245, 155);
    public static final int TEXT_WHITE = ColorUtils.rgb(255, 255, 255);

    public static final int LOGO_COLOR = ColorUtils.rgb(144, 109, 71);
    public static final int LOGO_COLOR_DARK = ColorUtils.rgb(113, 73, 38);
    public static final int LOGO_COLOR_LIGHT = ColorUtils.rgb(180, 145, 100);

    private ClickGuiNewLayout() {
    }

    public static int getSecondaryAccent(int colorTheme) {
        return ColorUtils.interpolateColor(colorTheme, ColorUtils.rgb(255, 255, 255), 0.45f);
    }

    public static int getPanelBg(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, PANEL_BG, 0.85f), 240);
    }

    public static int getPanelHeaderBg(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, PANEL_HEADER_BG, 0.75f), 250);
    }

    public static int getSeparator(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, SEPARATOR_COLOR, 0.70f), 180);
    }

    public static int getModuleEnabledBgTop(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, MODULE_ENABLED_BG, 0.65f), 230);
    }

    public static int getModuleEnabledBgBottom(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, MODULE_ENABLED_BG, 0.78f), 230);
    }

    public static int getModuleDisabledBgTop(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, MODULE_DISABLED_BG, 0.88f), 190);
    }

    public static int getModuleDisabledBgBottom(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, MODULE_DISABLED_BG, 0.92f), 170);
    }

    public static int getModuleHoverBg(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, MODULE_HOVER_BG, 0.75f), 200);
    }

    public static int getSliderTrack(int colorTheme) {
        return ColorUtils.interpolateColor(colorTheme, MODULE_DISABLED_BG, 0.78f);
    }

    public static int getToggleOff(int colorTheme) {
        return ColorUtils.interpolateColor(colorTheme, MODULE_DISABLED_BG, 0.72f);
    }

    public static int getSearchBg(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, PANEL_BG, 0.86f), 235);
    }

    public static int getSearchBorder(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, MODULE_DISABLED_BG, 0.65f), 180);
    }

    public static int getThemePanelBg(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, PANEL_BG, 0.86f), 235);
    }

    public static int getBorderLight(int colorTheme) {
        return ColorUtils.replAlpha(ColorUtils.interpolateColor(colorTheme, MODULE_DISABLED_BG, 0.68f), 160);
    }

    public static float getTotalCategoriesWidth(int categoryCount) {
        return (WIDTH * categoryCount) + ((CATEGORY_PANEL_STEP - WIDTH) * (categoryCount - 1));
    }

    public static float getCategoryPanelX(float x, int index) {
        return x + (index * CATEGORY_PANEL_STEP);
    }

    public static float getContentY(float y) {
        return y + 25f;
    }

    public static float getContentHeight() {
        return HEIGHT - 30f;
    }

    public static float getSearchX(float x, int categoryCount) {
        return x + (getTotalCategoriesWidth(categoryCount) / 2f) - (SEARCH_WIDTH / 2f);
    }

    public static float getSearchX(float x, int categoryCount, float searchWidth) {
        return x + (getTotalCategoriesWidth(categoryCount) / 2f) - (searchWidth / 2f);
    }

    public static float getSearchY(float y) {
        return y + HEIGHT + SEARCH_GAP;
    }

    public static boolean hasVisibleSettings(List<Setting> settings) {
        for (Setting setting : settings) {
            if (setting != null && setting.visible()) {
                return true;
            }
        }
        return false;
    }

    public static float calculateModeSettingHeight(ModeSetting modeSetting) {
        return modeSetting.getMods().size() * 10 + 12;
    }

    public static float calculateListSettingHeight(ListSetting listSetting) {
        int visibleCount = 0;
        for (BooleanSetting entry : listSetting.getSettings()) {
            if (entry.visible()) {
                visibleCount++;
            }
        }
        return visibleCount * 10 + 12;
    }

    public static float calculateSettingsHeight(Module module) {
        float height = 0f;
        List<Setting> settings = module.getSettings();
        if (settings == null || settings.isEmpty()) {
            return 0f;
        }

        boolean hasVisibleSetting = false;
        for (Setting setting : settings) {
            if (setting == null || !setting.visible()) {
                continue;
            }

            hasVisibleSetting = true;
            if (setting instanceof BooleanSetting || setting instanceof BindSetting) {
                height += 12f;
            } else if (setting instanceof TextSetting) {
                height += 12f;
            } else if (setting instanceof FloatSetting) {
                height += 22f;
            } else if (setting instanceof ModeSetting modeSetting) {
                height += calculateModeSettingHeight(modeSetting);
            } else if (setting instanceof ListSetting listSetting) {
                height += calculateListSettingHeight(listSetting);
            }
        }

        if (hasVisibleSetting) {
            height += SETTING_BOTTOM_PADDING;
        }
        return height;
    }

    public static float getModuleHeight(Module module, float openProgress) {
        return MODULE_HEADER_HEIGHT + (calculateSettingsHeight(module) * openProgress);
    }
}

package fun.popka.visuals.ui.clickgui;

import java.util.List;

import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.Setting;
import fun.popka.visuals.modules.settings.implement.BindSetting;
import fun.popka.visuals.modules.settings.implement.BooleanSetting;
import fun.popka.visuals.modules.settings.implement.FloatSetting;
import fun.popka.visuals.modules.settings.implement.ListSetting;
import fun.popka.visuals.modules.settings.implement.ModeSetting;
import fun.popka.visuals.modules.settings.implement.TextSetting;

public enum ClickGuiStyle {
    DROPDOWN {
        @Override public float getWidth() { return ClickGuiLayout.WIDTH; }
        @Override public float getHeight() { return ClickGuiLayout.HEIGHT; }
        @Override public float getCategoryPanelStep() { return ClickGuiLayout.CATEGORY_PANEL_STEP; }
        @Override public float getPanelRadius() { return ClickGuiLayout.PANEL_RADIUS; }
        @Override public float getThemePanelY() { return ClickGuiLayout.THEME_PANEL_Y; }
        @Override public float getModulePadding() { return ClickGuiLayout.MODULE_PADDING; }
        @Override public float getModuleGap() { return ClickGuiLayout.MODULE_GAP; }
        @Override public float getModuleHeaderHeight() { return ClickGuiLayout.MODULE_HEADER_HEIGHT; }
        @Override public float getModuleInnerWidth() { return ClickGuiLayout.MODULE_INNER_WIDTH; }
        @Override public float getSettingStartY() { return ClickGuiLayout.SETTING_START_Y; }
        @Override public float getSettingPadding() { return ClickGuiLayout.SETTING_PADDING; }
        @Override public float getSettingBottomPadding() { return ClickGuiLayout.SETTING_BOTTOM_PADDING; }
        @Override public float getSettingLeft() { return ClickGuiLayout.SETTING_LEFT; }
        @Override public float getSettingRight() { return ClickGuiLayout.SETTING_RIGHT; }
        @Override public float getSliderWidth() { return ClickGuiLayout.SLIDER_WIDTH; }
        @Override public float getTextSettingWidth() { return ClickGuiLayout.TEXT_SETTING_WIDTH; }
        @Override public float getClickableWidth() { return ClickGuiLayout.CLICKABLE_WIDTH; }
        @Override public int getSearchMaxChars() { return ClickGuiLayout.SEARCH_MAX_CHARS; }
        @Override public float getSearchWidth() { return ClickGuiLayout.SEARCH_WIDTH; }
        @Override public float getSearchHeight() { return ClickGuiLayout.SEARCH_HEIGHT; }
        @Override public float getSearchGap() { return ClickGuiLayout.SEARCH_GAP; }
        @Override public float getSearchIconX() { return ClickGuiLayout.SEARCH_ICON_X; }
        @Override public float getSearchTextX() { return ClickGuiLayout.SEARCH_TEXT_X; }
        @Override public float getSearchRightPadding() { return ClickGuiLayout.SEARCH_RIGHT_PADDING; }
        @Override public float getHeaderCenterIcon() { return ClickGuiLayout.HEADER_CENTER_ICON; }
        @Override public float getHeaderCenterText() { return ClickGuiLayout.HEADER_CENTER_TEXT; }
        @Override public float getSettingTextMaxRight() { return ClickGuiLayout.SETTING_TEXT_MAX_RIGHT; }
        @Override public float getBooleanToggleX() { return ClickGuiLayout.BOOLEAN_TOGGLE_X; }
        @Override public float getBooleanToggleW() { return ClickGuiLayout.BOOLEAN_TOGGLE_W; }
        @Override public float getBooleanCircleStart() { return ClickGuiLayout.BOOLEAN_CIRCLE_START; }
        @Override public float getBooleanCircleTravel() { return ClickGuiLayout.BOOLEAN_CIRCLE_TRAVEL; }
        @Override public float getModeCircleX() { return ClickGuiLayout.MODE_CIRCLE_X; }
        @Override public float getListCircleX() { return ClickGuiLayout.LIST_CIRCLE_X; }
        @Override public float getTextBoxX() { return ClickGuiLayout.TEXT_BOX_X; }
        @Override public float getDotsX() { return ClickGuiLayout.DOTS_X; }
        @Override public int getTextPrimary() { return ClickGuiLayout.TEXT_PRIMARY; }
        @Override public int getTextSecondary() { return ClickGuiLayout.TEXT_SECONDARY; }
        @Override public int getTextEnabled() { return ClickGuiLayout.TEXT_ENABLED; }
        @Override public int getTextDisabled() { return ClickGuiLayout.TEXT_DISABLED; }
        @Override public int getTextWhite() { return ClickGuiLayout.TEXT_WHITE; }
        @Override public int getSecondaryAccent(int colorTheme) { return ClickGuiLayout.getSecondaryAccent(colorTheme); }
        @Override public int getPanelBg(int colorTheme) { return ClickGuiLayout.getPanelBg(colorTheme); }
        @Override public int getPanelHeaderBg(int colorTheme) { return ClickGuiLayout.getPanelHeaderBg(colorTheme); }
        @Override public int getSeparator(int colorTheme) { return ClickGuiLayout.getSeparator(colorTheme); }
        @Override public int getModuleEnabledBgTop(int colorTheme) { return ClickGuiLayout.getModuleEnabledBgTop(colorTheme); }
        @Override public int getModuleEnabledBgBottom(int colorTheme) { return ClickGuiLayout.getModuleEnabledBgBottom(colorTheme); }
        @Override public int getModuleDisabledBgTop(int colorTheme) { return ClickGuiLayout.getModuleDisabledBgTop(colorTheme); }
        @Override public int getModuleDisabledBgBottom(int colorTheme) { return ClickGuiLayout.getModuleDisabledBgBottom(colorTheme); }
        @Override public int getSliderTrack(int colorTheme) { return ClickGuiLayout.getSliderTrack(colorTheme); }
        @Override public int getToggleOff(int colorTheme) { return ClickGuiLayout.getToggleOff(colorTheme); }
        @Override public int getSearchBg(int colorTheme) { return ClickGuiLayout.getSearchBg(colorTheme); }
        @Override public int getSearchBorder(int colorTheme) { return ClickGuiLayout.getSearchBorder(colorTheme); }
        @Override public int getThemePanelBg(int colorTheme) { return ClickGuiLayout.getThemePanelBg(colorTheme); }
        @Override public int getBorderLight(int colorTheme) { return ClickGuiLayout.getBorderLight(colorTheme); }
        @Override public float getTotalCategoriesWidth(int categoryCount) { return ClickGuiLayout.getTotalCategoriesWidth(categoryCount); }
        @Override public float getCategoryPanelX(float x, int index) { return ClickGuiLayout.getCategoryPanelX(x, index); }
        @Override public float getContentY(float y) { return ClickGuiLayout.getContentY(y); }
        @Override public float getContentHeight() { return ClickGuiLayout.getContentHeight(); }
        @Override public float getSearchX(float x, int categoryCount) { return ClickGuiLayout.getSearchX(x, categoryCount); }
        @Override public float getSearchX(float x, int categoryCount, float searchWidth) { return ClickGuiLayout.getSearchX(x, categoryCount, searchWidth); }
        @Override public float getSearchY(float y) { return ClickGuiLayout.getSearchY(y); }
        @Override public boolean hasVisibleSettings(List<Setting> settings) { return ClickGuiLayout.hasVisibleSettings(settings); }
        @Override public float calculateModeSettingHeight(ModeSetting modeSetting) { return ClickGuiLayout.calculateModeSettingHeight(modeSetting); }
        @Override public float calculateListSettingHeight(ListSetting listSetting) { return ClickGuiLayout.calculateListSettingHeight(listSetting); }
        @Override public float calculateSettingsHeight(Module module) { return ClickGuiLayout.calculateSettingsHeight(module); }
        @Override public float getModuleHeight(Module module, float openProgress) { return ClickGuiLayout.getModuleHeight(module, openProgress); }
    },
    NEW {
        @Override public float getWidth() { return ClickGuiNewLayout.WIDTH; }
        @Override public float getHeight() { return ClickGuiNewLayout.HEIGHT; }
        @Override public float getCategoryPanelStep() { return ClickGuiNewLayout.CATEGORY_PANEL_STEP; }
        @Override public float getPanelRadius() { return ClickGuiNewLayout.PANEL_RADIUS; }
        @Override public float getThemePanelY() { return ClickGuiNewLayout.THEME_PANEL_Y; }
        @Override public float getModulePadding() { return ClickGuiNewLayout.MODULE_PADDING; }
        @Override public float getModuleGap() { return ClickGuiNewLayout.MODULE_GAP; }
        @Override public float getModuleHeaderHeight() { return ClickGuiNewLayout.MODULE_HEADER_HEIGHT; }
        @Override public float getModuleInnerWidth() { return ClickGuiNewLayout.MODULE_INNER_WIDTH; }
        @Override public float getSettingStartY() { return ClickGuiNewLayout.SETTING_START_Y; }
        @Override public float getSettingPadding() { return ClickGuiNewLayout.SETTING_PADDING; }
        @Override public float getSettingBottomPadding() { return ClickGuiNewLayout.SETTING_BOTTOM_PADDING; }
        @Override public float getSettingLeft() { return ClickGuiNewLayout.SETTING_LEFT; }
        @Override public float getSettingRight() { return ClickGuiNewLayout.SETTING_RIGHT; }
        @Override public float getSliderWidth() { return ClickGuiNewLayout.SLIDER_WIDTH; }
        @Override public float getTextSettingWidth() { return ClickGuiNewLayout.TEXT_SETTING_WIDTH; }
        @Override public float getClickableWidth() { return ClickGuiNewLayout.CLICKABLE_WIDTH; }
        @Override public int getSearchMaxChars() { return ClickGuiNewLayout.SEARCH_MAX_CHARS; }
        @Override public float getSearchWidth() { return ClickGuiNewLayout.SEARCH_WIDTH; }
        @Override public float getSearchHeight() { return ClickGuiNewLayout.SEARCH_HEIGHT; }
        @Override public float getSearchGap() { return ClickGuiNewLayout.SEARCH_GAP; }
        @Override public float getSearchIconX() { return ClickGuiNewLayout.SEARCH_ICON_X; }
        @Override public float getSearchTextX() { return ClickGuiNewLayout.SEARCH_TEXT_X; }
        @Override public float getSearchRightPadding() { return ClickGuiNewLayout.SEARCH_RIGHT_PADDING; }
        @Override public float getHeaderCenterIcon() { return ClickGuiNewLayout.HEADER_CENTER_ICON; }
        @Override public float getHeaderCenterText() { return ClickGuiNewLayout.HEADER_CENTER_TEXT; }
        @Override public float getSettingTextMaxRight() { return ClickGuiNewLayout.SETTING_TEXT_MAX_RIGHT; }
        @Override public float getBooleanToggleX() { return ClickGuiNewLayout.BOOLEAN_TOGGLE_X; }
        @Override public float getBooleanToggleW() { return ClickGuiNewLayout.BOOLEAN_TOGGLE_W; }
        @Override public float getBooleanCircleStart() { return ClickGuiNewLayout.BOOLEAN_CIRCLE_START; }
        @Override public float getBooleanCircleTravel() { return ClickGuiNewLayout.BOOLEAN_CIRCLE_TRAVEL; }
        @Override public float getModeCircleX() { return ClickGuiNewLayout.MODE_CIRCLE_X; }
        @Override public float getListCircleX() { return ClickGuiNewLayout.LIST_CIRCLE_X; }
        @Override public float getTextBoxX() { return ClickGuiNewLayout.TEXT_BOX_X; }
        @Override public float getDotsX() { return ClickGuiNewLayout.DOTS_X; }
        @Override public int getTextPrimary() { return ClickGuiNewLayout.TEXT_PRIMARY; }
        @Override public int getTextSecondary() { return ClickGuiNewLayout.TEXT_SECONDARY; }
        @Override public int getTextEnabled() { return ClickGuiNewLayout.TEXT_ENABLED; }
        @Override public int getTextDisabled() { return ClickGuiNewLayout.TEXT_DISABLED; }
        @Override public int getTextWhite() { return ClickGuiNewLayout.TEXT_WHITE; }
        @Override public int getSecondaryAccent(int colorTheme) { return ClickGuiNewLayout.getSecondaryAccent(colorTheme); }
        @Override public int getPanelBg(int colorTheme) { return ClickGuiNewLayout.getPanelBg(colorTheme); }
        @Override public int getPanelHeaderBg(int colorTheme) { return ClickGuiNewLayout.getPanelHeaderBg(colorTheme); }
        @Override public int getSeparator(int colorTheme) { return ClickGuiNewLayout.getSeparator(colorTheme); }
        @Override public int getModuleEnabledBgTop(int colorTheme) { return ClickGuiNewLayout.getModuleEnabledBgTop(colorTheme); }
        @Override public int getModuleEnabledBgBottom(int colorTheme) { return ClickGuiNewLayout.getModuleEnabledBgBottom(colorTheme); }
        @Override public int getModuleDisabledBgTop(int colorTheme) { return ClickGuiNewLayout.getModuleDisabledBgTop(colorTheme); }
        @Override public int getModuleDisabledBgBottom(int colorTheme) { return ClickGuiNewLayout.getModuleDisabledBgBottom(colorTheme); }
        @Override public int getSliderTrack(int colorTheme) { return ClickGuiNewLayout.getSliderTrack(colorTheme); }
        @Override public int getToggleOff(int colorTheme) { return ClickGuiNewLayout.getToggleOff(colorTheme); }
        @Override public int getSearchBg(int colorTheme) { return ClickGuiNewLayout.getSearchBg(colorTheme); }
        @Override public int getSearchBorder(int colorTheme) { return ClickGuiNewLayout.getSearchBorder(colorTheme); }
        @Override public int getThemePanelBg(int colorTheme) { return ClickGuiNewLayout.getThemePanelBg(colorTheme); }
        @Override public int getBorderLight(int colorTheme) { return ClickGuiNewLayout.getBorderLight(colorTheme); }
        @Override public float getTotalCategoriesWidth(int categoryCount) { return ClickGuiNewLayout.getTotalCategoriesWidth(categoryCount); }
        @Override public float getCategoryPanelX(float x, int index) { return ClickGuiNewLayout.getCategoryPanelX(x, index); }
        @Override public float getContentY(float y) { return ClickGuiNewLayout.getContentY(y); }
        @Override public float getContentHeight() { return ClickGuiNewLayout.getContentHeight(); }
        @Override public float getSearchX(float x, int categoryCount) { return ClickGuiNewLayout.getSearchX(x, categoryCount); }
        @Override public float getSearchX(float x, int categoryCount, float searchWidth) { return ClickGuiNewLayout.getSearchX(x, categoryCount, searchWidth); }
        @Override public float getSearchY(float y) { return ClickGuiNewLayout.getSearchY(y); }
        @Override public boolean hasVisibleSettings(List<Setting> settings) { return ClickGuiNewLayout.hasVisibleSettings(settings); }
        @Override public float calculateModeSettingHeight(ModeSetting modeSetting) { return ClickGuiNewLayout.calculateModeSettingHeight(modeSetting); }
        @Override public float calculateListSettingHeight(ListSetting listSetting) { return ClickGuiNewLayout.calculateListSettingHeight(listSetting); }
        @Override public float calculateSettingsHeight(Module module) { return ClickGuiNewLayout.calculateSettingsHeight(module); }
        @Override public float getModuleHeight(Module module, float openProgress) { return ClickGuiNewLayout.getModuleHeight(module, openProgress); }
    };

    public abstract float getWidth();
    public abstract float getHeight();
    public abstract float getCategoryPanelStep();
    public abstract float getPanelRadius();
    public abstract float getThemePanelY();
    public abstract float getModulePadding();
    public abstract float getModuleGap();
    public abstract float getModuleHeaderHeight();
    public abstract float getModuleInnerWidth();
    public abstract float getSettingStartY();
    public abstract float getSettingPadding();
    public abstract float getSettingBottomPadding();
    public abstract float getSettingLeft();
    public abstract float getSettingRight();
    public abstract float getSliderWidth();
    public abstract float getTextSettingWidth();
    public abstract float getClickableWidth();
    public abstract int getSearchMaxChars();
    public abstract float getSearchWidth();
    public abstract float getSearchHeight();
    public abstract float getSearchGap();
    public abstract float getSearchIconX();
    public abstract float getSearchTextX();
    public abstract float getSearchRightPadding();
    public abstract float getHeaderCenterIcon();
    public abstract float getHeaderCenterText();
    public abstract float getSettingTextMaxRight();
    public abstract float getBooleanToggleX();
    public abstract float getBooleanToggleW();
    public abstract float getBooleanCircleStart();
    public abstract float getBooleanCircleTravel();
    public abstract float getModeCircleX();
    public abstract float getListCircleX();
    public abstract float getTextBoxX();
    public abstract float getDotsX();
    public abstract int getTextPrimary();
    public abstract int getTextSecondary();
    public abstract int getTextEnabled();
    public abstract int getTextDisabled();
    public abstract int getTextWhite();

    public abstract int getSecondaryAccent(int colorTheme);
    public abstract int getPanelBg(int colorTheme);
    public abstract int getPanelHeaderBg(int colorTheme);
    public abstract int getSeparator(int colorTheme);
    public abstract int getModuleEnabledBgTop(int colorTheme);
    public abstract int getModuleEnabledBgBottom(int colorTheme);
    public abstract int getModuleDisabledBgTop(int colorTheme);
    public abstract int getModuleDisabledBgBottom(int colorTheme);
    public abstract int getSliderTrack(int colorTheme);
    public abstract int getToggleOff(int colorTheme);
    public abstract int getSearchBg(int colorTheme);
    public abstract int getSearchBorder(int colorTheme);
    public abstract int getThemePanelBg(int colorTheme);
    public abstract int getBorderLight(int colorTheme);

    public abstract float getTotalCategoriesWidth(int categoryCount);
    public abstract float getCategoryPanelX(float x, int index);
    public abstract float getContentY(float y);
    public abstract float getContentHeight();
    public abstract float getSearchX(float x, int categoryCount);
    public abstract float getSearchX(float x, int categoryCount, float searchWidth);
    public abstract float getSearchY(float y);

    public abstract boolean hasVisibleSettings(List<Setting> settings);
    public abstract float calculateModeSettingHeight(ModeSetting modeSetting);
    public abstract float calculateListSettingHeight(ListSetting listSetting);
    public abstract float calculateSettingsHeight(Module module);
    public abstract float getModuleHeight(Module module, float openProgress);

    public static ClickGuiStyle fromName(String name) {
        if (name == null) {
            return DROPDOWN;
        }
        for (ClickGuiStyle style : values()) {
            if (style.name().equalsIgnoreCase(name)) {
                return style;
            }
        }
        return DROPDOWN;
    }
}

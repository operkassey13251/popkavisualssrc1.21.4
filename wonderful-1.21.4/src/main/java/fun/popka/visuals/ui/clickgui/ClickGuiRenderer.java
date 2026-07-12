package fun.popka.visuals.ui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import fun.popka.Popka;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.input.KeyBoardUtils;
import fun.popka.api.utils.math.HoveringUtils;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.api.utils.scissor.ScissorUtils;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiRenderer {
    private final ClickGuiState state;
    private final ClickGuiSettingRenderer settingRenderer;

    public ClickGuiRenderer(ClickGuiState state, ClickGuiSettingRenderer settingRenderer) {
        this.state = state;
        this.settingRenderer = settingRenderer;
    }

    private ClickGuiStyle layout() {
        return state.getStyle();
    }

    public void render(DrawContext context, int mouseX, int mouseY, Window window, float animationProgress) {
        if (window == null) {
            return;
        }

        float alphaMul = MathHelper.clamp(animationProgress, 0.0f, 1.0f);
        int shadeColor = getFadeShadeColor(alphaMul, 120);
        int colorTheme = getThemeColor();
        Module hoveredModule = null;

        Module.ModuleCategory[] categories = Module.ModuleCategory.values();
        for (int i = 0; i < categories.length; i++) {
            Module.ModuleCategory category = categories[i];
            float panelX = layout().getCategoryPanelX(state.getX(), i);
            Module categoryHoveredModule = renderCategoryPanel(context, mouseX, mouseY, panelX, category, colorTheme, alphaMul, shadeColor);
            if (categoryHoveredModule != null) {
                hoveredModule = categoryHoveredModule;
            }
        }

        renderSearch(context, categories.length, colorTheme, alphaMul, getFadeShadeColor(alphaMul, 95));
        renderDescription(context, window, hoveredModule, colorTheme, animationProgress);
    }

    private Module renderCategoryPanel(DrawContext context, int mouseX, int mouseY, float panelX, Module.ModuleCategory category, int colorTheme, float alphaMul, int shadeColor) {
        float panelY = state.getY() + state.getRenderOffsetY();
        RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY, layout().getWidth(), layout().getHeight(), layout().getPanelRadius(), alpha(layout().getPanelBg(colorTheme), alphaMul));
        RenderUtils.drawGradientRect(context.getMatrices(), panelX, panelY, layout().getWidth(), 24f, layout().getPanelRadius(), alpha(layout().getPanelHeaderBg(colorTheme), alphaMul), alpha(layout().getPanelBg(colorTheme), alphaMul));
        RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY + 23, layout().getWidth(), 0.5F, 0, alpha(layout().getSeparator(colorTheme), alphaMul));
        if (((shadeColor >> 24) & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY, layout().getWidth(), layout().getHeight(), layout().getPanelRadius(), shadeColor);
        }

        icons(14).drawCenteredString(context.getMatrices(), category.getIcons(), panelX + layout().getHeaderCenterIcon() - (issue(15).getWidth(category.getName()) / 2F) - 2, panelY + 10, alpha(colorTheme, alphaMul));
        issue(15).drawCenteredString(context.getMatrices(), category.getName(), panelX + layout().getHeaderCenterText(), panelY + 9, alpha(layout().getTextPrimary(), alphaMul));

        float contentY = layout().getContentY(panelY);
        float contentHeight = layout().getContentHeight();
        state.clampScroll(category, contentHeight);
        float moduleY = contentY + state.getScroll(category);
        Module hoveredModule = null;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(panelX, contentY, layout().getWidth(), contentHeight);

        for (Module module : state.getModules(category)) {
            float openProgress = state.getOpenProgress(module);
            float moduleHeight = layout().getModuleHeight(module, openProgress);

            if (moduleY + moduleHeight + layout().getModuleGap() >= contentY && moduleY <= contentY + contentHeight) {
                Module moduleHover = renderModule(context, mouseX, mouseY, panelX, moduleY, module, openProgress, moduleHeight, colorTheme, alphaMul, shadeColor);
                if (moduleHover != null) {
                    hoveredModule = moduleHover;
                }
            }

            moduleY += layout().getModuleGap() + moduleHeight;
        }

        ScissorUtils.pop();
        return hoveredModule;
    }

    private Module renderModule(DrawContext context, int mouseX, int mouseY, float panelX, float moduleY, Module module, float openProgress, float moduleHeight, int colorTheme, float alphaMul, int shadeColor) {
        List<Setting> settings = module.getSettings();
        renderModuleBackground(context, panelX, moduleY, moduleHeight, module.isEnable(), colorTheme, shadeColor);

        String moduleName = module.getName();
        String bindText = "";
        if (state.getBindingModule() == module) {
            bindText = " [...]";
        } else if (module.getKey() != -1) {
            bindText = " [" + state.toEnglish(KeyBoardUtils.getBindName(module.getKey())) + "]";
        }

        int nameColor = module.isEnable() ? alpha(layout().getTextEnabled(), alphaMul) : alpha(layout().getTextDisabled(), alphaMul);
        int bindColor = module.isEnable() ? alpha(ColorUtils.replAlpha(colorTheme, 200), alphaMul) : alpha(ColorUtils.rgba(130, 140, 158, 160), alphaMul);

        issue(14).draw(context.getMatrices(), moduleName, panelX + layout().getSettingLeft(), moduleY + 8, nameColor);
        if (!bindText.isEmpty()) {
            float nameWidth = issue(14).getWidth(moduleName);
            issue(11).draw(context.getMatrices(), bindText, panelX + layout().getSettingLeft() + nameWidth, moduleY + 9, bindColor);
        }

        if (settings != null && !settings.isEmpty() && layout().hasVisibleSettings(settings)) {
            renderModuleDots(context, panelX, moduleY, module, module.isEnable(), colorTheme, alphaMul);
        }

        if (settings != null && !settings.isEmpty()) {
            settingRenderer.render(context, module, panelX, moduleY, openProgress, colorTheme, mouseX, mouseY, state);
        }

        if (HoveringUtils.isHovered(mouseX, mouseY, panelX + layout().getModulePadding(), moduleY, layout().getModuleInnerWidth(), moduleHeight)) {
            return module;
        }
        return null;
    }

    private void renderModuleBackground(DrawContext context, float panelX, float moduleY, float moduleHeight, boolean enabled, int colorTheme, int shadeColor) {
        if (enabled) {
            RenderUtils.drawRoundedRect(context.getMatrices(), panelX + layout().getModulePadding(), moduleY - 0.5f, layout().getModuleInnerWidth(), moduleHeight + 1, 5, layout().getModuleEnabledBgBottom(colorTheme));
            RenderUtils.drawGradientRect(context.getMatrices(), panelX + layout().getModulePadding() + 0.5f, moduleY, layout().getModuleInnerWidth() - 1f, moduleHeight, 4, layout().getModuleEnabledBgTop(colorTheme), layout().getModuleEnabledBgBottom(colorTheme), false);
            if (((shadeColor >> 24) & 0xFF) > 0) {
                RenderUtils.drawRoundedRect(context.getMatrices(), panelX + layout().getModulePadding() + 0.5f, moduleY, layout().getModuleInnerWidth() - 1f, moduleHeight, 4, shadeColor);
            }
            return;
        }

        RenderUtils.drawRoundedRect(context.getMatrices(), panelX + layout().getModulePadding(), moduleY - 0.5f, layout().getModuleInnerWidth(), moduleHeight + 1, 5, layout().getModuleDisabledBgBottom(colorTheme));
        RenderUtils.drawGradientRect(context.getMatrices(), panelX + layout().getModulePadding() + 0.5f, moduleY, layout().getModuleInnerWidth() - 1f, moduleHeight, 4, layout().getModuleDisabledBgTop(colorTheme), layout().getModuleDisabledBgBottom(colorTheme), false);
        if (((shadeColor >> 24) & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.getMatrices(), panelX + layout().getModulePadding() + 0.5f, moduleY, layout().getModuleInnerWidth() - 1f, moduleHeight, 4, shadeColor);
        }
    }

    private void renderModuleDots(DrawContext context, float panelX, float moduleY, Module module, boolean enabled, int colorTheme, float alphaMul) {
        int dotsColor = enabled ? alpha(layout().getSecondaryAccent(colorTheme), alphaMul) : alpha(layout().getTextSecondary(), alphaMul);
        float dotsX = panelX + layout().getDotsX();
        float baseY = moduleY + 10f;
        float spacing = 2f;
        float radius = 2.1f;
        float bottomXOffset = 2.1f;
        float angle = state.updateDotsRotation(module, module.isOpen() ? (float) (Math.PI / 2f) : 0f);
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] offsets = {
                {0f, -spacing},
                {-bottomXOffset, spacing},
                {bottomXOffset, spacing}
        };

        for (float[] offset : offsets) {
            float rx = offset[0] * cos - offset[1] * sin;
            float ry = offset[0] * sin + offset[1] * cos;
            RenderUtils.drawRoundCircle(context.getMatrices(), dotsX + rx, baseY + ry, radius, dotsColor);
        }
    }

    private int getThemeColor() {
        if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            return Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        }
        return ColorUtils.getThemeColor();
    }

    private void renderSearch(DrawContext context, int categoryCount, int colorTheme, float alphaMul, int shadeColor) {
        float searchY = layout().getSearchY(state.getY() + state.getRenderOffsetY());
        float searchW = getSearchWidth();
        float searchX = layout().getSearchX(state.getX(), categoryCount, searchW);
        float searchH = layout().getSearchHeight();
        float selectionPaddingLeft = 3.0f;
        float selectionPaddingRight = 1.5f;
        int borderColor = alpha(layout().getSearchBorder(colorTheme), alphaMul);

        RenderUtils.drawRoundedRect(context.getMatrices(), searchX - 0.5f, searchY - 0.5f, searchW + 1f, searchH + 1f, 5.5f, borderColor);
        RenderUtils.drawRoundedRect(context.getMatrices(), searchX, searchY, searchW, searchH, 5f, alpha(layout().getSearchBg(colorTheme), alphaMul));
        if (((shadeColor >> 24) & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.getMatrices(), searchX, searchY, searchW, searchH, 5f, shadeColor);
        }

        String query = state.getSearchText();
        String text = query.isEmpty() ? "Search..." : query;
        int textColor = query.isEmpty()
                ? alpha(layout().getTextSecondary(), alphaMul)
                : alpha(layout().getTextPrimary(), alphaMul);

        float iconX = searchX + layout().getSearchIconX();
        float textX = searchX + layout().getSearchTextX();
        float textY = searchY + 6.2f;
        iconsNew(18).drawGradientStringHorizontal(context.getMatrices(), "l", iconX + 2, searchY + 6.5f, alpha(colorTheme, alphaMul), alpha(layout().getSecondaryAccent(colorTheme), alphaMul));

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(
                textX - selectionPaddingLeft,
                searchY,
                searchW - layout().getSearchTextX() - layout().getSearchRightPadding() + selectionPaddingLeft,
                searchH
        );
        if (!query.isEmpty() && state.hasSearchSelection()) {
            int selectionStart = state.getSearchSelectionStart();
            int selectionEnd = state.getSearchSelectionEnd();
            float selectedX = textX + issue(14).getWidth(query.substring(0, selectionStart)) - selectionPaddingLeft;
            float selectedW = issue(14).getWidth(query.substring(selectionStart, selectionEnd)) + selectionPaddingLeft + selectionPaddingRight;
            RenderUtils.drawRoundedRect(context.getMatrices(), selectedX, searchY + 3.8f, selectedW, 10.5f, 1.5f, alpha(ColorUtils.replAlpha(colorTheme, 120), alphaMul));
        }

        issue(14).draw(context.getMatrices(), text, textX, textY + 1, textColor);
        if (state.isSearchActive() && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            float cursorX = textX + issue(14).getWidth(query.substring(0, Math.min(state.getSearchCursor(), query.length())));
            RenderUtils.drawRoundedRect(context.getMatrices(), cursorX + 1f, searchY + 4.5f, 0.8f, 9f, 0f, alpha(colorTheme, alphaMul));
        }
        ScissorUtils.pop();
    }

    private void renderDescription(DrawContext context, Window window, Module hoveredModule, int colorTheme, float alphaMul) {
        if (hoveredModule == null) {
            return;
        }

        String description = hoveredModule.getDisplayDescription();
        if (description == null || description.isBlank() || "NULLABLE".equalsIgnoreCase(description) || "desc".equalsIgnoreCase(description)) {
            return;
        }

        Font descriptionFont = issue(16);
        float maxWidth = window.getScaledWidth() - 40.0f;
        List<String> lines = wrapDescription(descriptionFont, description, maxWidth);
        if (lines.isEmpty()) {
            return;
        }

        float lineHeight = descriptionFont.getHeight() - 2.0f;
        float boxHeight = lines.size() * lineHeight;
        float centerX = window.getScaledWidth() * 0.5f;
        float startY = layout().getThemePanelY() - boxHeight - 6.0f;

        for (int i = 0; i < lines.size(); i++) {
            descriptionFont.drawCenteredString(context.getMatrices(), lines.get(i), centerX, startY + i * lineHeight, ColorUtils.applyAlpha(layout().getTextPrimary(), alphaMul));
        }
    }

    private List<String> wrapDescription(Font font, String text, float maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.trim().split("\\s+");
        if (words.length == 0) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.getWidth(candidate) <= maxWidth || currentLine.isEmpty()) {
                currentLine.setLength(0);
                currentLine.append(candidate);
                continue;
            }

            lines.add(currentLine.toString());
            currentLine.setLength(0);
            currentLine.append(word);
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private float getSearchWidth() {
        String query = state.getSearchText();
        String text = query.isEmpty() ? "Search..." : query;
        float contentWidth = layout().getSearchTextX() + issue(14).getWidth(text) + layout().getSearchRightPadding();
        return Math.max(layout().getSearchWidth(), contentWidth);
    }

    private Font issue(int size) {
        return Fonts.getFont("suisse", size);
    }

    private Font icons(int size) {
        return Fonts.getFont("icon", size);
    }

    private Font iconsNew(int size) {
        return Fonts.getFont("icon1", size);
    }

    private int alpha(int color, float alphaMul) {
        return ColorUtils.applyAlpha(color, alphaMul);
    }

    private int getFadeShadeColor(float alphaMul, int maxAlpha) {
        int alpha = MathHelper.clamp((int) ((1.0f - alphaMul) * maxAlpha), 0, 255);
        return ColorUtils.rgba(0, 0, 0, alpha);
    }
}

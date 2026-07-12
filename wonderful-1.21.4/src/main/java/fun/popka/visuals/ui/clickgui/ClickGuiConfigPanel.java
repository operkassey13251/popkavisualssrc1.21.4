package fun.popka.visuals.ui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import fun.popka.Popka;
import fun.popka.api.QClient;
import fun.popka.api.utils.animation.AnimationUtils;
import fun.popka.api.utils.animation.Easings;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.math.HoveringUtils;
import fun.popka.api.utils.notification.NotificationManager;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.api.utils.scissor.ScissorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiConfigPanel implements QClient {
    private static final float PANEL_GAP = ClickGuiLayout.CATEGORY_PANEL_STEP;
    private static final float SLIDE_DISTANCE = 220f;
    private static final float HEADER_HEIGHT = 24f;
    private static final float SEPARATOR_Y = 23f;
    private static final float CONTENT_TOP = 25f;
    private static final float CONTENT_BOTTOM_PADDING = 4f;
    private static final float BUTTON_AREA_HEIGHT = 26f;
    private static final float BUTTON_HEIGHT = 18f;
    private static final float BUTTON_GAP = 4f;
    private static final float CONFIG_ROW_HEIGHT = 14f;
    private static final float CONFIG_ROW_GAP = 2f;
    private static final float ROW_BTN_WIDTH = 40f;
    private static final float ROW_BTN_HEIGHT = 13f;
    private static final float ROW_BTN_GAP = 2f;
    private static final float EXPANSION_EXTRA = 18f;
    private static final float CONFIG_NAME_LEFT = 6f;
    private static final float CONFIG_DOT_X = 94f;
    private static final float CONFIG_DOT_RADIUS = 3.5f;
    private static final int CONFIG_NAME_MAX_CHARS = 18;
    private static final long DOUBLE_CLICK_MS = 350L;
    private static final String HEADER_NAME = "Configs";
    private static final String HEADER_ICON = "f";
    private static final String CONFIG_EXTENSION = ".wonder";
    private static final int DOT_RED = ColorUtils.rgb(205, 65, 65);
    private static final int DOT_GREEN = ColorUtils.rgb(70, 200, 110);

    private static final String[] BUTTON_LABELS = {"Add", "Folder"};

    private final ClickGuiState state;
    private final AnimationUtils slideAnimation = new AnimationUtils(0f, 6.5f, Easings.CUBIC_OUT);
    private final AnimationUtils scrollAnimation = new AnimationUtils(0f, 8f, Easings.CUBIC_OUT);
    private final Map<String, AnimationUtils> buttonHoverAnimations = new HashMap<>();
    private final Map<String, AnimationUtils> configHoverAnimations = new HashMap<>();
    private final Map<String, AnimationUtils> rowButtonHoverAnimations = new HashMap<>();

    private float scrollTarget = 0f;
    private boolean textInputActive = false;
    private boolean renaming = false;
    private String renamingTarget = null;
    private String textInputBuffer = "";
    private long lastConfigRefresh = 0L;
    private List<String> cachedConfigs = new ArrayList<>();
    private String lastClickConfig = null;
    private long lastClickTime = 0L;

    public ClickGuiConfigPanel(ClickGuiState state) {
        this.state = state;
    }

    public void update(boolean closing) {
        if (closing) {
            slideAnimation.setEasing(Easings.CUBIC_IN);
            slideAnimation.update(0f);
            cancelTextInput();
        } else {
            slideAnimation.setEasing(Easings.CUBIC_OUT);
            slideAnimation.update(1f);
        }
    }

    public float getSlideProgress() {
        return MathHelper.clamp(slideAnimation.getValue(), 0.0f, 1.0f);
    }

    public float getRestingX() {
        return state.getX() - PANEL_GAP;
    }

    public float getPanelX() {
        return getRestingX() - (1.0f - getSlideProgress()) * SLIDE_DISTANCE;
    }

    public float getPanelY() {
        return state.getY() + state.getRenderOffsetY();
    }

    public boolean isTextInputActive() {
        return textInputActive;
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

        refreshConfigs();

        float listTop = panelY + CONTENT_TOP;
        float buttonAreaTop = panelY + ClickGuiLayout.HEIGHT - BUTTON_AREA_HEIGHT - CONTENT_BOTTOM_PADDING;
        float listHeight = buttonAreaTop - listTop - 2f;

        clampScroll(listHeight);
        scrollAnimation.update(scrollTarget);
        float scroll = scrollAnimation.getValue();

        RenderUtils.drawRoundedRect(context.getMatrices(), panelX + ClickGuiLayout.MODULE_PADDING, buttonAreaTop - 1.5f, ClickGuiLayout.MODULE_INNER_WIDTH, 0.5f, 0, alpha(ClickGuiLayout.getSeparator(colorTheme), alphaMul));

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(panelX, listTop, ClickGuiLayout.WIDTH, listHeight);

        float rowY = listTop + scroll;
        String currentConfig = Popka.INSTANCE.configStorage.currentConfig;
        int count = cachedConfigs.size();
        float[] hovers = new float[count];
        float[] rowYs = new float[count];
        float[] rowHeights = new float[count];

        for (int i = 0; i < count; i++) {
            AnimationUtils anim = getConfigHoverAnimation(cachedConfigs.get(i), false);
            float h = anim.getValue();
            hovers[i] = h;
            rowYs[i] = rowY;
            rowHeights[i] = CONFIG_ROW_HEIGHT + h * EXPANSION_EXTRA;
            rowY += rowHeights[i] + CONFIG_ROW_GAP;
        }

        for (int i = 0; i < count; i++) {
            boolean isHovered = HoveringUtils.isHovered(mouseX, mouseY, panelX + ClickGuiLayout.MODULE_PADDING, rowYs[i], ClickGuiLayout.MODULE_INNER_WIDTH, rowHeights[i]);
            AnimationUtils anim = getConfigHoverAnimation(cachedConfigs.get(i), isHovered);
            anim.update(isHovered ? 1f : 0f);
            hovers[i] = anim.getValue();
        }

        rowY = listTop + scroll;
        for (int i = 0; i < count; i++) {
            rowYs[i] = rowY;
            rowHeights[i] = CONFIG_ROW_HEIGHT + hovers[i] * EXPANSION_EXTRA;
            rowY += rowHeights[i] + CONFIG_ROW_GAP;
        }

        for (int i = 0; i < count; i++) {
            if (rowYs[i] + rowHeights[i] >= listTop && rowYs[i] <= listTop + listHeight) {
                String configName = cachedConfigs.get(i);
                boolean isCurrent = configName.equals(currentConfig);
                renderConfigRow(context, panelX, rowYs[i], rowHeights[i], hovers[i], configName, isCurrent, colorTheme, alphaMul, mouseX, mouseY);
            }
        }

        ScissorUtils.pop();

        if (!textInputActive) {
            renderButtons(context, mouseX, mouseY, panelX, buttonAreaTop, colorTheme, alphaMul);
        } else {
            renderTextInput(context, panelX, buttonAreaTop, colorTheme, alphaMul);
        }
    }

    private void renderConfigRow(DrawContext context, float panelX, float rowY, float rowHeight, float hover, String name, boolean isCurrent, int colorTheme, float alphaMul, int mouseX, int mouseY) {
        float intensity = isCurrent ? 1.0f : hover;

        if (intensity > 0.01f) {
            int bgTop = ColorUtils.applyAlpha(ClickGuiLayout.getModuleEnabledBgTop(colorTheme), alphaMul * intensity);
            int bgBottom = ColorUtils.applyAlpha(ClickGuiLayout.getModuleEnabledBgBottom(colorTheme), alphaMul * intensity);
            RenderUtils.drawRoundedRect(context.getMatrices(), panelX + ClickGuiLayout.MODULE_PADDING, rowY - 0.5f, ClickGuiLayout.MODULE_INNER_WIDTH, rowHeight + 1, 4, bgBottom);
            RenderUtils.drawGradientRect(context.getMatrices(), panelX + ClickGuiLayout.MODULE_PADDING + 0.5f, rowY, ClickGuiLayout.MODULE_INNER_WIDTH - 1f, rowHeight, 4, bgTop, bgBottom, false);
        }

        int nameColor = isCurrent ? alpha(colorTheme, alphaMul) : alpha(ClickGuiLayout.TEXT_PRIMARY, alphaMul);
        String displayName = truncateName(name, CONFIG_DOT_X - 2f - CONFIG_NAME_LEFT);
        issue(13).draw(context.getMatrices(), displayName, panelX + CONFIG_NAME_LEFT, rowY + 5f, nameColor);

        int dotColor = isCurrent ? DOT_GREEN : DOT_RED;
        RenderUtils.drawRoundCircle(context.getMatrices(), panelX + CONFIG_DOT_X, rowY + CONFIG_ROW_HEIGHT / 2f, CONFIG_DOT_RADIUS, alpha(dotColor, alphaMul));

        if (hover > 0.01f) {
            float expansion = hover * EXPANSION_EXTRA;
            float btnY = rowY + CONFIG_ROW_HEIGHT + (expansion - ROW_BTN_HEIGHT) / 2f;
            float leftX = panelX + getRowButtonsLeft();
            float renameX = leftX;
            float deleteX = leftX + ROW_BTN_WIDTH + ROW_BTN_GAP;
            renderRowButton(context, mouseX, mouseY, renameX, btnY, "Rename", name + ":rename", colorTheme, alphaMul * hover);
            renderRowButton(context, mouseX, mouseY, deleteX, btnY, "Delete", name + ":delete", colorTheme, alphaMul * hover);
        }
    }

    private float getRowButtonsLeft() {
        return ClickGuiLayout.MODULE_PADDING + (ClickGuiLayout.MODULE_INNER_WIDTH - (2 * ROW_BTN_WIDTH + ROW_BTN_GAP)) / 2f;
    }

    private void renderRowButton(DrawContext context, int mouseX, int mouseY, float x, float y, String label, String hoverKey, int colorTheme, float alphaMul) {
        boolean hovered = HoveringUtils.isHovered(mouseX, mouseY, x, y, ROW_BTN_WIDTH, ROW_BTN_HEIGHT);
        AnimationUtils hoverAnim = getRowButtonHoverAnimation(hoverKey, hovered);
        hoverAnim.update(hovered ? 1f : 0f);
        float hover = hoverAnim.getValue();

        int bgColor = ColorUtils.applyAlpha(ClickGuiLayout.getSliderTrack(colorTheme), alphaMul * (0.55f + 0.45f * hover));
        int borderColor = ColorUtils.applyAlpha(ClickGuiLayout.getBorderLight(colorTheme), alphaMul * (0.4f + 0.6f * hover));
        RenderUtils.drawRoundedRect(context.getMatrices(), x - 0.5f, y - 0.5f, ROW_BTN_WIDTH + 1f, ROW_BTN_HEIGHT + 1f, 3.5f, borderColor);
        RenderUtils.drawRoundedRect(context.getMatrices(), x, y, ROW_BTN_WIDTH, ROW_BTN_HEIGHT, 3f, bgColor);

        int textColor = ColorUtils.applyAlpha(ClickGuiLayout.TEXT_PRIMARY, alphaMul * (0.8f + 0.2f * hover));
        issue(11).drawCenteredString(context.getMatrices(), label, x + ROW_BTN_WIDTH / 2f, y + 4.5f, textColor);
    }

    private void renderButtons(DrawContext context, int mouseX, int mouseY, float panelX, float buttonAreaTop, int colorTheme, float alphaMul) {
        float buttonWidth = (ClickGuiLayout.MODULE_INNER_WIDTH - BUTTON_GAP) / 2f;
        float leftX = panelX + ClickGuiLayout.MODULE_PADDING;
        float rightX = leftX + buttonWidth + BUTTON_GAP;
        float[][] positions = {
                {leftX, buttonAreaTop},
                {rightX, buttonAreaTop}
        };

        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            String label = BUTTON_LABELS[i];
            float bx = positions[i][0];
            float by = positions[i][1];
            boolean hovered = HoveringUtils.isHovered(mouseX, mouseY, bx, by, buttonWidth, BUTTON_HEIGHT);
            AnimationUtils hoverAnim = getButtonHoverAnimation(label, hovered);
            hoverAnim.update(hovered ? 1f : 0f);
            float hover = hoverAnim.getValue();

            int bgColor = ColorUtils.applyAlpha(ClickGuiLayout.getSliderTrack(colorTheme), alphaMul * (0.6f + 0.4f * hover));
            int borderColor = ColorUtils.applyAlpha(ClickGuiLayout.getBorderLight(colorTheme), alphaMul * (0.5f + 0.5f * hover));
            RenderUtils.drawRoundedRect(context.getMatrices(), bx - 0.5f, by - 0.5f, buttonWidth + 1f, BUTTON_HEIGHT + 1f, 4.5f, borderColor);
            RenderUtils.drawRoundedRect(context.getMatrices(), bx, by, buttonWidth, BUTTON_HEIGHT, 4f, bgColor);

            int textColor = ColorUtils.applyAlpha(ClickGuiLayout.TEXT_PRIMARY, alphaMul * (0.85f + 0.15f * hover));
            issue(13).drawCenteredString(context.getMatrices(), label, bx + buttonWidth / 2f, by + 7.2f, textColor);
        }
    }

    private void renderTextInput(DrawContext context, float panelX, float buttonAreaTop, int colorTheme, float alphaMul) {
        float overlayX = panelX + ClickGuiLayout.MODULE_PADDING;
        float overlayY = buttonAreaTop;
        float overlayW = ClickGuiLayout.MODULE_INNER_WIDTH;
        float overlayH = BUTTON_AREA_HEIGHT;

        RenderUtils.drawRoundedRect(context.getMatrices(), overlayX, overlayY, overlayW, overlayH, 4f, ColorUtils.applyAlpha(ClickGuiLayout.getSliderTrack(colorTheme), alphaMul * 0.95f));

        float fieldX = overlayX + 4f;
        float fieldY = overlayY + 4f;
        float fieldW = overlayW - 8f;
        float fieldH = overlayH - 8f;
        RenderUtils.drawRoundedRect(context.getMatrices(), fieldX, fieldY, fieldW, fieldH, 3f, ColorUtils.applyAlpha(colorTheme, alphaMul * 0.55f));

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(fieldX + 2f, fieldY + 1f, fieldW - 4f, fieldH - 2f);

        String displayText = textInputBuffer;
        int textCol = alpha(ClickGuiLayout.TEXT_WHITE, alphaMul);
        if (displayText.isEmpty()) {
            displayText = renaming ? "rename..." : "new config...";
            textCol = alpha(ClickGuiLayout.TEXT_SECONDARY, alphaMul);
        } else if ((System.currentTimeMillis() / 500L) % 2L == 0L) {
            displayText = displayText + "_";
        }
        issue(12).draw(context.getMatrices(), displayText, fieldX + 5f, fieldY + 6f, textCol);

        ScissorUtils.pop();
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

        if (textInputActive) {
            if (!onPanel) {
                cancelTextInput();
            }
            return onPanel;
        }

        if (!onPanel) {
            return false;
        }

        float buttonAreaTop = panelY + ClickGuiLayout.HEIGHT - BUTTON_AREA_HEIGHT - CONTENT_BOTTOM_PADDING;
        float buttonWidth = (ClickGuiLayout.MODULE_INNER_WIDTH - BUTTON_GAP) / 2f;
        float leftX = panelX + ClickGuiLayout.MODULE_PADDING;
        float rightX = leftX + buttonWidth + BUTTON_GAP;
        float[][] positions = {
                {leftX, buttonAreaTop},
                {rightX, buttonAreaTop}
        };

        for (int i = 0; i < BUTTON_LABELS.length; i++) {
            float bx = positions[i][0];
            float by = positions[i][1];
            if (HoveringUtils.isHovered(mouseX, mouseY, bx, by, buttonWidth, BUTTON_HEIGHT)) {
                handleButtonClick(BUTTON_LABELS[i]);
                return true;
            }
        }

        float listTop = panelY + CONTENT_TOP;
        float listHeight = buttonAreaTop - listTop - 2f;
        if (HoveringUtils.isHovered(mouseX, mouseY, panelX, listTop, ClickGuiLayout.WIDTH, listHeight)) {
            scrollAnimation.update(scrollTarget);
            float scroll = scrollAnimation.getValue();
            float rowY = listTop + scroll;
            for (String configName : cachedConfigs) {
                float h = getConfigHoverAnimation(configName, false).getValue();
                float rowHeight = CONFIG_ROW_HEIGHT + h * EXPANSION_EXTRA;
                if (HoveringUtils.isHovered(mouseX, mouseY, panelX + ClickGuiLayout.MODULE_PADDING, rowY, ClickGuiLayout.MODULE_INNER_WIDTH, rowHeight)) {
                    if (h > 0.5f) {
                        float expansion = h * EXPANSION_EXTRA;
                        float btnY = rowY + CONFIG_ROW_HEIGHT + (expansion - ROW_BTN_HEIGHT) / 2f;
                        float rowLeftX = panelX + getRowButtonsLeft();
                        float renameX = rowLeftX;
                        float deleteX = rowLeftX + ROW_BTN_WIDTH + ROW_BTN_GAP;
                        if (HoveringUtils.isHovered(mouseX, mouseY, renameX, btnY, ROW_BTN_WIDTH, ROW_BTN_HEIGHT)) {
                            startRename(configName);
                            return true;
                        }
                        if (HoveringUtils.isHovered(mouseX, mouseY, deleteX, btnY, ROW_BTN_WIDTH, ROW_BTN_HEIGHT)) {
                            deleteConfig(configName);
                            return true;
                        }
                    }
                    handleRowClick(configName);
                    return true;
                }
                rowY += rowHeight + CONFIG_ROW_GAP;
            }
        }

        return true;
    }

    private void handleRowClick(String configName) {
        long now = System.currentTimeMillis();
        if (lastClickConfig != null && lastClickConfig.equals(configName) && (now - lastClickTime) <= DOUBLE_CLICK_MS) {
            lastClickConfig = null;
            lastClickTime = 0L;
            loadConfig(configName);
        } else {
            lastClickConfig = configName;
            lastClickTime = now;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        if (getSlideProgress() <= 0.01f) {
            return false;
        }

        float panelX = getPanelX();
        float panelY = getPanelY();
        float listTop = panelY + CONTENT_TOP;
        float buttonAreaTop = panelY + ClickGuiLayout.HEIGHT - BUTTON_AREA_HEIGHT - CONTENT_BOTTOM_PADDING;
        float listHeight = buttonAreaTop - listTop - 2f;

        if (HoveringUtils.isHovered(mouseX, mouseY, panelX, listTop, ClickGuiLayout.WIDTH, listHeight)) {
            scrollTarget += (float) (verticalAmount * 20);
            clampScroll(listHeight);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int modifiers) {
        if (!textInputActive) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            cancelTextInput();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            confirmTextInput();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!textInputBuffer.isEmpty()) {
                textInputBuffer = textInputBuffer.substring(0, textInputBuffer.length() - 1);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            if (mc != null && mc.keyboard != null) {
                String clip = mc.keyboard.getClipboard();
                if (clip != null) {
                    for (char c : clip.toCharArray()) {
                        if (!Character.isISOControl(c) && textInputBuffer.length() < CONFIG_NAME_MAX_CHARS) {
                            textInputBuffer += c;
                        }
                    }
                }
            }
            return true;
        }
        return true;
    }

    public boolean charTyped(char chr) {
        if (!textInputActive) {
            return false;
        }
        if (Character.isISOControl(chr)) {
            return true;
        }
        if (textInputBuffer.length() < CONFIG_NAME_MAX_CHARS) {
            textInputBuffer += chr;
        }
        return true;
    }

    private void handleButtonClick(String label) {
        switch (label) {
            case "Add" -> {
                renaming = false;
                renamingTarget = null;
                textInputBuffer = "";
                textInputActive = true;
            }
            case "Folder" -> openConfigsFolder();
        }
    }

    private void startRename(String configName) {
        renaming = true;
        renamingTarget = configName;
        textInputBuffer = configName;
        textInputActive = true;
    }

    private void confirmTextInput() {
        String name = textInputBuffer.trim();
        name = name.replaceAll("[\\\\/:*?\"<>|]", "");
        if (name.isEmpty()) {
            cancelTextInput();
            return;
        }

        try {
            if (renaming) {
                String old = renamingTarget;
                if (old == null || old.isEmpty()) {
                    NotificationManager.pushCustom("No config to rename", HEADER_ICON);
                } else {
                    Popka.INSTANCE.configStorage.renameConfig(old, name);
                    NotificationManager.pushCustom("Config renamed: " + old + " -> " + name, HEADER_ICON);
                }
            } else {
                Popka.INSTANCE.configStorage.saveConfig(name);
                NotificationManager.pushCustom("Config saved: " + name, HEADER_ICON);
            }
        } catch (Exception e) {
            NotificationManager.pushCustom("Error: " + e.getMessage(), HEADER_ICON);
        }

        cancelTextInput();
        refreshConfigs();
    }

    private void cancelTextInput() {
        textInputActive = false;
        textInputBuffer = "";
        renaming = false;
        renamingTarget = null;
    }

    private void loadConfig(String name) {
        try {
            Popka.INSTANCE.configStorage.loadConfig(name);
            NotificationManager.pushCustom("Config loaded: " + name, HEADER_ICON);
        } catch (Exception e) {
            NotificationManager.pushCustom("Error loading: " + e.getMessage(), HEADER_ICON);
        }
    }

    private void deleteConfig(String name) {
        try {
            Popka.INSTANCE.configStorage.deleteConfig(name);
            NotificationManager.pushCustom("Config deleted: " + name, HEADER_ICON);
        } catch (Exception e) {
            NotificationManager.pushCustom("Error deleting: " + e.getMessage(), HEADER_ICON);
        }
        refreshConfigs();
    }

    private void openConfigsFolder() {
        try {
            File configsDir = Popka.INSTANCE.configsDir;
            if (configsDir == null) {
                NotificationManager.pushCustom("Configs dir not set", HEADER_ICON);
                return;
            }
            if (!configsDir.exists()) {
                configsDir.mkdirs();
            }
            new ProcessBuilder("explorer.exe", configsDir.getAbsolutePath()).start();
            NotificationManager.pushCustom("Configs folder opened", HEADER_ICON);
        } catch (Exception e) {
            NotificationManager.pushCustom("Error opening folder", HEADER_ICON);
        }
    }

    private void refreshConfigs() {
        long now = System.currentTimeMillis();
        if (now - lastConfigRefresh < 400L) {
            return;
        }
        lastConfigRefresh = now;

        File dir = Popka.INSTANCE.configsDir;
        if (dir == null || !dir.isDirectory()) {
            cachedConfigs = new ArrayList<>();
            return;
        }
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(CONFIG_EXTENSION));
        cachedConfigs = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                String n = f.getName();
                if (n.toLowerCase().endsWith(CONFIG_EXTENSION)) {
                    n = n.substring(0, n.length() - CONFIG_EXTENSION.length());
                }
                if (!n.isEmpty()) {
                    cachedConfigs.add(n);
                }
            }
        }
        String current = Popka.INSTANCE.configStorage.currentConfig;
        if (current != null && !current.isEmpty() && !cachedConfigs.contains(current)) {
            cachedConfigs.add(0, current);
        }
    }

    private void clampScroll(float listHeight) {
        float totalHeight = cachedConfigs.size() * (CONFIG_ROW_HEIGHT + CONFIG_ROW_GAP);
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

    private AnimationUtils getButtonHoverAnimation(String key, boolean hovered) {
        return buttonHoverAnimations.computeIfAbsent(key, k -> new AnimationUtils(hovered ? 1f : 0f, 9f, Easings.CUBIC_OUT));
    }

    private AnimationUtils getConfigHoverAnimation(String key, boolean hovered) {
        return configHoverAnimations.computeIfAbsent(key, k -> new AnimationUtils(hovered ? 1f : 0f, 9f, Easings.CUBIC_OUT));
    }

    private AnimationUtils getRowButtonHoverAnimation(String key, boolean hovered) {
        return rowButtonHoverAnimations.computeIfAbsent(key, k -> new AnimationUtils(hovered ? 1f : 0f, 9f, Easings.CUBIC_OUT));
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

package fun.popka.visuals.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import fun.popka.api.QClient;
import fun.popka.api.utils.animation.AnimationUtils;
import fun.popka.api.utils.animation.Easings;
import fun.popka.api.utils.client.ClientSoundPlayer;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.ui.clickgui.ClickGuiConfigPanel;
import fun.popka.visuals.ui.clickgui.ClickGuiInputHandler;
import fun.popka.visuals.ui.clickgui.ClickGuiRenderer;
import fun.popka.visuals.ui.clickgui.ClickGuiSettingRenderer;
import fun.popka.visuals.ui.clickgui.ClickGuiState;
import fun.popka.visuals.ui.clickgui.ClickGuiStyle;
import fun.popka.visuals.ui.clickgui.ClickGuiThemePanel;

public class MenuPanel extends Screen implements QClient {
    private static final ClickGuiState SHARED_STATE = new ClickGuiState();
    private final int categoryCount = Module.ModuleCategory.values().length;
    private final ClickGuiState state = SHARED_STATE;
    private final ClickGuiRenderer renderer = new ClickGuiRenderer(state, new ClickGuiSettingRenderer());
    private final ClickGuiInputHandler inputHandler = new ClickGuiInputHandler(state);
    private final ClickGuiConfigPanel configPanel = new ClickGuiConfigPanel(state);
    private final ClickGuiThemePanel themePanel = new ClickGuiThemePanel(state);
    private final AnimationUtils openAnimation = new AnimationUtils(0f, 7.5f, Easings.CUBIC_OUT);
    private boolean closing;
    private boolean closeSoundPlayed;

    public MenuPanel() {
        super(Text.of("ClickGui"));
        state.refreshModules();
    }

    private Window getWindow() {
        return mc == null ? null : mc.getWindow();
    }

    private void syncLayout() {
        Window window = getWindow();
        if (window != null) {
            state.updatePosition(window, categoryCount);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Window window = getWindow();
        if (window == null) {
            return;
        }

        updateAnimation();
        float progress = getAnimationProgress();
        if (closing && progress <= 0.001f) {
            if (mc != null) {
                mc.setScreen(null);
            }
            return;
        }

        state.updatePosition(window, categoryCount);
        state.setRenderOffsetY(getPanelOffsetY(progress));
        configPanel.update(closing);
        configPanel.render(context, mouseX, mouseY, window, progress);
        themePanel.update(closing);
        themePanel.render(context, mouseX, mouseY, window, progress);
        renderer.render(context, mouseX, mouseY, window, progress);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing) return true;
        syncLayout();
        state.setRenderOffsetY(getPanelOffsetY(getAnimationProgress()));
        if (configPanel.mouseClicked(mouseX, mouseY, button, getWindow())) {
            return true;
        }
        if (themePanel.mouseClicked(mouseX, mouseY, button, getWindow())) {
            return true;
        }
        return inputHandler.mouseClicked(mouseX, mouseY, button, getWindow())
                || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (closing) return true;
        syncLayout();
        return inputHandler.mouseReleased(button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (closing) return true;
        syncLayout();
        state.setRenderOffsetY(getPanelOffsetY(getAnimationProgress()));
        return inputHandler.mouseDragged(mouseX, mouseY, button)
                || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (closing) return true;
        syncLayout();
        state.setRenderOffsetY(getPanelOffsetY(getAnimationProgress()));
        if (configPanel.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        if (themePanel.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        return inputHandler.mouseScrolled(mouseX, mouseY, verticalAmount)
                || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (closing) return true;
        if (configPanel.isTextInputActive() && configPanel.keyPressed(keyCode, modifiers)) {
            return true;
        }
        if (inputHandler.keyPressed(keyCode, modifiers)) {
            return true;
        }
        if (canToggleStyle() && keyCode == GLFW.GLFW_KEY_R) {
            toggleStyle();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            startClosing();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean canToggleStyle() {
        return !configPanel.isTextInputActive()
                && !state.isSearchActive()
                && state.getBindingModule() == null
                && state.getBindingSetting() == null;
    }

    private void toggleStyle() {
        ClickGuiStyle next = state.getStyle() == ClickGuiStyle.DROPDOWN
                ? ClickGuiStyle.NEW
                : ClickGuiStyle.DROPDOWN;
        state.setStyle(next);
        Window window = getWindow();
        if (window != null) {
            state.updatePosition(window, categoryCount);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (closing) return true;
        if (configPanel.isTextInputActive() && configPanel.charTyped(chr)) {
            return true;
        }
        return inputHandler.charTyped(chr) || super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        startClosing();
    }

    @Override
    public void removed() {
        if (!closeSoundPlayed) {
            closeSoundPlayed = true;
            ClientSoundPlayer.playSound("closegui.wav", 0.6, 1.0f);
        }
        super.removed();
    }

    private void startClosing() {
        if (closing) {
            return;
        }

        closing = true;
        openAnimation.setEasing(Easings.CUBIC_IN);

        if (!closeSoundPlayed) {
            closeSoundPlayed = true;
            ClientSoundPlayer.playSound("closegui.wav", 0.6, 1.0f);
        }
    }

    private void updateAnimation() {
        if (closing) {
            openAnimation.update(0.0f);
        } else {
            openAnimation.setEasing(Easings.CUBIC_OUT);
            openAnimation.update(1.0f);
        }
    }

    private float getAnimationProgress() {
        return MathHelper.clamp(openAnimation.getValue(), 0.0f, 1.0f);
    }

    private float getPanelOffsetY(float progress) {
        return (1.0f - progress) * 22.0f;
    }
}

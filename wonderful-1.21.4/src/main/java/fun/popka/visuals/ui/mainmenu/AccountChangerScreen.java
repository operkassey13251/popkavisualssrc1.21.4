package fun.popka.visuals.ui.mainmenu;

import fun.popka.Popka;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.api.utils.scissor.ScissorUtils;
import fun.popka.mixin.IMinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AccountChangerScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 320;
    private static final int ROW_HEIGHT = 22;
    private static final int ROW_SPACING = 4;
    private static final int LIST_TOP_PADDING = 40;
    private static final int LIST_BOTTOM_PADDING = 70;

    private static final int COLOR_BG = ColorUtils.rgba(0, 0, 0, 200);
    private static final int COLOR_BORDER = ColorUtils.rgba(60, 60, 60, 255);
    private static final int COLOR_ROW = ColorUtils.rgba(20, 20, 20, 220);
    private static final int COLOR_ROW_HOVER = ColorUtils.rgba(40, 40, 40, 240);
    private static final int COLOR_TEXT_INACTIVE = ColorUtils.rgb(160, 160, 160);
    private static final int COLOR_TEXT_ACTIVE = ColorUtils.rgb(36, 218, 118);
    private static final int COLOR_X = ColorUtils.rgb(239, 72, 54);
    private static final int COLOR_X_HOVER = ColorUtils.rgb(255, 120, 100);
    private static final int X_BTN_SIZE = 14;
    private static final int COLOR_INPUT_BG = ColorUtils.rgba(35, 35, 35, 230);
    private static final int COLOR_INPUT_BORDER = ColorUtils.rgba(80, 80, 80, 255);
    private static final int COLOR_PLACEHOLDER = ColorUtils.rgb(100, 100, 100);

    private final Screen parent;
    private TextFieldWidget inputField;
    private float scrollOffset = 0f;

    private int lastClickedIndex = -1;
    private long lastClickTime = 0L;
    private static final long DOUBLE_CLICK_MS = 400L;

    public AccountChangerScreen(Screen parent) {
        super(Text.literal("Account Changer"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        Fonts.init();
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        int inputY = panelY + PANEL_HEIGHT - 50;
        int inputWidth = PANEL_WIDTH - 90;
        int inputHeight = 20;

        inputField = new TextFieldWidget(this.textRenderer, panelX + 10, inputY, inputWidth, inputHeight, Text.literal("Nickname"));
        inputField.setMaxLength(32);
        inputField.setPlaceholder(Text.literal(""));
        inputField.setDrawsBackground(false);
        this.addDrawableChild(inputField);

        int addBtnX = panelX + 10 + inputWidth + 10;
        PopkaMainMenuButton addBtn = new PopkaMainMenuButton(addBtnX, inputY, 60, inputHeight, Text.literal("Add"), b -> addNickname());
        this.addDrawableChild(addBtn);
    }

    private void addNickname() {
        if (inputField == null) return;
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) return;
        if (Popka.INSTANCE != null && Popka.INSTANCE.accountChangerStorage != null) {
            Popka.INSTANCE.accountChangerStorage.add(text);
        }
        inputField.setText("");
    }

    private List<String> getNicknames() {
        if (Popka.INSTANCE == null || Popka.INSTANCE.accountChangerStorage == null) return java.util.Collections.emptyList();
        return Popka.INSTANCE.accountChangerStorage.getNicknames();
    }

    private void applyNickname(String nickname) {
        if (this.client == null) return;
        Session current = this.client.getSession();
        if (current == null) return;
        try {
            Session newSession = new Session(
                    nickname,
                    current.getUuidOrNull(),
                    current.getAccessToken(),
                    current.getXuid(),
                    current.getClientId(),
                    current.getAccountType()
            );
            ((IMinecraftClientAccessor) this.client).setSession(newSession);
            if (Popka.INSTANCE != null && Popka.INSTANCE.accountChangerStorage != null) {
                Popka.INSTANCE.accountChangerStorage.setActive(nickname);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        PopkaMenuBackground.render(context, this.width, this.height, delta);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 8, COLOR_BORDER);
        RenderUtils.drawRoundedRect(context.getMatrices(), panelX + 1, panelY + 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2, 7, COLOR_BG);

        Font titleFont = Fonts.getFont("suisse", 18);
        if (titleFont != null) {
            String title = "Account Changer";
            titleFont.drawCenteredString(context.getMatrices(), title, panelX + PANEL_WIDTH / 2f, panelY + 14, 0xFFFFFFFF);
        }

        int listX = panelX + 10;
        int listY = panelY + LIST_TOP_PADDING;
        int listWidth = PANEL_WIDTH - 20;
        int listHeight = PANEL_HEIGHT - LIST_TOP_PADDING - LIST_BOTTOM_PADDING;

        RenderUtils.drawRoundedRect(context.getMatrices(), listX, listY, listWidth, listHeight, 4, ColorUtils.rgba(10, 10, 10, 200));

        List<String> nicks = getNicknames();
        Font rowFont = Fonts.getFont("suisse", 14);
        Font smallFont = Fonts.getFont("suisse", 12);

        float rowFull = ROW_HEIGHT + ROW_SPACING;
        float maxScroll = Math.max(0, nicks.size() * rowFull - listHeight);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(listX, listY, listWidth, listHeight);
        try {
            if (rowFont != null) {
                for (int i = 0; i < nicks.size(); i++) {
                    String nick = nicks.get(i);
                    float rowY = listY + i * rowFull - scrollOffset;
                    if (rowY + ROW_HEIGHT < listY) continue;
                    if (rowY > listY + listHeight) continue;

                    boolean isActive = Popka.INSTANCE != null
                            && Popka.INSTANCE.accountChangerStorage != null
                            && Popka.INSTANCE.accountChangerStorage.isActive(nick);
                    boolean rowHover = isMouseOver(mouseX, mouseY, listX + 2, rowY, listWidth - 4, ROW_HEIGHT);
                    float xBtnX = listX + listWidth - X_BTN_SIZE - 6;
                    float xBtnY = rowY + (ROW_HEIGHT - X_BTN_SIZE) / 2f;
                    boolean xHover = isMouseOver(mouseX, mouseY, xBtnX, xBtnY, X_BTN_SIZE, X_BTN_SIZE);

                    int rowBg = rowHover ? COLOR_ROW_HOVER : COLOR_ROW;
                    RenderUtils.drawRoundedRect(context.getMatrices(), listX + 2, rowY, listWidth - 4, ROW_HEIGHT, 3, rowBg);

                    if (isActive) {
                        RenderUtils.drawRoundedRect(context.getMatrices(), listX + 2, rowY, 3, ROW_HEIGHT, 1.5f, COLOR_TEXT_ACTIVE);
                    }

                    int textColor = isActive ? COLOR_TEXT_ACTIVE : COLOR_TEXT_INACTIVE;
                    rowFont.drawString(context.getMatrices(), nick, listX + 12, rowY + (ROW_HEIGHT - 14) / 2f + 5, textColor);

                    int xColor = xHover ? COLOR_X_HOVER : COLOR_X;
                    RenderUtils.drawRoundedRect(context.getMatrices(), xBtnX, xBtnY, X_BTN_SIZE, X_BTN_SIZE, 3, xColor);
                    if (smallFont != null) {
                        smallFont.drawCenteredString(context.getMatrices(), "X", xBtnX + X_BTN_SIZE / 2f, xBtnY + (X_BTN_SIZE - 12) / 2f + 5, 0xFFFFFFFF);
                    }
                }
            }

            if (nicks.isEmpty() && rowFont != null) {
                String empty = "No nicknames. Add one below.";
                rowFont.drawCenteredString(context.getMatrices(), empty, listX + listWidth / 2f, listY + listHeight / 2f - 7, ColorUtils.rgb(120, 120, 120));
            }
        } finally {
            ScissorUtils.unset();
            ScissorUtils.pop();
        }

        int inputX = panelX + 10;
        int inputY = panelY + PANEL_HEIGHT - 50;
        int inputWidth = PANEL_WIDTH - 90;
        int inputHeight = 20;

        RenderUtils.drawRoundedRect(context.getMatrices(), inputX - 2, inputY - 2, inputWidth + 4, inputHeight + 4, 5, COLOR_INPUT_BORDER);
        RenderUtils.drawRoundedRect(context.getMatrices(), inputX, inputY, inputWidth, inputHeight, 4, COLOR_INPUT_BG);

        super.render(context, mouseX, mouseY, delta);

        if (inputField != null && inputField.getText().isEmpty()) {
            Font placeholderFont = Fonts.getFont("suisse", 13);
            if (placeholderFont != null) {
                placeholderFont.drawString(context.getMatrices(), "Enter nickname...", inputX + 6, inputY + (inputHeight - 13) / 2f + 5, COLOR_PLACEHOLDER);
            }
        }

        Font hintFont = Fonts.getFont("suisse", 11);
        if (hintFont != null) {
            String hint = "Double-click to activate. ESC to close.";
            hintFont.drawCenteredString(context.getMatrices(), hint, panelX + PANEL_WIDTH / 2f, panelY + PANEL_HEIGHT - 24, ColorUtils.rgb(140, 140, 140));
        }
    }

    private boolean isMouseOver(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private int getRowAt(double mouseX, double mouseY) {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        int listX = panelX + 10;
        int listY = panelY + LIST_TOP_PADDING;
        int listWidth = PANEL_WIDTH - 20;
        int listHeight = PANEL_HEIGHT - LIST_TOP_PADDING - LIST_BOTTOM_PADDING;
        if (mouseX < listX || mouseX > listX + listWidth) return -1;
        if (mouseY < listY || mouseY > listY + listHeight) return -1;
        float rowFull = ROW_HEIGHT + ROW_SPACING;
        float relY = (float) (mouseY - listY) + scrollOffset;
        int index = (int) (relY / rowFull);
        if (index < 0 || index >= getNicknames().size()) return -1;
        float rowLocalY = relY - index * rowFull;
        if (rowLocalY > ROW_HEIGHT) return -1;
        return index;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int panelX = (this.width - PANEL_WIDTH) / 2;
            int panelY = (this.height - PANEL_HEIGHT) / 2;
            int listX = panelX + 10;
            int listY = panelY + LIST_TOP_PADDING;
            int listWidth = PANEL_WIDTH - 20;

            int index = getRowAt(mouseX, mouseY);
            if (index >= 0) {
                List<String> nicks = getNicknames();
                String nick = nicks.get(index);
                float rowFull = ROW_HEIGHT + ROW_SPACING;
                float rowY = listY + index * rowFull - scrollOffset;
                float xBtnX = listX + listWidth - X_BTN_SIZE - 6;
                float xBtnY = rowY + (ROW_HEIGHT - X_BTN_SIZE) / 2f;
                if (isMouseOver(mouseX, mouseY, xBtnX, xBtnY, X_BTN_SIZE, X_BTN_SIZE)) {
                    if (Popka.INSTANCE != null && Popka.INSTANCE.accountChangerStorage != null) {
                        Popka.INSTANCE.accountChangerStorage.remove(nick);
                    }
                    return true;
                }

                long now = System.currentTimeMillis();
                if (index == lastClickedIndex && (now - lastClickTime) < DOUBLE_CLICK_MS) {
                    applyNickname(nick);
                    lastClickedIndex = -1;
                    lastClickTime = 0L;
                } else {
                    lastClickedIndex = index;
                    lastClickTime = now;
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (float) verticalAmount * 20f;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        if (inputField != null && inputField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                addNickname();
                return true;
            }
            return inputField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (inputField != null) {
            return inputField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String text = inputField != null ? inputField.getText() : "";
        super.resize(client, width, height);
        if (inputField != null) inputField.setText(text);
    }
}

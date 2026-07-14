package fun.popka.visuals.ui.mainmenu;

import fun.popka.api.utils.client.MenuMusicPlayer;
import fun.popka.api.utils.render.fonts.ttf.Fonts;
import fun.popka.api.utils.render.fonts.ttf.MCFontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PopkaTitleScreen extends Screen {

    private static final String[] QUOTES = {
            "Интересный факт, мы придумали PopkaVisuals въебав 67 литров пива",
            "Пока не доказано, не ебёт, что сказано",
            "Одна ошибка и ты ошибся",
            "Студенткам художественного училища сюда @noviyprizmarin",
            "Владелец дебуды клиента - сын фермера"
    };
    private static final long QUOTE_INTERVAL_MS = 5000L;

    private final List<ButtonSpec> buttonSpecs = new ArrayList<>();

    private final MCFontRenderer titleFont;
    private final MCFontRenderer quoteFont;

    private int quoteIndex = 0;
    private long quoteStartTime = 0L;

    public PopkaTitleScreen() {
        super(Text.literal("PopkaVisuals"));
        Fonts.init();
        titleFont = Fonts.getSystemFont("Segoe UI", 42, Font.BOLD);
        quoteFont = Fonts.getSystemFont("Segoe UI", 16, Font.PLAIN);
    }

    @Override
    protected void init() {
        this.clearChildren();
        this.buttonSpecs.clear();

        MenuMusicPlayer.start();

        int buttonWidth = 200;
        int buttonHeight = 24;
        int buttonSpacing = 6;
        int totalButtons = 5;
        int totalHeight = totalButtons * buttonHeight + (totalButtons - 1) * buttonSpacing;
        int titleOffset = 40;
        int startY = (this.height - totalHeight) / 2 + titleOffset;
        int centerX = this.width / 2 - buttonWidth / 2;

        addButton(centerX, startY, buttonWidth, buttonHeight, Text.translatable("menu.singleplayer"), b -> this.client.setScreen(new SelectWorldScreen(this)));
        addButton(centerX, startY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight, Text.translatable("menu.multiplayer"), b -> this.client.setScreen(new MultiplayerScreen(this)));
        addButton(centerX, startY + 2 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, Text.translatable("menu.options"), b -> this.client.setScreen(new OptionsScreen(this, this.client.options)));
        addButton(centerX, startY + 3 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, Text.literal("Account Changer"), b -> this.client.setScreen(new AccountChangerScreen(this)));
        addButton(centerX, startY + 4 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, Text.translatable("menu.quit"), b -> this.client.scheduleStop());

        for (ButtonSpec spec : buttonSpecs) {
            PopkaMainMenuButton button = new PopkaMainMenuButton(spec.x, spec.y, spec.width, spec.height, spec.text, spec.onPress);
            this.addDrawableChild(button);
        }
    }

    private void addButton(int x, int y, int width, int height, Text text, net.minecraft.client.gui.widget.ButtonWidget.PressAction onPress) {
        buttonSpecs.add(new ButtonSpec(x, y, width, height, text, onPress));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        PopkaMenuBackground.render(context, this.width, this.height, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void removed() {
        MenuMusicPlayer.stop();
        super.removed();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        String title = "PopkaVisuals";
        float titleWidth = titleFont.getStringWidth(title);
        float titleX = (this.width - titleWidth) / 2f;
        float titleY = (this.height / 2f) - 90f;
        titleFont.drawStringWithShadow(title, titleX, titleY, 0xFFFFFFFF);

        renderQuote();
    }

    private void renderQuote() {
        long now = System.currentTimeMillis();
        if (quoteStartTime == 0L) quoteStartTime = now;

        long elapsed = now - quoteStartTime;
        if (elapsed >= QUOTE_INTERVAL_MS) {
            quoteIndex = (quoteIndex + 1) % QUOTES.length;
            quoteStartTime = now;
        }

        String quote = QUOTES[quoteIndex];
        float quoteWidth = quoteFont.getStringWidth(quote);
        float quoteX = (this.width - quoteWidth) / 2f;
        float quoteY = this.height - 28f;
        quoteFont.drawStringWithShadow(quote, quoteX, quoteY, 0xFFCCCCCC);
    }

    private record ButtonSpec(int x, int y, int width, int height, Text text, net.minecraft.client.gui.widget.ButtonWidget.PressAction onPress) {
    }
}

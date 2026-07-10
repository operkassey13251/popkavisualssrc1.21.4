package fun.popka.visuals.ui.mainmenu;

import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.ttf.Fonts;
import fun.popka.api.utils.render.fonts.ttf.MCFontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PopkaTitleScreen extends Screen {

    private static final Identifier BACKGROUND_GIF = Identifier.of("popka", "textures/gui/fon.gif");
    private static final Text TITLE_TEXT = Text.literal("PopkaVisuals");

    private final List<ButtonSpec> buttonSpecs = new ArrayList<>();
    private GifTexture backgroundTexture;

    private final MCFontRenderer titleFont;

    public PopkaTitleScreen() {
        super(Text.literal("PopkaVisuals"));
        Fonts.init();
        titleFont = Fonts.getSystemFont("Segoe UI", 42, Font.BOLD);
        backgroundTexture = new GifTexture(BACKGROUND_GIF);
    }

    @Override
    protected void init() {
        this.clearChildren();
        this.buttonSpecs.clear();

        int buttonWidth = 200;
        int buttonHeight = 24;
        int buttonSpacing = 6;
        int totalButtons = 4;
        int totalHeight = totalButtons * buttonHeight + (totalButtons - 1) * buttonSpacing;
        int titleOffset = 40;
        int startY = (this.height - totalHeight) / 2 + titleOffset;
        int centerX = this.width / 2 - buttonWidth / 2;

        addButton(centerX, startY, buttonWidth, buttonHeight, Text.translatable("menu.singleplayer"), b -> this.client.setScreen(new SelectWorldScreen(this)));
        addButton(centerX, startY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight, Text.translatable("menu.multiplayer"), b -> this.client.setScreen(new MultiplayerScreen(this)));
        addButton(centerX, startY + 2 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, Text.translatable("menu.options"), b -> this.client.setScreen(new OptionsScreen(this, this.client.options)));
        addButton(centerX, startY + 3 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, Text.translatable("menu.quit"), b -> this.client.scheduleStop());

        for (ButtonSpec spec : buttonSpecs) {
            PopkaMainMenuButton button = new PopkaMainMenuButton(spec.x, spec.y, spec.width, spec.height, spec.text, spec.onPress);
            this.addDrawableChild(button);
        }
    }

    private void addButton(int x, int y, int width, int height, Text text, ButtonWidget.PressAction onPress) {
        buttonSpecs.add(new ButtonSpec(x, y, width, height, text, onPress));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (backgroundTexture != null && backgroundTexture.isLoaded()) {
            backgroundTexture.update(delta);
            RenderUtils.drawImage(context.getMatrices(), backgroundTexture.getIdentifier(), 0, 0, this.width, this.height, 0xFFFFFFFF);
        } else {
            context.fill(0, 0, this.width, this.height, 0xFF000000);
        }
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
    }

    @Override
    public void removed() {
        if (backgroundTexture != null) {
            backgroundTexture.close();
            backgroundTexture = null;
        }
        super.removed();
    }

    private record ButtonSpec(int x, int y, int width, int height, Text text, ButtonWidget.PressAction onPress) {
    }
}

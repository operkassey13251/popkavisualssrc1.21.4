package fun.popka.visuals.ui.mainmenu;

import fun.popka.api.utils.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class PopkaMenuBackground {

    private static final Identifier BACKGROUND_GIF = Identifier.of("popka", "textures/gui/fon.gif");
    private static volatile GifTexture backgroundTexture;
    private static volatile boolean initialized = false;
    private static volatile boolean loadFailed = false;

    private PopkaMenuBackground() {
    }

    private static void init() {
        if (initialized) return;
        synchronized (PopkaMenuBackground.class) {
            if (initialized) return;
            initialized = true;
            try {
                if (MinecraftClient.getInstance().getResourceManager().getResource(BACKGROUND_GIF).isPresent()) {
                    backgroundTexture = new GifTexture(BACKGROUND_GIF);
                } else {
                    loadFailed = true;
                }
            } catch (Exception e) {
                loadFailed = true;
                e.printStackTrace();
            }
        }
    }

    public static void render(DrawContext context, int width, int height, float delta) {
        if (!initialized) init();
        if (backgroundTexture != null && backgroundTexture.isLoaded()) {
            backgroundTexture.update(delta);
            RenderUtils.drawImage(context.getMatrices(), backgroundTexture.getIdentifier(), 0, 0, width, height, 0xFFFFFFFF);
        } else {
            context.fill(0, 0, width, height, 0xFF000000);
        }
    }

    public static boolean isReady() {
        if (!initialized) init();
        return backgroundTexture != null && backgroundTexture.isLoaded();
    }
}

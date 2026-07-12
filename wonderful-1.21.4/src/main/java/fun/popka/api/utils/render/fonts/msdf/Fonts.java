package fun.popka.api.utils.render.fonts.msdf;

import net.minecraft.client.util.math.MatrixStack;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

public class Fonts {

    private static final HashMap<String, MsdfFont> loadedFonts = new HashMap<>();
    private static final HashMap<String, Font[]> fontCache = new HashMap<>();
    private static final HashMap<Integer, VanillaFont> vanillaFontCache = new HashMap<>();
    private static BooleanSupplier vanillaFontSupplier = () -> false;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        loadFont("sf_regular");
        loadFont("icon");
        loadFont("icon1");
        loadFont("iconnew");
        loadFont("suisse");
    }

    private static void loadFont(String name) {
        try {
            MsdfFont msdfFont = MsdfFont.builder().atlas(name).data(name).build();
            loadedFonts.put(name, msdfFont);

            Font[] fonts = new Font[100];
            for (int i = 8; i < 100; i++) {
                fonts[i] = new Font(msdfFont, i);
            }
            fontCache.put(name, fonts);
        } catch (Exception e) {
            System.err.println("[Fonts] Failed to load " + name + ": " + e.getMessage());
        }
    }

    public static Font getFont(String name, int size) {
        if (!initialized) init();

        String cleanName = name.replace(".ttf", "");

        if (size < 8) size = 8;
        if (size >= 100) size = 99;

        if (cleanName.equals("suisse") && vanillaFontSupplier.getAsBoolean()) {
            return getVanillaFont(size);
        }

        Font[] fonts = fontCache.get(cleanName);
        if (fonts != null && fonts[size] != null) {
            return fonts[size];
        }

        if (!loadedFonts.containsKey(cleanName)) {
            loadFont(cleanName);
        }

        fonts = fontCache.get(cleanName);
        if (fonts != null && fonts[size] != null) {
            return fonts[size];
        }

        return null;
    }

    private static Font getVanillaFont(int size) {
        VanillaFont cached = vanillaFontCache.get(size);
        if (cached != null) return cached;
        VanillaFont font = new VanillaFont(size);
        vanillaFontCache.put(size, font);
        return font;
    }

    public static void setVanillaFontSupplier(BooleanSupplier supplier) {
        vanillaFontSupplier = supplier;
    }

    public static void drawStringWithFade(Font font, String text, float x, float y, float maxWidth, int color) {
        if (font == null) return;
        MatrixStack stack = new MatrixStack();
        font.drawStringWithFade(stack, text, x, y, maxWidth, color);
    }
}
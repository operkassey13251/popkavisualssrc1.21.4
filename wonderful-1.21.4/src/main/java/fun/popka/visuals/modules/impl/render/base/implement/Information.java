package fun.popka.visuals.modules.impl.render.base.implement;

import fun.popka.Popka;
import fun.popka.api.events.implement.EventRender;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.draggable.Draggable;
import fun.popka.api.utils.math.MathUtils;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.visuals.modules.impl.render.base.InterfaceProcessing;

import net.minecraft.util.Identifier;

public class Information extends InterfaceProcessing {

    private static final Identifier INFO_ICON_TEXTURE = Identifier.of("popka", "textures/hud/info.png");
    private static final Identifier COORDS_ICON_TEXTURE = Identifier.of("popka", "textures/hud/coords.png");

    public Information(Draggable draggable) {
        super(draggable);
    }

    @Override
    public void onRender(EventRender.Default eventRender) {
        DefaultStyle(eventRender);
        super.onRender(eventRender);
    }

    public void DefaultStyle(EventRender.Default eventRender) {
        float x = draggable.getX(), y = draggable.getY();
        Font font = Fonts.getFont("suisse", 13);

        int colorTheme;
        if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            colorTheme = Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } else {
            colorTheme = ColorUtils.getThemeColor();
        }
        boolean drawSquares = isUnusualRectType();

        int px = (int)Math.floor(mc.player.getX());
        int py = (int)Math.floor(mc.player.getY());
        int pz = (int)Math.floor(mc.player.getZ());

        float height = 16f;
        double bps = MathUtils.calculateBPS();
        String xValue = String.valueOf(px);
        String yValue = String.valueOf(py);
        String zValue = String.valueOf(pz);
        String coordsText = xValue + "x " + yValue + "y " + zValue + "z";
        String bpsValue = formatTwoDecimals(bps);
        String bpsSuffix = " b/s";
        float widthbps = font.getWidth(bpsValue + bpsSuffix);
        float xbps = x + 17 + widthbps;
        float widthCords = font.getWidth(coordsText);
        float totalWidth = 13 + widthCords + widthbps + 2 + 13.8f;

        if (drawSquares) {
            RenderUtils.drawLiquidGlassPanel(eventRender.getContext().getMatrices(), x, y, totalWidth, height, 3f, 3.5f, colorTheme);
        } else {
            RenderUtils.drawDefaultHudThemedPanel(eventRender.getContext().getMatrices(), x, y, totalWidth, height, 3f, 3.5f, colorTheme);
            RenderUtils.drawShimmeringOutline(eventRender.getContext().getMatrices(), x, y, totalWidth, height, 3.5f, colorTheme, 1.0f);
        }

        float speedTextX = x + 13.5f;
        float bpsValueWidth = font.getWidth(bpsValue);
        font.draw(eventRender.getContext().getMatrices(), bpsValue, speedTextX, y + 6.6, -1);
        font.draw(eventRender.getContext().getMatrices(), bpsSuffix, speedTextX + bpsValueWidth - 2, y + 6.6, colorTheme);
        float coordsX = xbps + 9f;
        font.draw(eventRender.getContext().getMatrices(), xValue, coordsX, y + 6.6, -1);
        coordsX += font.getWidth(xValue);
        font.draw(eventRender.getContext().getMatrices(), "x", coordsX - 1, y + 6.6, colorTheme);
        coordsX += font.getWidth("x ");
        font.draw(eventRender.getContext().getMatrices(), yValue, coordsX, y + 6.6, -1);
        coordsX += font.getWidth(yValue);
        font.draw(eventRender.getContext().getMatrices(), "y", coordsX - 1, y + 6.6, colorTheme);
        coordsX += font.getWidth("y ");
        font.draw(eventRender.getContext().getMatrices(), zValue, coordsX, y + 6.6, -1);
        coordsX += font.getWidth(zValue);
        font.draw(eventRender.getContext().getMatrices(), "z", coordsX - 1, y + 6.6, colorTheme);
        RenderUtils.drawImage(eventRender.getContext().getMatrices(), INFO_ICON_TEXTURE, x + 3.25f, y + 4f, 8f, 8f, colorTheme);
        RenderUtils.drawImage(eventRender.getContext().getMatrices(), COORDS_ICON_TEXTURE, xbps - 1, y + 4f, 8f, 8f, colorTheme);


        draggable.setHeight(height);
        draggable.setWidth(totalWidth);
    }

    private String formatTwoDecimals(double value) {
        int scaled = (int) Math.round(value * 100.0D);
        int fraction = Math.abs(scaled % 100);
        return (scaled / 100) + "." + (fraction < 10 ? "0" : "") + fraction;
    }
}

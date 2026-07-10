package fun.popka.visuals.modules.impl.render.base.implement;

import net.minecraft.client.util.math.MatrixStack;
import fun.popka.Popka;
import fun.popka.api.events.implement.EventRender;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.draggable.Draggable;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.visuals.modules.impl.render.base.InterfaceProcessing;

import java.awt.*;

public class WaterMark extends InterfaceProcessing {
    private boolean showFps = true;
    private boolean showMs = true;
    private boolean showServer = true;
    private boolean showTps = true;

    public static String getUsername() {
        return "t.me/popkavisuals";
    }

    public static String getUID() {
        return "1";
    }

    public WaterMark(Draggable draggable) {
        super(draggable);
    }

    public boolean isShowFps() {
        return showFps;
    }

    public void setShowFps(boolean showFps) {
        this.showFps = showFps;
    }

    public boolean isShowMs() {
        return showMs;
    }

    public void setShowMs(boolean showMs) {
        this.showMs = showMs;
    }

    public boolean isShowServer() {
        return showServer;
    }

    public void setShowServer(boolean showServer) {
        this.showServer = showServer;
    }

    public boolean isShowTps() {
        return showTps;
    }

    public void setShowTps(boolean showTps) {
        this.showTps = showTps;
    }

    @Override
    public void onRender(EventRender.Default eventRender) {
        if (ModuleClass.interfaceModule.style.is("Wave")) WaveStyle(eventRender);
        else DefaultStyle(eventRender);
        super.onRender(eventRender);
    }

    public void DefaultStyle(EventRender.Default eventRender) {
        var matrices = eventRender.getContext().getMatrices();
        float x = draggable.getX();
        float y = draggable.getY();
        var iconNew14 = Fonts.getFont("iconnew", 14);
        var icon14 = Fonts.getFont("icon", 14);
        var statsIconFont = Fonts.getFont("popka", 14);
        if (statsIconFont == null) statsIconFont = iconNew14 != null ? iconNew14 : icon14;
        var suisse13 = Fonts.getFont("suisse", 13);

        int themeColor;
        if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            themeColor = Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } else {
            themeColor = ColorUtils.getThemeColor();
        }

        int whiteColor = new Color(255, 255, 255, 255).getRGB();
        int grayColor = ColorUtils.rgba(170, 170, 170, 255);

        float panelH = 18f;
        float pad = 4f;

        String username = "";
        if (mc != null && mc.player != null) {
            username = mc.player.getName().getString();
        }
        if (username.isEmpty()) username = getUsername();

        String serverIP = "Singleplayer";
        if (mc != null) {
            var info = mc.getCurrentServerEntry();
            if (info != null && info.address != null && !info.address.isEmpty()) {
                serverIP = info.address;
            }
        }

        int ping = 0;
        if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }
        String pingValue = String.valueOf(ping);
        String pingSuffix = "ms";
        String pingText = pingValue + pingSuffix;

        int fps = mc != null ? mc.getCurrentFps() : 0;
        String fpsValue = String.valueOf(fps);
        String fpsSuffix = "fps";
        String fpsText = fpsValue + fpsSuffix;

        String tpsValue = formatOneDecimal(getServerTps());
        String tpsSuffix = "tps";
        String tpsText = tpsValue + tpsSuffix;

        String userIconGlyph = "e";
        String serverIconGlyph = "n";
        String pingIconGlyph = "f";
        String fpsIconGlyph = "j";
        String tpsIconGlyph = "y";
        String glassIconGlyph = "g";

        float iconSquareSize = 14f;

        float contentW = pad;
        contentW += iconSquareSize + pad;

        float leftContentW = 0f;
        if (!username.isEmpty()) {
            leftContentW += iconNew14.getStringWidth(userIconGlyph) + 2f;
            leftContentW += suisse13.getStringWidth(username) + pad;
        }
        if (showServer) {
            leftContentW += iconNew14.getStringWidth(serverIconGlyph) + 2f;
            leftContentW += suisse13.getStringWidth(serverIP) + pad;
        }
        if (showFps) {
            leftContentW += statsIconFont != null ? statsIconFont.getStringWidth(fpsIconGlyph) + 2f : 0f;
            leftContentW += suisse13.getStringWidth(fpsText) + pad;
        }
        if (showTps) {
            leftContentW += statsIconFont != null ? statsIconFont.getStringWidth(tpsIconGlyph) + 2f : 0f;
            leftContentW += suisse13.getStringWidth(tpsText) + pad;
        }

        float rightContentW = 0f;
        if (showMs) {
            rightContentW += statsIconFont != null ? statsIconFont.getStringWidth(pingIconGlyph) + 2f : 0f;
            rightContentW += suisse13.getStringWidth(pingText) + pad;
        }

        float gap = 4f;
        contentW += leftContentW;
        if (showMs && leftContentW > 0f) contentW += gap;
        contentW += rightContentW;

        float panelW = contentW;

        RenderUtils.drawDefaultHudThemedPanel(matrices, x, y, panelW, panelH, 2.8f, 3.3f, themeColor);

        float iconSquareX = x + pad;
        float iconSquareY = y + (panelH - iconSquareSize) / 2f;
        RenderUtils.drawRoundedRect(matrices, iconSquareX, iconSquareY, iconSquareSize, iconSquareSize, 2f, themeColor);

        float glassIconW = iconNew14.getStringWidth(glassIconGlyph);
        float glassIconX = iconSquareX + (iconSquareSize - glassIconW) / 2f;
        float glassIconY = y - 8f;
        iconNew14.drawGradientStringHorizontal(matrices, glassIconGlyph, glassIconX, glassIconY, themeColor, themeColor);

        float drawX = x + pad + iconSquareSize + pad;
        float contentY = y + (panelH - 12f) / 2f + 4.6f;

        if (!username.isEmpty()) {
            iconNew14.drawGradientStringHorizontal(matrices, userIconGlyph, drawX, contentY, grayColor, grayColor);
            drawX += iconNew14.getStringWidth(userIconGlyph) + 2f;
            suisse13.drawString(matrices, username, drawX, contentY, whiteColor);
            drawX += suisse13.getStringWidth(username) + pad;
        }

        if (showServer) {
            iconNew14.drawGradientStringHorizontal(matrices, serverIconGlyph, drawX, contentY, grayColor, grayColor);
            drawX += iconNew14.getStringWidth(serverIconGlyph) + 2f;
            drawServerNameWithThemeParts(matrices, serverIP, drawX, contentY, themeColor, whiteColor);
            drawX += suisse13.getStringWidth(serverIP) + pad;
        }

        if (showFps) {
            if (statsIconFont != null) {
                statsIconFont.drawGradientStringHorizontal(matrices, fpsIconGlyph, drawX, contentY, grayColor, grayColor);
                drawX += statsIconFont.getStringWidth(fpsIconGlyph) + 2f;
            }
            suisse13.drawString(matrices, fpsValue, drawX, contentY, whiteColor);
            suisse13.drawString(matrices, fpsSuffix, drawX + suisse13.getStringWidth(fpsValue) - 1, contentY, themeColor);
            drawX += suisse13.getStringWidth(fpsText) + pad;
        }

        if (showTps) {
            if (statsIconFont != null) {
                statsIconFont.drawGradientStringHorizontal(matrices, tpsIconGlyph, drawX, contentY, grayColor, grayColor);
                drawX += statsIconFont.getStringWidth(tpsIconGlyph) + 2f;
            }
            suisse13.drawString(matrices, tpsValue, drawX, contentY, whiteColor);
            suisse13.drawString(matrices, tpsSuffix, drawX + suisse13.getStringWidth(tpsValue) - 1, contentY, themeColor);
            drawX += suisse13.getStringWidth(tpsText) + pad;
        }

        if (showMs) {
            float pingDrawX = x + panelW - pad;
            float pingTextW = suisse13.getStringWidth(pingText);
            pingDrawX -= pingTextW;
            suisse13.drawString(matrices, pingValue, pingDrawX, contentY, whiteColor);
            suisse13.drawString(matrices, pingSuffix, pingDrawX + suisse13.getStringWidth(pingValue) - 0.5f, contentY, themeColor);
            if (statsIconFont != null) {
                pingDrawX -= statsIconFont.getStringWidth(pingIconGlyph) + 2f;
                statsIconFont.drawGradientStringHorizontal(matrices, pingIconGlyph, pingDrawX, contentY, grayColor, grayColor);
            }
        }

        draggable.setWidth(panelW);
        draggable.setHeight(panelH);
    }

    public void WaveStyle(EventRender.Default eventRender) {
        float x = draggable.getX(), y = draggable.getY();
        var matrices = eventRender.getContext().getMatrices();
        var waveFont = Fonts.getFont("wave", 30);
        String watermarkText = "popka";

        int indexColor = ColorUtils.getThemeColor(90);
        int indexColor2 = ColorUtils.getThemeColor(180);
        int indexColor3 = ColorUtils.getThemeColor(270);
        int indexColor4 = ColorUtils.getColor(360);
        float glowWidth = 95.0f + waveFont.getStringWidth("ful");

        RenderUtils.drawShadow(matrices, x, y, glowWidth, 12, 10, 15, indexColor4, indexColor2, indexColor, indexColor3);
        waveFont.drawGradientStringHorizontal(matrices, watermarkText, x, y, indexColor, indexColor2);

        draggable.setWidth(Math.max(glowWidth, waveFont.getStringWidth(watermarkText)));
        draggable.setHeight(12);
    }

    private void drawServerNameWithThemeParts(MatrixStack matrices, String serverName, float x, float y, int themeColor, int whiteColor) {
        var font = Fonts.getFont("suisse", 13);
        String[] parts = serverName.split("\\.");
        if (parts.length < 2) {
            font.drawString(matrices, serverName, x, y, whiteColor);
            return;
        }

        String mainPart = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
        String suffixPart = "." + parts[parts.length - 1];

        font.drawString(matrices, mainPart, x, y, whiteColor);
        float suffixX = x + font.getStringWidth(mainPart) - 2f;
        font.drawString(matrices, suffixPart, suffixX, y, themeColor);
    }

    private String formatServerNameForDisplay(String serverName) {
        if (serverName == null || serverName.isEmpty()) {
            return "";
        }

        String host = serverName;
        int portIndex = host.indexOf(':');
        if (portIndex > 0) {
            host = host.substring(0, portIndex);
        }

        String[] parts = host.split("\\.");
        if (parts.length >= 3) {
            return String.join(".", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        }
        return host;
    }

    private float getServerTps() {
        if (Popka.INSTANCE == null || Popka.INSTANCE.tpsCalc == null) {
            return 20.0f;
        }
        return Math.max(0.0f, Math.min(20.0f, Popka.INSTANCE.tpsCalc.getTPS()));
    }

    private String formatOneDecimal(float value) {
        int scaled = Math.round(value * 10.0f);
        return (scaled / 10) + "." + Math.abs(scaled % 10);
    }
}

package fun.popka.visuals.ui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import fun.popka.Popka;
import fun.popka.api.QClient;
import fun.popka.api.storages.implement.ThemeStorage;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.api.utils.animation.AnimationUtils;
import fun.popka.api.utils.animation.Easings;
import fun.popka.api.utils.chat.ChatUtils;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.input.KeyBoardUtils;
import fun.popka.api.utils.math.HoveringUtils;
import fun.popka.api.utils.notification.NotificationManager;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.api.utils.scissor.ScissorUtils;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.Setting;
import fun.popka.visuals.modules.settings.implement.BindSetting;
import fun.popka.visuals.modules.settings.implement.BooleanSetting;
import fun.popka.visuals.modules.settings.implement.FloatSetting;
import fun.popka.visuals.modules.settings.implement.ListSetting;
import fun.popka.visuals.modules.settings.implement.ModeSetting;
import fun.popka.visuals.modules.settings.implement.TextSetting;

import java.util.*;

public class PivoScreen implements QClient {

    private static final float WIN_W = 330f;
    private static final float WIN_H = 270f;
    private static final float WIN_RADIUS = 6f;

    private static final float SIDE_W = 88f;
    private static final float ICON_H = 38f;
    private static final float CAT_ROW_H = 20f;
    private static final float CAT_GAP = 2f;
    private static final float MOD_ROW_H = 22f;
    private static final float MOD_GAP = 2f;

    private static final float TOGGLE_W = 26f;
    private static final float TOGGLE_H = 13f;
    private static final float TOGGLE_KNOB = 9f;

    private static final float MODE_ROW_H = 17f;
    private static final float LIST_ROW_H = 20f;

    private static final float THEME_ROW_H = 28f;
    private static final float THEME_GAP = 3f;

    private static final float SEARCH_H = 22f;
    private static final float SEARCH_MARGIN = 4f;
    private static final int SEARCH_MAX_LEN = 24;

    private static final String THEME_NAME = "Theme";
    private static final String THEME_ICON = "g";

    private static final int C_WIN_BG   = ColorUtils.rgba(8, 10, 14, 235);
    private static final int C_SIDE_BG  = ColorUtils.rgba(12, 14, 20, 250);
    private static final int C_CONT_BG  = ColorUtils.rgba(10, 12, 18, 250);
    private static final int C_CHILD    = ColorUtils.rgba(18, 20, 28, 200);
    private static final int C_SEP      = ColorUtils.rgba(30, 35, 50, 200);
    private static final int C_BLACK    = ColorUtils.rgba(0, 0, 0, 255);
    private static final int C_WHITE    = ColorUtils.rgba(255, 255, 255, 255);
    private static final int C_TEXT     = ColorUtils.rgb(230, 235, 245);
    private static final int C_TEXT_SEC = ColorUtils.rgb(120, 130, 155);
    private static final int C_TEXT_DIM = ColorUtils.rgb(85, 95, 120);
    private static final int C_BEER     = ColorUtils.rgb(245, 175, 60);
    private static final int C_BEER_DK  = ColorUtils.rgb(200, 130, 30);
    private static final int C_FOAM     = ColorUtils.rgb(255, 250, 230);
    private static final int C_SEL      = ColorUtils.rgba(25, 27, 36, 255);
    private static final int C_TOGGLE_OFF = ColorUtils.rgba(15, 15, 18, 240);
    private static final int DOT_GREEN  = ColorUtils.rgb(70, 200, 110);

    private static final Module.ModuleCategory[] CATS = {
            Module.ModuleCategory.COMBAT,
            Module.ModuleCategory.MOVEMENT,
            Module.ModuleCategory.RENDER,
            Module.ModuleCategory.MISC,
            Module.ModuleCategory.PLAYER
    };

    private float winX, winY;
    private int selCat = 0;
    private int selMod = 0;

    private final AnimationUtils[] catSelAnim = new AnimationUtils[CATS.length + 1];
    private final AnimationUtils[] catHovAnim = new AnimationUtils[CATS.length + 1];
    private final Map<Module, AnimationUtils> modHovAnim = new HashMap<>();
    private final Map<Module, AnimationUtils> modEnAnim = new HashMap<>();
    private final Map<Module, AnimationUtils> modExpandAnim = new HashMap<>();
    private final Map<Setting, AnimationUtils> toggleAnim = new HashMap<>();
    private final Map<ThemeStorage.Themes, AnimationUtils> themeHovAnim = new HashMap<>();

    private float contScrollTarget = 0f;
    private final AnimationUtils contScrollAnim = new AnimationUtils(0f, 8f, Easings.CUBIC_OUT);

    private Module bindingModule = null;
    private BindSetting bindingSetting = null;
    private TextSetting editingText = null;
    private String searchText = "";
    private boolean searching = false;
    private List<Module> allModules = new ArrayList<>();

    public PivoScreen() {
        for (int i = 0; i < catSelAnim.length; i++) {
            catSelAnim[i] = new AnimationUtils(i == 0 ? 1f : 0f, 12f, Easings.CUBIC_OUT);
            catHovAnim[i] = new AnimationUtils(0f, 10f, Easings.CUBIC_OUT);
        }
        refreshModules();
    }

    private void refreshModules() {
        allModules.clear();
        allModules.addAll(ModuleClass.INSTANCE.getObject().stream()
                .filter(m -> !"AutoBuy".equals(m.getName()) && !"AutoForest".equals(m.getName()))
                .toList());
    }

    private boolean isThemeCategory() {
        return selCat == CATS.length;
    }

    private boolean searchActive() {
        return searchText != null && !searchText.isEmpty();
    }

    private Module.ModuleCategory selectedCategory() {
        return CATS[selCat];
    }

    private List<Module> getModules() {
        if (searchActive()) {
            String q = searchText.toLowerCase(Locale.ROOT);
            return allModules.stream()
                    .filter(m -> m.getName().toLowerCase(Locale.ROOT).contains(q)
                            || m.getDisplayName().toLowerCase(Locale.ROOT).contains(q))
                    .toList();
        }
        return allModules.stream().filter(m -> m.getCategory() == selectedCategory()).toList();
    }

    public void render(DrawContext ctx, int mx, int my, Window window, float progress, boolean closing) {
        if (window == null) return;
        float alpha = MathHelper.clamp(progress, 0f, 1f);
        winX = (window.getScaledWidth() - WIN_W) / 2f;
        winY = (window.getScaledHeight() - WIN_H) / 2f;

        RenderUtils.drawRoundedRect(ctx.getMatrices(), winX, winY, WIN_W, WIN_H, WIN_RADIUS,
                ColorUtils.applyAlpha(C_WIN_BG, alpha));

        renderSidebar(ctx, mx, my, alpha);
        renderContent(ctx, mx, my, alpha);
    }

    private void renderSidebar(DrawContext ctx, int mx, int my, float alpha) {
        float sx = winX, sy = winY;

        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx, sy, SIDE_W, WIN_H, WIN_RADIUS, 0, 0, WIN_RADIUS,
                ColorUtils.applyAlpha(C_SIDE_BG, alpha));
        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + SIDE_W - 1f, sy + 4, 1f, WIN_H - 8, 0,
                ColorUtils.applyAlpha(C_SEP, alpha));

        renderBeerMug(ctx, sx + SIDE_W / 2f, sy + ICON_H / 2f + 4f, 16f, alpha);

        float ry = sy + ICON_H + 4f;
        int count = CATS.length + 1;
        for (int i = 0; i < count; i++) {
            boolean sel = (i == selCat);
            boolean hov = HoveringUtils.isHovered(mx, my, sx + 3, ry, SIDE_W - 6, CAT_ROW_H);
            catSelAnim[i].update(sel ? 1f : 0f);
            catHovAnim[i].update(hov ? 1f : 0f);
            float sv = catSelAnim[i].getValue();
            float hv = catHovAnim[i].getValue();

            if (sv > 0.01f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + 3, ry, (SIDE_W - 6) * sv, CAT_ROW_H, 3f,
                        ColorUtils.applyAlpha(accent(), (int)(alpha * sv * 180)));
            else if (hv > 0.01f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + 3, ry, SIDE_W - 6, CAT_ROW_H, 3f,
                        ColorUtils.applyAlpha(C_CHILD, (int)(alpha * hv * 255)));

            String icon = (i < CATS.length) ? CATS[i].getIcons() : THEME_ICON;
            String name = (i < CATS.length) ? CATS[i].getName() : THEME_NAME;
            int clr = sel ? ColorUtils.applyAlpha(C_BLACK, alpha)
                    : ColorUtils.applyAlpha(ColorUtils.interpolateColor(C_TEXT_DIM, C_TEXT_SEC, hv), alpha);
            iconFont(11).draw(ctx.getMatrices(), icon, sx + 8f, ry + (CAT_ROW_H - 11f) / 2f, clr);
            font(11).draw(ctx.getMatrices(), name, sx + 22f, ry + (CAT_ROW_H - 11f) / 2f, clr);
            ry += CAT_ROW_H + CAT_GAP;
        }

        renderSearch(ctx, mx, my, alpha);
    }

    private void renderSearch(DrawContext ctx, int mx, int my, float alpha) {
        float sx = winX, sy = winY;
        float boxX = sx + SEARCH_MARGIN;
        float boxY = sy + WIN_H - SEARCH_H - SEARCH_MARGIN;
        float boxW = SIDE_W - SEARCH_MARGIN * 2;

        boolean hov = HoveringUtils.isHovered(mx, my, boxX, boxY, boxW, SEARCH_H);
        int bg = searching ? C_CHILD : (hov ? C_CHILD : C_SEP);
        RenderUtils.drawRoundedRect(ctx.getMatrices(), boxX, boxY, boxW, SEARCH_H, 3f,
                ColorUtils.applyAlpha(bg, alpha));

        if (searching) {
            RenderUtils.drawRoundedRect(ctx.getMatrices(), boxX, boxY, 2f, SEARCH_H, 1f,
                    ColorUtils.applyAlpha(accent(), alpha));
        }

        String display = searchActive() ? searchText : "Search";
        boolean blink = searching && (System.currentTimeMillis() / 500 % 2 == 0);
        if (searching && blink) display += "_";
        int clr = searchActive() ? C_TEXT : C_TEXT_DIM;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(boxX + 5f, boxY, boxW - 10f, SEARCH_H);
        font(17).drawCenteredString(ctx.getMatrices(), display, boxX + boxW / 2f, boxY + (SEARCH_H - 17f * 0.5f) / 2f + 1.5f,
                ColorUtils.applyAlpha(clr, alpha));
        ScissorUtils.pop();
    }

    private float contentTopY() {
        return winY + 26f;
    }

    private float contentHeight() {
        return WIN_H - 26f;
    }

    private void renderContent(DrawContext ctx, int mx, int my, float alpha) {
        float cx = winX + SIDE_W, cy = winY;
        float cw = WIN_W - SIDE_W, ch = WIN_H;

        RenderUtils.drawRoundedRect(ctx.getMatrices(), cx, cy, cw, ch, 0, WIN_RADIUS, WIN_RADIUS, 0,
                ColorUtils.applyAlpha(C_CONT_BG, alpha));

        String header;
        if (searchActive()) {
            header = "Search / " + searchText;
        } else if (isThemeCategory()) {
            header = THEME_NAME;
        } else {
            List<Module> mods = getModules();
            String modName = (mods.isEmpty() || selMod < 0 || selMod >= mods.size()) ? "-" : mods.get(selMod).getName();
            header = selectedCategory().getName() + " / " + modName;
        }
        font(11).draw(ctx.getMatrices(), header, cx + 8, cy + 8, ColorUtils.applyAlpha(C_TEXT_SEC, alpha));
        RenderUtils.drawRoundedRect(ctx.getMatrices(), cx + 4, cy + 22f, cw - 8, 1f, 0,
                ColorUtils.applyAlpha(C_SEP, alpha));

        if (!searchActive() && isThemeCategory()) {
            renderThemeList(ctx, mx, my, alpha);
        } else {
            renderModuleGrid(ctx, mx, my, alpha);
        }
    }

    private void renderModuleGrid(DrawContext ctx, int mx, int my, float alpha) {
        float cx = winX + SIDE_W;
        float cw = WIN_W - SIDE_W;
        float contentY = contentTopY();
        float contentH = contentHeight();

        List<Module> mods = getModules();
        if (mods.isEmpty()) {
            font(12).draw(ctx.getMatrices(), "No modules", cx + 8, contentY + 12,
                    ColorUtils.applyAlpha(C_TEXT_DIM, alpha));
            return;
        }

        for (int i = 0; i < mods.size(); i++) {
            Module mod = mods.get(i);
            boolean sel = (i == selMod);
            AnimationUtils a = modExpandAnim.computeIfAbsent(mod,
                    k -> new AnimationUtils(sel ? 1f : 0f, 9f, Easings.CUBIC_OUT));
            a.update(sel ? 1f : 0f);
        }

        float colW = (cw - 12f) / 2f;
        float col0X = cx + 4f;
        float col1X = cx + 4f + colW + 4f;
        int mid = (mods.size() + 1) / 2;

        contScrollAnim.update(contScrollTarget);
        float scroll = contScrollAnim.getValue();
        float col0H = colHeight(mods, 0, mid);
        float col1H = colHeight(mods, mid, mods.size());
        float maxH = Math.max(col0H, col1H);
        float maxScroll = Math.min(0f, contentH - maxH);
        if (contScrollTarget < maxScroll) contScrollTarget = maxScroll;
        if (contScrollTarget > 0f) contScrollTarget = 0f;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(cx, contentY, cw, contentH);

        renderColumn(ctx, mx, my, col0X, contentY + scroll, colW, mods, 0, mid, alpha);
        renderColumn(ctx, mx, my, col1X, contentY + scroll, colW, mods, mid, mods.size(), alpha);

        ScissorUtils.pop();
    }

    private float settingsFullHeight(Module mod) {
        List<Setting> all = mod.getSettings();
        if (all == null) return 0f;
        List<Setting> vis = all.stream().filter(s -> s != null && s.visible()).toList();
        if (vis.isEmpty()) return 16f;
        float h = 0f;
        for (Setting s : vis) h += getRowH(s) + 3f;
        return h;
    }

    private float expandValue(Module mod) {
        AnimationUtils a = modExpandAnim.get(mod);
        return a == null ? 0f : a.getValue();
    }

    private float colHeight(List<Module> mods, int start, int end) {
        float h = 0f;
        for (int i = start; i < end; i++) {
            Module mod = mods.get(i);
            h += MOD_ROW_H + MOD_GAP + settingsFullHeight(mod) * expandValue(mod);
        }
        return h;
    }

    private void renderColumn(DrawContext ctx, int mx, int my, float colX, float startY, float colW,
                              List<Module> mods, int start, int end, float alpha) {
        float ry = startY;
        for (int i = start; i < end; i++) {
            Module mod = mods.get(i);
            boolean sel = (i == selMod);
            float ev = expandValue(mod);
            float setH = settingsFullHeight(mod);
            float clipH = setH * ev;

            boolean hov = HoveringUtils.isHovered(mx, my, colX, ry, colW, MOD_ROW_H);
            boolean ena = mod.isEnable();

            AnimationUtils ha = modHovAnim.computeIfAbsent(mod, k -> new AnimationUtils(0f, 9f, Easings.CUBIC_OUT));
            AnimationUtils ea = modEnAnim.computeIfAbsent(mod, k -> new AnimationUtils(ena ? 1f : 0f, 8f, Easings.LINEAR));
            ha.update(hov || sel ? 1f : 0f);
            ea.update(ena ? 1f : 0f);
            float hv = ha.getValue();
            float enV = ea.getValue();

            if (sel)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), colX, ry, colW, MOD_ROW_H, 3f,
                        ColorUtils.applyAlpha(C_SEL, alpha));
            else if (hv > 0.01f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), colX, ry, colW, MOD_ROW_H, 3f,
                        ColorUtils.applyAlpha(C_CHILD, (int)(alpha * hv * 255)));

            if (enV > 0.01f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), colX, ry + 4, 2f, MOD_ROW_H - 8, 1f,
                        ColorUtils.applyAlpha(accent(), (int)(alpha * enV * 255)));

            int nameClr = ColorUtils.applyAlpha(ena ? C_TEXT : hov ? C_TEXT_SEC : C_TEXT_DIM, alpha);
            font(11).draw(ctx.getMatrices(), mod.getName(), colX + 10f, ry + (MOD_ROW_H - 11f) / 2f, nameClr);

            ry += MOD_ROW_H + MOD_GAP;

            if (ev > 0.01f && setH > 0f) {
                float setAlpha = alpha * ev;
                ScissorUtils.push();
                ScissorUtils.setFromComponentCoordinates(colX, ry, colW, clipH);
                renderModuleSettings(ctx, mx, my, colX, ry, colW, mod, setAlpha);
                ScissorUtils.pop();
            }

            ry += clipH;
        }
    }

    private void renderModuleSettings(DrawContext ctx, int mx, int my, float colX, float ry, float colW,
                                      Module mod, float alpha) {
        List<Setting> all = mod.getSettings();
        if (all == null) return;
        List<Setting> vis = all.stream().filter(s -> s != null && s.visible()).toList();
        if (vis.isEmpty()) {
            font(11).draw(ctx.getMatrices(), "No settings", colX + 10f, ry + 2f,
                    ColorUtils.applyAlpha(C_TEXT_DIM, alpha));
            return;
        }
        float sy = ry;
        for (Setting s : vis) {
            float sh = getRowH(s);
            renderRow(ctx, mx, my, colX, sy, colW, s, alpha);
            sy += sh + 3f;
        }
    }

    private void renderThemeList(DrawContext ctx, int mx, int my, float alpha) {
        float cx = winX + SIDE_W;
        float cw = WIN_W - SIDE_W;
        float contentY = contentTopY();
        float contentH = contentHeight();

        List<ThemeStorage.Themes> themes = Popka.INSTANCE.themeStorage.getThemeList();
        ThemeStorage.Themes current = Popka.INSTANCE.themeStorage.getThemes();

        contScrollAnim.update(contScrollTarget);
        float scroll = contScrollAnim.getValue();
        float totalH = themes.size() * (THEME_ROW_H + THEME_GAP);
        float maxScroll = Math.min(0f, contentH - totalH);
        if (contScrollTarget < maxScroll) contScrollTarget = maxScroll;
        if (contScrollTarget > 0f) contScrollTarget = 0f;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(cx, contentY, cw, contentH);

        float ry = contentY + scroll;
        for (ThemeStorage.Themes theme : themes) {
            boolean isCurrent = (theme == current);
            boolean hov = HoveringUtils.isHovered(mx, my, cx + 4, ry, cw - 8, THEME_ROW_H);
            AnimationUtils anim = themeHovAnim.computeIfAbsent(theme,
                    k -> new AnimationUtils(0f, 9f, Easings.CUBIC_OUT));
            anim.update(hov ? 1f : 0f);
            float hv = anim.getValue();

            if (isCurrent)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), cx + 4, ry, cw - 8, THEME_ROW_H, 3f,
                        ColorUtils.applyAlpha(C_SEL, alpha));
            else if (hv > 0.01f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), cx + 4, ry, cw - 8, THEME_ROW_H, 3f,
                        ColorUtils.applyAlpha(C_CHILD, (int)(alpha * hv * 255)));

            int swatchCol;
            try {
                if (!theme.getTheme().getName().equals("Rainbow"))
                    swatchCol = theme.getTheme().color[0];
                else
                    swatchCol = ColorUtils.getThemeColor();
            } catch (Exception e) {
                swatchCol = accent();
            }
            RenderUtils.drawRoundCircle(ctx.getMatrices(), cx + 18f, ry + THEME_ROW_H / 2f, 11f,
                    ColorUtils.applyAlpha(swatchCol, alpha));
            RenderUtils.drawRoundCircle(ctx.getMatrices(), cx + 18f, ry + THEME_ROW_H / 2f, 7f,
                    ColorUtils.applyAlpha(C_WIN_BG, alpha * 0.5f));

            String displayName = theme.getTheme().getName();
            int nameClr = ColorUtils.applyAlpha(isCurrent ? C_TEXT : hov ? C_TEXT_SEC : C_TEXT_DIM, alpha);
            font(13).draw(ctx.getMatrices(), displayName, cx + 34f, ry + (THEME_ROW_H - 13f) / 2f, nameClr);

            if (isCurrent)
                RenderUtils.drawRoundCircle(ctx.getMatrices(), cx + cw - 16f, ry + THEME_ROW_H / 2f, 6f,
                        ColorUtils.applyAlpha(DOT_GREEN, alpha));

            ry += THEME_ROW_H + THEME_GAP;
        }

        ScissorUtils.pop();
    }

    private void renderRow(DrawContext ctx, int mx, int my, float rx, float ry, float rw, Setting s, float alpha) {
        float sh = getRowH(s);
        float lx = rx + 8f, rightX = rx + rw - 8f;

        if (s instanceof BooleanSetting bs) {
            boolean hov = HoveringUtils.isHovered(mx, my, rx, ry, rw, sh);
            font(12).draw(ctx.getMatrices(), s.name(), lx, ry + (sh - 12f) / 2f,
                    ColorUtils.applyAlpha(bs.isState() ? C_TEXT : hov ? C_TEXT_SEC : C_TEXT_DIM, alpha));
            float tx = rightX - TOGGLE_W;
            float ty = ry + (sh - TOGGLE_H) / 2f;
            renderToggleSwitch(ctx, tx, ty, bs.isState(), s, alpha);
        } else if (s instanceof FloatSetting fs) {
            font(12).draw(ctx.getMatrices(), s.name(), lx, ry + 5f,
                    ColorUtils.applyAlpha(C_TEXT_SEC, alpha));
            String val = String.format(Locale.ROOT, fs.getIncrement() < 1f ? "%.1f" : "%.0f", fs.get());
            font(11).draw(ctx.getMatrices(), val, rightX - font(11).getWidth(val), ry + 5f,
                    ColorUtils.applyAlpha(accent(), alpha));
            float sliderY = ry + sh - 11f;
            float sliderW = rw - 16f;
            float sliderH = 3f;
            float pos = (fs.getMax() == fs.getMin()) ? 0
                    : (fs.get() - fs.getMin()) / (fs.getMax() - fs.getMin());
            RenderUtils.drawRoundedRect(ctx.getMatrices(), lx, sliderY, sliderW, sliderH, sliderH / 2,
                    ColorUtils.applyAlpha(C_BLACK, alpha));
            if (pos > 0.001f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), lx, sliderY, sliderW * pos, sliderH, sliderH / 2,
                        ColorUtils.applyAlpha(accent(), alpha));
            RenderUtils.drawRoundCircle(ctx.getMatrices(), lx + sliderW * pos, sliderY + sliderH / 2f, 5f,
                    ColorUtils.applyAlpha(C_WHITE, alpha));
        } else if (s instanceof ModeSetting ms) {
            font(12).draw(ctx.getMatrices(), s.name(), lx, ry + 5f,
                    ColorUtils.applyAlpha(C_TEXT_SEC, alpha));
            String cur = ms.getCurrent();
            font(11).draw(ctx.getMatrices(), cur, rightX - font(11).getWidth(cur), ry + 5f,
                    ColorUtils.applyAlpha(accent(), alpha));
            float oy = ry + 20f;
            for (String mode : ms.getMods()) {
                boolean sel = mode.equals(ms.getCurrent());
                boolean hov = HoveringUtils.isHovered(mx, my, rx, oy, rw, MODE_ROW_H);
                float cX = lx + 4f, cY = oy + 8f;
                RenderUtils.drawRoundCircle(ctx.getMatrices(), cX, cY, 7f,
                        ColorUtils.applyAlpha(sel ? accent() : C_SEP, alpha));
                if (sel)
                    RenderUtils.drawRoundCircle(ctx.getMatrices(), cX, cY, 4f, ColorUtils.applyAlpha(C_WHITE, alpha));
                int clr = ColorUtils.applyAlpha(sel ? accent() : hov ? C_TEXT_SEC : C_TEXT_DIM, alpha);
                font(12).draw(ctx.getMatrices(), mode, lx + 14f, oy + 2f, clr);
                oy += MODE_ROW_H;
            }
        } else if (s instanceof BindSetting bind) {
            font(12).draw(ctx.getMatrices(), s.name(), lx, ry + (sh - 12f) / 2f,
                    ColorUtils.applyAlpha(C_TEXT_DIM, alpha));
            String bStr = (bindingSetting == bind || bind.getKey() == -1) ? "..." : KeyBoardUtils.getBindName(bind.getKey());
            float bw = font(11).getWidth(bStr) + 10f;
            float bx = rightX - bw;
            RenderUtils.drawRoundedRect(ctx.getMatrices(), bx, ry + 4f, bw, sh - 8f, 2f,
                    ColorUtils.applyAlpha(C_WHITE, alpha));
            font(11).draw(ctx.getMatrices(), bStr, bx + 5f, ry + (sh - 11f) / 2f, ColorUtils.applyAlpha(C_BLACK, alpha));
        } else if (s instanceof TextSetting ts) {
            font(12).draw(ctx.getMatrices(), s.name(), lx, ry + (sh - 12f) / 2f,
                    ColorUtils.applyAlpha(C_TEXT_DIM, alpha));
            boolean editing = editingText == ts;
            String tv = ts.get() == null || ts.get().isEmpty() ? "" : ts.get();
            String boxText = editing ? tv + "_" : (tv.isEmpty() ? "..." : tv);
            float bw = font(11).getWidth(boxText) + 10f;
            float bx = rightX - bw;
            RenderUtils.drawRoundedRect(ctx.getMatrices(), bx, ry + 4f, bw, sh - 8f, 2f,
                    ColorUtils.applyAlpha(editing ? C_WHITE : C_CHILD, alpha));
            font(11).draw(ctx.getMatrices(), boxText, bx + 5f, ry + (sh - 11f) / 2f,
                    ColorUtils.applyAlpha(editing ? C_BLACK : C_TEXT_SEC, alpha));
        } else if (s instanceof ListSetting ls) {
            font(12).draw(ctx.getMatrices(), s.name(), lx, ry + 5f,
                    ColorUtils.applyAlpha(C_TEXT_SEC, alpha));
            float oy = ry + 20f;
            for (BooleanSetting entry : ls.getSettings()) {
                if (!entry.visible()) { oy += LIST_ROW_H; continue; }
                boolean hov = HoveringUtils.isHovered(mx, my, rx, oy, rw, LIST_ROW_H);
                font(12).draw(ctx.getMatrices(), entry.name(), lx + 14f, oy + 4f,
                        ColorUtils.applyAlpha(entry.isState() ? C_TEXT : hov ? C_TEXT_SEC : C_TEXT_DIM, alpha));
                float tx = rightX - TOGGLE_W;
                float ty = oy + (LIST_ROW_H - TOGGLE_H) / 2f;
                renderToggleSwitch(ctx, tx, ty, entry.isState(), entry, alpha);
                oy += LIST_ROW_H;
            }
        }
    }

    private void renderToggleSwitch(DrawContext ctx, float tx, float ty, boolean on, Setting key, float alpha) {
        AnimationUtils anim = toggleAnim.computeIfAbsent(key,
                k -> new AnimationUtils(on ? 1f : 0f, 10f, Easings.BACK_OUT));
        anim.update(on ? 1f : 0f);
        float t = anim.getValue();

        int offCol = ColorUtils.applyAlpha(C_TOGGLE_OFF, alpha);
        int onCol = ColorUtils.applyAlpha(accent(), alpha);
        int bgColor = ColorUtils.interpolateColor(offCol, onCol, t);

        if (t > 0.01f) {
            RenderUtils.drawRoundedRect(ctx.getMatrices(), tx - 1f, ty - 1f, TOGGLE_W + 2f, TOGGLE_H + 2f, TOGGLE_H / 2f + 1f,
                    ColorUtils.applyAlpha(accent(), (int)(alpha * t * 60)));
        }

        RenderUtils.drawRoundedRect(ctx.getMatrices(), tx, ty, TOGGLE_W, TOGGLE_H, TOGGLE_H / 2f, bgColor);

        float knobR = TOGGLE_KNOB;
        float knobX = tx + 2f + t * (TOGGLE_W - TOGGLE_KNOB - 2f) + knobR / 2f;
        float knobY = ty + TOGGLE_H / 2f;
        RenderUtils.drawRoundCircle(ctx.getMatrices(), knobX, knobY, knobR, ColorUtils.applyAlpha(C_WHITE, alpha));
    }

    private void renderBeerMug(DrawContext ctx, float cx, float cy, float scale, float alpha) {
        float w = scale * 1.2f;
        float h = scale * 1.4f;
        float foamH = h * 0.32f;
        float bodyTop = cy - h / 2f + foamH;
        float bodyBot = cy + h / 2f;
        float left = cx - w / 2f;
        float right = cx + w / 2f;
        float handleW = w * 0.3f;
        float handleH = h * 0.45f;

        RenderUtils.drawRoundedRect(ctx.getMatrices(),
                right - 0.5f, cy - handleH / 2f, handleW, handleH, handleW / 2f,
                ColorUtils.applyAlpha(C_BEER_DK, alpha));
        RenderUtils.drawRoundedRect(ctx.getMatrices(),
                right + 1f, cy - handleH / 2f + 2f, handleW - 3f, handleH - 4f, (handleW - 3f) / 2f,
                ColorUtils.applyAlpha(C_SIDE_BG, alpha));

        RenderUtils.drawRoundedRect(ctx.getMatrices(), left, bodyTop, w, bodyBot - bodyTop, 2f,
                ColorUtils.applyAlpha(C_BEER, alpha));

        RenderUtils.drawRoundedRect(ctx.getMatrices(), left, cy - h / 2f, w, foamH, 2f,
                ColorUtils.applyAlpha(C_FOAM, alpha));
        RenderUtils.drawRoundedRect(ctx.getMatrices(), left, cy - h / 2f + foamH - 1f, w, 2f, 0,
                ColorUtils.applyAlpha(C_FOAM, alpha));

        RenderUtils.drawRoundedRect(ctx.getMatrices(), left, bodyBot - 1.5f, w, 1.5f, 0,
                ColorUtils.applyAlpha(C_BEER_DK, alpha));
    }

    private float getRowH(Setting s) {
        if (s instanceof FloatSetting) return 32f;
        if (s instanceof BooleanSetting) return 24f;
        if (s instanceof ModeSetting ms) return 22f + ms.getMods().size() * MODE_ROW_H;
        if (s instanceof ListSetting ls) {
            int vis = 0;
            for (BooleanSetting e : ls.getSettings()) if (e.visible()) vis++;
            return 22f + vis * LIST_ROW_H;
        }
        return 24f;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (mc == null) return false;
        editingText = null;
        searching = false;

        float sx = winX, sy = winY;
        float sBoxX = sx + SEARCH_MARGIN;
        float sBoxY = sy + WIN_H - SEARCH_H - SEARCH_MARGIN;
        float sBoxW = SIDE_W - SEARCH_MARGIN * 2;
        if (button == 0 && HoveringUtils.isHovered(mx, my, sBoxX, sBoxY, sBoxW, SEARCH_H)) {
            searching = true;
            return true;
        }

        float ry = sy + ICON_H + 4f;
        int count = CATS.length + 1;
        for (int i = 0; i < count; i++) {
            if (HoveringUtils.isHovered(mx, my, sx + 3, ry, SIDE_W - 6, CAT_ROW_H) && button == 0) {
                selCat = i; selMod = 0;
                contScrollTarget = 0;
                searchText = "";
                return true;
            }
            ry += CAT_ROW_H + CAT_GAP;
        }

        if (bindingModule != null && button >= 2) {
            bindingModule.setKey(KeyBoardUtils.createMouseBind(button));
            bindingModule = null;
            return true;
        }

        if (!searchActive() && isThemeCategory()) {
            return handleThemeClick(mx, my, button);
        }

        return handleModuleGridClick(mx, my, button);
    }

    private boolean handleThemeClick(double mx, double my, int button) {
        if (button != 0) return false;
        float cx = winX + SIDE_W;
        float cw = WIN_W - SIDE_W;
        float contentY = contentTopY();

        List<ThemeStorage.Themes> themes = Popka.INSTANCE.themeStorage.getThemeList();
        contScrollAnim.update(contScrollTarget);
        float scroll = contScrollAnim.getValue();
        float ry = contentY + scroll;
        for (ThemeStorage.Themes theme : themes) {
            if (HoveringUtils.isHovered(mx, my, cx + 4, ry, cw - 8, THEME_ROW_H)) {
                applyTheme(theme);
                return true;
            }
            ry += THEME_ROW_H + THEME_GAP;
        }
        return false;
    }

    private void applyTheme(ThemeStorage.Themes theme) {
        try {
            Popka.INSTANCE.themeStorage.setThemes(theme);
            NotificationManager.pushCustom("Theme applied: " + theme.name(), THEME_ICON);
            ChatUtils.sendMessage("Тема " + theme.name() + " успешно применена!");
        } catch (Exception e) {
            NotificationManager.pushCustom("Error: " + e.getMessage(), THEME_ICON);
            ChatUtils.sendMessage("Ошибка при применении темы " + theme.name() + "!");
        }
    }

    private boolean handleModuleGridClick(double mx, double my, int button) {
        float cx = winX + SIDE_W;
        float cw = WIN_W - SIDE_W;
        float contentY = contentTopY();

        List<Module> mods = getModules();
        if (mods.isEmpty()) return false;

        float colW = (cw - 12f) / 2f;
        float col0X = cx + 4f;
        float col1X = cx + 4f + colW + 4f;
        int mid = (mods.size() + 1) / 2;

        contScrollAnim.update(contScrollTarget);
        float scroll = contScrollAnim.getValue();

        if (clickColumn(mx, my, button, col0X, contentY + scroll, colW, mods, 0, mid)) return true;
        if (clickColumn(mx, my, button, col1X, contentY + scroll, colW, mods, mid, mods.size())) return true;
        return false;
    }

    private boolean clickColumn(double mx, double my, int button, float colX, float startY, float colW,
                                List<Module> mods, int start, int end) {
        float ry = startY;
        for (int i = start; i < end; i++) {
            Module mod = mods.get(i);
            float ev = expandValue(mod);
            float setH = settingsFullHeight(mod);
            float clipH = setH * ev;

            if (HoveringUtils.isHovered(mx, my, colX, ry, colW, MOD_ROW_H)) {
                if (button == 0) { mod.toggle(); return true; }
                if (button == 1) { selMod = (selMod == i) ? -1 : i; return true; }
                if (button == 2) { bindingModule = mod; return true; }
            }
            ry += MOD_ROW_H + MOD_GAP;

            if (ev > 0.5f && setH > 0f) {
                List<Setting> all = mod.getSettings();
                if (all != null) {
                    List<Setting> vis = all.stream().filter(s -> s != null && s.visible()).toList();
                    if (!vis.isEmpty()) {
                        float sy = ry;
                        for (Setting s : vis) {
                            float sh = getRowH(s);
                            if (handleClick(mx, my, button, colX, sy, colW, s)) return true;
                            sy += sh + 3f;
                        }
                    }
                }
            }
            ry += clipH;
        }
        return false;
    }

    private boolean handleClick(double mx, double my, int button, float rx, float ry, float rw, Setting s) {
        float sh = getRowH(s);
        float lx = rx + 8f, rightX = rx + rw - 8f;
        if (s instanceof BooleanSetting bs && button == 0) {
            float tx = rightX - TOGGLE_W;
            float ty = ry + (sh - TOGGLE_H) / 2f;
            if (HoveringUtils.isHovered(mx, my, tx, ty, TOGGLE_W, TOGGLE_H)) {
                bs.setState(!bs.isState()); return true;
            }
            if (HoveringUtils.isHovered(mx, my, rx, ry, rw, sh)) {
                bs.setState(!bs.isState()); return true;
            }
        }
        if (s instanceof FloatSetting fs && button == 0) {
            float sliderY = ry + sh - 11f;
            float sliderW = rw - 16f;
            if (HoveringUtils.isHovered(mx, my, lx, sliderY - 2, sliderW, 8)) {
                float pos = (float) Math.max(0, Math.min(1, (mx - lx) / sliderW));
                float val = fs.getMin() + (fs.getMax() - fs.getMin()) * pos;
                val = Math.round(val / fs.getIncrement()) * fs.getIncrement();
                fs.setValue(Math.max(fs.getMin(), Math.min(fs.getMax(), val)));
                return true;
            }
        }
        if (s instanceof BindSetting && button == 0) {
            bindingSetting = (BindSetting) s; return true;
        }
        if (s instanceof TextSetting && button == 0) {
            editingText = (TextSetting) s; return true;
        }
        if (s instanceof ModeSetting ms && button == 0) {
            float oy = ry + 20f;
            for (String mode : ms.getMods()) {
                if (HoveringUtils.isHovered(mx, my, rx, oy, rw, MODE_ROW_H)) {
                    ms.set(mode); return true;
                }
                oy += MODE_ROW_H;
            }
        }
        if (s instanceof ListSetting ls && button == 0) {
            float oy = ry + 20f;
            for (BooleanSetting entry : ls.getSettings()) {
                if (!entry.visible()) { oy += LIST_ROW_H; continue; }
                float tx = rightX - TOGGLE_W;
                float ty = oy + (LIST_ROW_H - TOGGLE_H) / 2f;
                if (HoveringUtils.isHovered(mx, my, tx, ty, TOGGLE_W, TOGGLE_H)) {
                    entry.setState(!entry.isState()); return true;
                }
                if (HoveringUtils.isHovered(mx, my, rx, oy, rw, LIST_ROW_H)) {
                    entry.setState(!entry.isState()); return true;
                }
                oy += LIST_ROW_H;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mx, double my, int button) { return false; }
    public boolean mouseDragged(double mx, double my, int button) { return false; }

    public boolean mouseScrolled(double mx, double my, double amount) {
        float cx = winX + SIDE_W;
        float cw = WIN_W - SIDE_W;
        float contentY = contentTopY();
        float contentH = contentHeight();
        if (HoveringUtils.isHovered(mx, my, cx, contentY, cw, contentH)) {
            contScrollTarget += (float)(amount * 15);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int modifiers) {
        if (searching) {
            if (keyCode == 256) { searching = false; }
            else if (keyCode == 259) {
                if (!searchText.isEmpty()) searchText = searchText.substring(0, searchText.length() - 1);
            }
            else if (keyCode == 261) { searchText = ""; }
            return true;
        }
        if (editingText != null) {
            if (keyCode == 256 || keyCode == 257) { editingText = null; }
            else if (keyCode == 259) {
                String t = editingText.get();
                if (t != null && !t.isEmpty()) editingText.setText(t.substring(0, t.length() - 1));
            }
            else if (keyCode == 261) { editingText.setText(""); }
            return true;
        }
        if (bindingSetting != null) {
            if (keyCode == 256) { bindingSetting = null; }
            else if (keyCode == 261 || keyCode == 259) { bindingSetting.setKey(-1); bindingSetting = null; }
            else { bindingSetting.setKey(keyCode); bindingSetting = null; }
            return true;
        }
        if (bindingModule != null) {
            if (keyCode == 256) { bindingModule = null; }
            else if (keyCode == 261 || keyCode == 259) { bindingModule.setKey(-1); bindingModule = null; }
            else { bindingModule.setKey(keyCode); bindingModule = null; }
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr) {
        if (searching) {
            if (searchText.length() < SEARCH_MAX_LEN) searchText += chr;
            return true;
        }
        if (editingText != null) {
            String t = editingText.get();
            if (t == null) t = "";
            if (t.length() < editingText.getMaxLength())
                editingText.setText(t + chr);
            return true;
        }
        return false;
    }

    private int accent() {
        try {
            if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow"))
                return Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } catch (Exception ignored) {}
        return ColorUtils.getThemeColor();
    }

    private Font font(int size) { return Fonts.getFont("sf_regular", size); }
    private Font iconFont(int size) { return Fonts.getFont("icon", size); }
}

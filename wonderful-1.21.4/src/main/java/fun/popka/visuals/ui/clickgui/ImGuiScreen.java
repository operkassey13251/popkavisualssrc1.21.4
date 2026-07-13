package fun.popka.visuals.ui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import fun.popka.Popka;
import fun.popka.api.QClient;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.api.utils.animation.AnimationUtils;
import fun.popka.api.utils.animation.Easings;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.input.KeyBoardUtils;
import fun.popka.api.utils.math.HoveringUtils;
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

public class ImGuiScreen implements QClient {

    // ── Window (900/3 = 300, 500/3 = 167)
    private static final float WIN_W   = 300f;
    private static final float WIN_H   = 460f;

    // ── Left tab panel
    private static final float SEC_W   = 52f;
    private static final float SEC_H   = 30f;
    private static final float HEADER_H = 28f;

    // ── Middle sub-section panel
    private static final float SUB_W   = 90f;
    private static final float SUB_ROW = 32f;

    // ── Right content panel
    private static final float DESC_H  = 22f;
    // Setting row heights
    private static final float ROW_BOOL  = 28f;
    private static final float ROW_FLOAT = 44f;
    private static final float ROW_OTHER = 24f;

    // ── Colors (from colors.h)
    private static final int C_WIN_BG   = ColorUtils.rgba(0,   0,   0,   220);
    private static final int C_SEC_BG   = ColorUtils.rgba(18,  18,  23,  255);
    private static final int C_HEADER   = ColorUtils.rgba(11,  12,  15,  255);
    private static final int C_SUB_BG   = ColorUtils.rgba(13,  14,  17,  255);
    private static final int C_CONT_BG  = ColorUtils.rgba(10,  11,  14,  255);
    private static final int C_CHILD_BG = ColorUtils.rgba(7,   8,   11,  180);
    private static final int C_BLACK    = ColorUtils.rgba(0,   0,   0,   255);
    private static final int C_WHITE    = ColorUtils.rgba(255, 255, 255, 255);
    private static final int C_TEXT_ACT = ColorUtils.rgb(255, 255, 255);
    private static final int C_TEXT_HOV = ColorUtils.rgb(109, 116, 137);
    private static final int C_TEXT_INK = ColorUtils.rgb(89,  96,  117);
    private static final int C_CB_DIS   = ColorUtils.rgba(56,  57,  68,  255);

    // ── Tabs
    private static final String[] TAB_ICONS = {"b","c","d","d","h","e","h"};
    private static final String[] TAB_NAMES = {"Combat","Movement","World","Render","Other","Theme","Config"};
    private static final Module.ModuleCategory[] TAB_CATS = {
        Module.ModuleCategory.COMBAT,
        Module.ModuleCategory.MOVEMENT,
        Module.ModuleCategory.MISC,
        Module.ModuleCategory.RENDER,
        Module.ModuleCategory.PLAYER,
        null,
        null
    };
    private static final int TAB_THEME  = 5;
    private static final int TAB_CONFIG = 6;

    // ── State
    private float winX, winY;
    private int   selTab = 0, selSub = 0;

    private final AnimationUtils[] tabSelAnim  = new AnimationUtils[TAB_NAMES.length];
    private final AnimationUtils[] tabHovAnim  = new AnimationUtils[TAB_NAMES.length];
    private final Map<Integer, AnimationUtils>  subHovAnim = new HashMap<>();
    private final Map<Integer, AnimationUtils>  subEnAnim  = new HashMap<>();
    private final Map<Setting,  AnimationUtils> cbAnim     = new HashMap<>();

    private float subScroll = 0f, subScrollTarget = 0f;
    private final AnimationUtils subScrollAnim = new AnimationUtils(0f, 8f, Easings.CUBIC_OUT);
    private float contScroll = 0f, contScrollTarget = 0f;
    private final AnimationUtils contScrollAnim = new AnimationUtils(0f, 8f, Easings.CUBIC_OUT);

    private Module bindingModule = null;
    private BindSetting bindingSetting = null;
    private TextSetting editingText = null;
    private List<Module> allModules = new ArrayList<>();

    public ImGuiScreen() {
        for (int i = 0; i < TAB_NAMES.length; i++) {
            tabSelAnim[i] = new AnimationUtils(i == 0 ? 1f : 0f, 12f, Easings.CUBIC_OUT);
            tabHovAnim[i] = new AnimationUtils(0f, 10f, Easings.CUBIC_OUT);
        }
        refreshModules();
    }

    private void refreshModules() {
        allModules.clear();
        allModules.addAll(ModuleClass.INSTANCE.getObject().stream()
            .filter(m -> !"AutoBuy".equals(m.getName()) && !"AutoForest".equals(m.getName()))
            .toList());
    }

    private List<Module> getTabModules() {
        if (selTab >= TAB_CATS.length || TAB_CATS[selTab] == null) return List.of();
        Module.ModuleCategory cat = TAB_CATS[selTab];
        return allModules.stream().filter(m -> m.getCategory() == cat).toList();
    }

    // ── Render entry ──────────────────────────────────────────────────────────
    public void render(DrawContext ctx, int mx, int my, Window window, float progress, boolean closing) {
        if (window == null) return;
        float alpha = MathHelper.clamp(progress, 0f, 1f);
        winX = window.getScaledWidth()  / 2f - WIN_W / 2f;
        winY = window.getScaledHeight() / 2f - WIN_H / 2f;

        RenderUtils.drawRoundedRect(ctx.getMatrices(), winX, winY, WIN_W, WIN_H, 3f,
            ColorUtils.applyAlpha(C_WIN_BG, alpha));

        renderTabs(ctx, mx, my, alpha);
        renderSubList(ctx, mx, my, alpha);
        renderContent(ctx, mx, my, alpha);
    }

    // ── Left tab panel ────────────────────────────────────────────────────────
    private void renderTabs(DrawContext ctx, int mx, int my, float alpha) {
        float sx = winX, sy = winY;
        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx, sy, SEC_W, WIN_H, 3f,
            ColorUtils.applyAlpha(C_SEC_BG, alpha));
        // Header
        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx, sy, SEC_W, HEADER_H, 0,
            ColorUtils.applyAlpha(C_HEADER, alpha));
        font(10).drawCenteredString(ctx.getMatrices(), "S",
            sx + SEC_W / 2f, sy + 8f, ColorUtils.applyAlpha(accent(), alpha));

        float ty = sy + HEADER_H + 2f;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            boolean sel = (i == selTab);
            boolean hov = HoveringUtils.isHovered(mx, my, sx, ty, SEC_W, SEC_H);
            tabSelAnim[i].update(sel ? 1f : 0f);
            tabHovAnim[i].update(hov ? 1f : 0f);
            float sv = tabSelAnim[i].getValue();
            float hv = tabHovAnim[i].getValue();

            if (sv > 0.01f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), sx, ty, SEC_W * sv, SEC_H, 0,
                    ColorUtils.applyAlpha(accent(), alpha));

            float iconY = ty + (SEC_H - 10f) / 2f;
            int clr = sel ? ColorUtils.applyAlpha(C_BLACK, alpha)
                : ColorUtils.applyAlpha(ColorUtils.interpolateColor(C_TEXT_INK, C_TEXT_HOV, hv), alpha);
            iconFont(10).draw(ctx.getMatrices(), TAB_ICONS[i], sx + 8f, iconY, clr);
            font(10).draw(ctx.getMatrices(), TAB_NAMES[i], sx + 20f, iconY, clr);

            ty += SEC_H;
            if (i == 3) ty += 6f; // gap before Other/Theme/Config
        }
    }

    // ── Middle sub-list ───────────────────────────────────────────────────────
    private void renderSubList(DrawContext ctx, int mx, int my, float alpha) {
        float sx = winX + SEC_W, sy = winY;
        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx, sy, SUB_W, WIN_H, 0,
            ColorUtils.applyAlpha(C_SUB_BG, alpha));

        List<Module> mods = getTabModules();
        if (mods.isEmpty()) return;

        // Scroll
        subScrollAnim.update(subScrollTarget);
        float scroll = subScrollAnim.getValue();

        // Clamp
        float totalH = mods.size() * (SUB_ROW + 2f);
        float maxScroll = Math.min(0f, WIN_H - 8f - totalH);
        if (subScrollTarget < maxScroll) subScrollTarget = maxScroll;
        if (subScrollTarget > 0f)       subScrollTarget = 0f;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(sx, sy, SUB_W, WIN_H);

        float ty = sy + 4f + scroll;
        for (int i = 0; i < mods.size(); i++) {
            Module mod = mods.get(i);
            boolean sel = (i == selSub);
            boolean hov = HoveringUtils.isHovered(mx, my, sx + 2, ty, SUB_W - 4, SUB_ROW);
            boolean ena = mod.isEnable();

            AnimationUtils ha = subHovAnim.computeIfAbsent(i, k -> new AnimationUtils(0f, 9f, Easings.CUBIC_OUT));
            AnimationUtils ea = subEnAnim.computeIfAbsent(i,  k -> new AnimationUtils(ena ? 1f : 0f, 8f, Easings.LINEAR));
            ha.update(hov ? 1f : 0f);
            ea.update(ena ? 1f : 0f);
            float ev = ea.getValue();

            // Row bg
            int rowBg = sel
                ? ColorUtils.applyAlpha(ColorUtils.rgba(25, 25, 32, 255), alpha)
                : ColorUtils.applyAlpha(C_CHILD_BG, alpha);
            RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + 2, ty, SUB_W - 4, SUB_ROW, 2f, rowBg);

            // Enabled accent left bar
            if (ev > 0.01f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + 2, ty + 4, 2f, SUB_ROW - 8, 1f,
                    ColorUtils.applyAlpha(accent(), alpha * ev));

            // Module name
            int nameClr = ColorUtils.applyAlpha(
                ena ? C_TEXT_ACT : hov ? C_TEXT_HOV : C_TEXT_INK, alpha);
            font(11).draw(ctx.getMatrices(), mod.getName(), sx + 8, ty + (SUB_ROW - 10f) / 2f, nameClr);

            // ON / OFF badge
            int badgeClr = ColorUtils.applyAlpha(
                ColorUtils.interpolateColor(C_TEXT_INK, accent(), ev), alpha);
            String badge = ena ? "ON" : "OFF";
            float badgeX = sx + SUB_W - font(8).getWidth(badge) - 5;
            font(8).draw(ctx.getMatrices(), badge, badgeX, ty + (SUB_ROW - 8f) / 2f, badgeClr);

            ty += SUB_ROW + 2f;
        }

        ScissorUtils.pop();
    }

    // ── Right content panel — все настройки выбранного модуля ────────────────
    private void renderContent(DrawContext ctx, int mx, int my, float alpha) {
        float cx = winX + SEC_W + SUB_W, cy = winY;
        float cw = WIN_W - SEC_W - SUB_W, ch = WIN_H;

        RenderUtils.drawRoundedRect(ctx.getMatrices(), cx, cy, cw, ch, 0,
            ColorUtils.applyAlpha(C_CONT_BG, alpha));

        // Description bar
        RenderUtils.drawRoundedRect(ctx.getMatrices(), cx, cy, cw, DESC_H, 0,
            ColorUtils.applyAlpha(C_BLACK, alpha));
        List<Module> mods = getTabModules();
        String subName = (mods.isEmpty() || selSub >= mods.size())
            ? "-" : mods.get(selSub).getName();
        String path = TAB_NAMES[selTab] + "/" + subName;
        font(10).draw(ctx.getMatrices(), path, cx + 6, cy + 7,
            ColorUtils.applyAlpha(C_TEXT_HOV, alpha));

        float contentY = cy + DESC_H;
        float contentH = ch - DESC_H;

        if (selTab == TAB_THEME) {
            renderThemeContent(ctx, cx, contentY, cw, contentH, alpha);
            return;
        }
        if (selTab == TAB_CONFIG) {
            renderConfigContent(ctx, cx, contentY, cw, contentH, alpha);
            return;
        }
        if (mods.isEmpty() || selSub >= mods.size()) return;

        Module mod = mods.get(selSub);
        List<Setting> allSettings = mod.getSettings();
        if (allSettings == null) return;
        List<Setting> vis = allSettings.stream().filter(s -> s != null && s.visible()).toList();
        if (vis.isEmpty()) {
            font(10).draw(ctx.getMatrices(), "No settings", cx + 6, contentY + 12,
                ColorUtils.applyAlpha(C_TEXT_INK, alpha));
            return;
        }

        // Single scrollable column with ALL settings
        contScrollAnim.update(contScrollTarget);
        float scroll = contScrollAnim.getValue();

        // Clamp scroll
        float totalH = 4f;
        for (Setting s : vis) totalH += getRowH(s) + 2f;
        float maxScroll = Math.min(0f, contentH - totalH);
        if (contScrollTarget < maxScroll) contScrollTarget = maxScroll;
        if (contScrollTarget > 0f)       contScrollTarget = 0f;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(cx, contentY, cw, contentH);

        float ry = contentY + scroll + 4f;
        for (Setting s : vis) {
            float sh = getRowH(s);
            if (ry + sh >= contentY && ry <= contentY + contentH)
                renderRow(ctx, mx, my, cx + 3, ry, cw - 6, s, mod, alpha);
            ry += sh + 2f;
        }

        ScissorUtils.pop();
    }

    // ── Single setting row ────────────────────────────────────────────────────
    private void renderRow(DrawContext ctx, int mx, int my,
                            float rx, float ry, float rw,
                            Setting s, Module mod, float alpha) {
        float sh = getRowH(s);
        // Row bg
        RenderUtils.drawRoundedRect(ctx.getMatrices(), rx, ry, rw, sh, 2f,
            ColorUtils.applyAlpha(C_CHILD_BG, alpha));

        float lx = rx + 6f, rightX = rx + rw - 6f;

        if (s instanceof BooleanSetting bs) {
            boolean hov = HoveringUtils.isHovered(mx, my, rx, ry, rw, sh);
            int nameClr = ColorUtils.applyAlpha(
                bs.isState() ? C_TEXT_ACT : hov ? C_TEXT_HOV : C_TEXT_INK, alpha);
            font(11).draw(ctx.getMatrices(), s.name(), lx, ry + (sh - 10f) / 2f, nameClr);

            // Checkbox right side
            float cbS = 11f;
            float cbX = rightX - cbS - 2, cbY = ry + (sh - cbS) / 2f;
            AnimationUtils ca = cbAnim.computeIfAbsent(s,
                k -> new AnimationUtils(bs.isState() ? 1f : 0f, 9f, Easings.CUBIC_OUT));
            ca.update(bs.isState() ? 1f : 0f);
            float cv = ca.getValue();
            RenderUtils.drawRoundedRect(ctx.getMatrices(), cbX, cbY, cbS, cbS, 1f,
                ColorUtils.applyAlpha(C_CB_DIS, alpha));
            if (cv > 0.01f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), cbX, cbY, cbS, cbS, 1f,
                    ColorUtils.applyAlpha(accent(), (int)(alpha * cv)));

        } else if (s instanceof FloatSetting fs) {
            boolean hov = HoveringUtils.isHovered(mx, my, rx, ry, rw, sh);
            font(11).draw(ctx.getMatrices(), s.name(), lx, ry + 5f,
                ColorUtils.applyAlpha(hov ? C_TEXT_HOV : C_TEXT_INK, alpha));

            String valStr = String.format(Locale.ROOT,
                fs.getIncrement() < 1f ? "~$ %.1f" : "~$ %.0f", fs.get());
            font(10).draw(ctx.getMatrices(), valStr, lx, ry + 16f,
                ColorUtils.applyAlpha(C_TEXT_ACT, alpha));

            float sliderY = ry + sh - 11f;
            float sliderW = rw - 12f;
            float sliderH = 4f;
            float pos = (fs.getMax() == fs.getMin()) ? 0
                : (fs.get() - fs.getMin()) / (fs.getMax() - fs.getMin());
            // Track
            RenderUtils.drawRoundedRect(ctx.getMatrices(), lx, sliderY, sliderW, sliderH, sliderH / 2,
                ColorUtils.applyAlpha(C_BLACK, alpha));
            // Fill
            if (pos > 0.001f)
                RenderUtils.drawRoundedRect(ctx.getMatrices(), lx, sliderY, sliderW * pos, sliderH, sliderH / 2,
                    ColorUtils.applyAlpha(accent(), alpha));
            // Handle
            float grabX = lx + sliderW * pos - 3f;
            RenderUtils.drawRoundedRect(ctx.getMatrices(), grabX, sliderY - 1f, 6f, sliderH + 2f, 2f,
                ColorUtils.applyAlpha(C_WHITE, alpha));

        } else if (s instanceof ModeSetting ms) {
            font(11).draw(ctx.getMatrices(), s.name(), lx, ry + 5f,
                ColorUtils.applyAlpha(C_TEXT_HOV, alpha));
            float oy = ry + 20f;
            for (String mode : ms.getMods()) {
                boolean sel = mode.equals(ms.getCurrent());
                boolean hov = HoveringUtils.isHovered(mx, my, rx, oy, rw, MODE_ROW_H);
                float circX = lx + 3f, circY = oy + 8f;
                RenderUtils.drawRoundCircle(ctx.getMatrices(), circX, circY, 6f,
                    ColorUtils.applyAlpha(sel ? accent() : C_CB_DIS, alpha));
                if (sel)
                    RenderUtils.drawRoundCircle(ctx.getMatrices(), circX, circY, 3f,
                        ColorUtils.applyAlpha(C_WHITE, alpha));
                int clr = ColorUtils.applyAlpha(
                    sel ? accent() : hov ? C_TEXT_HOV : C_TEXT_INK, alpha);
                font(11).draw(ctx.getMatrices(), mode, lx + 12f, oy + 3f, clr);
                oy += MODE_ROW_H;
            }

        } else if (s instanceof BindSetting bind) {
            font(11).draw(ctx.getMatrices(), s.name(), lx, ry + (sh - 10f) / 2f,
                ColorUtils.applyAlpha(C_TEXT_INK, alpha));
            String bStr = (bindingSetting == bind || bind.getKey() == -1)
                ? "..." : KeyBoardUtils.getBindName(bind.getKey());
            float bw = font(10).getWidth(bStr) + 8f;
            float bx = rightX - bw;
            RenderUtils.drawRoundedRect(ctx.getMatrices(), bx, ry + 4f, bw, sh - 8f, 1f,
                ColorUtils.applyAlpha(C_WHITE, alpha));
            font(10).draw(ctx.getMatrices(), bStr, bx + 4f, ry + (sh - 9f) / 2f,
                ColorUtils.applyAlpha(C_BLACK, alpha));

        } else if (s instanceof TextSetting ts) {
            font(11).draw(ctx.getMatrices(), s.name(), lx, ry + (sh - 10f) / 2f,
                ColorUtils.applyAlpha(C_TEXT_INK, alpha));
            boolean editing = editingText == ts;
            String tv = ts.get() == null || ts.get().isEmpty() ? "" : ts.get();
            String boxText = editing ? tv + "_" : (tv.isEmpty() ? "..." : tv);
            float bw = font(10).getWidth(boxText) + 8f;
            float bx = rightX - bw;
            int boxBg = ColorUtils.applyAlpha(editing ? C_WHITE : C_CHILD_BG, alpha);
            RenderUtils.drawRoundedRect(ctx.getMatrices(), bx, ry + 4f, bw, sh - 8f, 1f, boxBg);
            int txtClr = ColorUtils.applyAlpha(editing ? C_BLACK : C_TEXT_HOV, alpha);
            font(10).draw(ctx.getMatrices(), boxText, bx + 4f, ry + (sh - 9f) / 2f, txtClr);

        } else if (s instanceof ListSetting ls) {
            font(11).draw(ctx.getMatrices(), s.name(), lx, ry + 5f,
                ColorUtils.applyAlpha(C_TEXT_HOV, alpha));
            float oy = ry + 20f;
            for (BooleanSetting entry : ls.getSettings()) {
                if (!entry.visible()) { oy += LIST_ROW_H; continue; }
                boolean hov = HoveringUtils.isHovered(mx, my, rx, oy, rw, LIST_ROW_H);
                int nameClr = ColorUtils.applyAlpha(
                    entry.isState() ? C_TEXT_ACT : hov ? C_TEXT_HOV : C_TEXT_INK, alpha);
                font(11).draw(ctx.getMatrices(), entry.name(), lx + 12f, oy + 4f, nameClr);

                float cbS = 11f;
                float cbX = rightX - cbS - 2, cbY = oy + (LIST_ROW_H - cbS) / 2f;
                AnimationUtils ca = cbAnim.computeIfAbsent(entry,
                    k -> new AnimationUtils(entry.isState() ? 1f : 0f, 9f, Easings.CUBIC_OUT));
                ca.update(entry.isState() ? 1f : 0f);
                float cv = ca.getValue();
                RenderUtils.drawRoundedRect(ctx.getMatrices(), cbX, cbY, cbS, cbS, 1f,
                    ColorUtils.applyAlpha(C_CB_DIS, alpha));
                if (cv > 0.01f)
                    RenderUtils.drawRoundedRect(ctx.getMatrices(), cbX, cbY, cbS, cbS, 1f,
                        ColorUtils.applyAlpha(accent(), (int)(alpha * cv)));
                oy += LIST_ROW_H;
            }
        }
    }

    private float getRowH(Setting s) {
        if (s instanceof FloatSetting) return ROW_FLOAT;
        if (s instanceof BooleanSetting) return ROW_BOOL;
        if (s instanceof ModeSetting ms) return 20f + ms.getMods().size() * 16f;
        if (s instanceof ListSetting ls) {
            int visCount = 0;
            for (BooleanSetting e : ls.getSettings()) if (e.visible()) visCount++;
            return 20f + visCount * 18f;
        }
        return ROW_OTHER;
    }

    private static final float MODE_ROW_H = 16f;
    private static final float LIST_ROW_H = 18f;

    // ── Theme / Config tabs ───────────────────────────────────────────────────
    private void renderThemeContent(DrawContext ctx, float cx, float cy, float cw, float ch, float alpha) {
        font(11).draw(ctx.getMatrices(), "Theme", cx + 6, cy + 8,
            ColorUtils.applyAlpha(C_TEXT_ACT, alpha));
        font(10).draw(ctx.getMatrices(), "Color theme settings",
            cx + 6, cy + 22, ColorUtils.applyAlpha(C_TEXT_INK, alpha));
        try {
            int ac = accent();
            RenderUtils.drawRoundedRect(ctx.getMatrices(), cx + 6, cy + 38, 30f, 15f, 2f,
                ColorUtils.applyAlpha(ac, alpha));
            font(10).draw(ctx.getMatrices(), "Accent color", cx + 42, cy + 42,
                ColorUtils.applyAlpha(C_TEXT_HOV, alpha));
        } catch (Exception ignored) {}
    }

    private void renderConfigContent(DrawContext ctx, float cx, float cy, float cw, float ch, float alpha) {
        font(11).draw(ctx.getMatrices(), "Configs", cx + 6, cy + 8,
            ColorUtils.applyAlpha(C_TEXT_ACT, alpha));
        font(10).draw(ctx.getMatrices(), "Manage configurations",
            cx + 6, cy + 22, ColorUtils.applyAlpha(C_TEXT_INK, alpha));
        try {
            RenderUtils.drawRoundedRect(ctx.getMatrices(), cx + 4, cy + 38, cw - 8, 18f, 1f,
                ColorUtils.applyAlpha(C_BLACK, alpha));
            String cur = Popka.INSTANCE.configStorage.clickGuiStyle != null
                ? Popka.INSTANCE.configStorage.clickGuiStyle : "default";
            font(10).draw(ctx.getMatrices(), "Current: " + cur, cx + 8, cy + 43,
                ColorUtils.applyAlpha(C_TEXT_HOV, alpha));
        } catch (Exception ignored) {}
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    public boolean mouseClicked(double mx, double my, int button) {
        if (mc == null) return false;
        editingText = null;

        // Tab clicks
        float ty = winY + HEADER_H + 2f;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            if (HoveringUtils.isHovered(mx, my, winX, ty, SEC_W, SEC_H) && button == 0) {
                selTab = i; selSub = 0;
                subScrollTarget = 0; contScrollTarget = 0;
                return true;
            }
            ty += SEC_H;
            if (i == 3) ty += 6f;
        }

        // Sub-list clicks
        float subX = winX + SEC_W;
        List<Module> mods = getTabModules();
        subScrollAnim.update(subScrollTarget);
        float scroll = subScrollAnim.getValue();
        float subY = winY + 4f + scroll;
        for (int i = 0; i < mods.size(); i++) {
            if (HoveringUtils.isHovered(mx, my, subX + 2, subY, SUB_W - 4, SUB_ROW)) {
                if (button == 0) { mods.get(i).toggle(); return true; }
                if (button == 1) { selSub = i; contScrollTarget = 0; return true; }
                if (button == 2) { bindingModule = mods.get(i); return true; }
            }
            subY += SUB_ROW + 2f;
        }

        if (bindingModule != null && button >= 2) {
            bindingModule.setKey(KeyBoardUtils.createMouseBind(button));
            bindingModule = null;
            return true;
        }

        // Content clicks
        if (!mods.isEmpty() && selSub < mods.size()) {
            Module mod = mods.get(selSub);
            List<Setting> all = mod.getSettings();
            if (all != null) {
                List<Setting> vis = all.stream().filter(s -> s != null && s.visible()).toList();
                float cx = winX + SEC_W + SUB_W;
                float cw = WIN_W - SEC_W - SUB_W;
                contScrollAnim.update(contScrollTarget);
                float cs = contScrollAnim.getValue();
                float ry = winY + DESC_H + cs + 4f;
                for (Setting s : vis) {
                    float sh = getRowH(s);
                    if (handleClick(mx, my, button, cx + 3, ry, cw - 6, s)) return true;
                    ry += sh + 2f;
                }
            }
        }
        return false;
    }

    private boolean handleClick(double mx, double my, int button,
                                 float rx, float ry, float rw, Setting s) {
        float sh = getRowH(s);
        if (!HoveringUtils.isHovered(mx, my, rx, ry, rw, sh)) return false;
        if (s instanceof BooleanSetting bs && button == 0) { bs.setState(!bs.isState()); return true; }
        if (s instanceof FloatSetting fs && button == 0) {
            float sliderY = ry + sh - 11f;
            float lx = rx + 6f, sliderW = rw - 12f;
            if (HoveringUtils.isHovered(mx, my, lx, sliderY - 2, sliderW, 8)) {
                float pos = (float) Math.max(0, Math.min(1, (mx - lx) / sliderW));
                float val = fs.getMin() + (fs.getMax() - fs.getMin()) * pos;
                val = Math.round(val / fs.getIncrement()) * fs.getIncrement();
                fs.setValue(Math.max(fs.getMin(), Math.min(fs.getMax(), val)));
                return true;
            }
        }
        if (s instanceof BindSetting && button == 0) {
            bindingSetting = (BindSetting) s;
            return true;
        }
        if (s instanceof TextSetting && button == 0) {
            editingText = (TextSetting) s;
            return true;
        }
        if (s instanceof ModeSetting ms && button == 0) {
            float oy = ry + 20f;
            for (String mode : ms.getMods()) {
                if (HoveringUtils.isHovered(mx, my, rx, oy, rw, MODE_ROW_H)) {
                    ms.set(mode);
                    return true;
                }
                oy += MODE_ROW_H;
            }
        }
        if (s instanceof ListSetting ls && button == 0) {
            float oy = ry + 20f;
            for (BooleanSetting entry : ls.getSettings()) {
                if (!entry.visible()) { oy += LIST_ROW_H; continue; }
                if (HoveringUtils.isHovered(mx, my, rx, oy, rw, LIST_ROW_H)) {
                    entry.setState(!entry.isState());
                    return true;
                }
                oy += LIST_ROW_H;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mx, double my, int button) { return false; }
    public boolean mouseDragged(double mx, double my, int button) { return false; }

    public boolean mouseScrolled(double mx, double my, double amount) {
        float subX = winX + SEC_W;
        if (HoveringUtils.isHovered(mx, my, subX, winY, SUB_W, WIN_H)) {
            subScrollTarget += (float)(amount * 15);
            return true;
        }
        float cx = winX + SEC_W + SUB_W;
        float cw = WIN_W - SEC_W - SUB_W;
        if (HoveringUtils.isHovered(mx, my, cx, winY + DESC_H, cw, WIN_H - DESC_H)) {
            contScrollTarget += (float)(amount * 15);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int modifiers) {
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
        if (editingText != null) {
            String t = editingText.get();
            if (t == null) t = "";
            if (t.length() < editingText.getMaxLength())
                editingText.setText(t + chr);
            return true;
        }
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int accent() {
        try {
            if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow"))
                return Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } catch (Exception ignored) {}
        return ColorUtils.getThemeColor();
    }

    private Font font(int size)     { return Fonts.getFont("suisse", size); }
    private Font iconFont(int size) { return Fonts.getFont("icon", size); }
}

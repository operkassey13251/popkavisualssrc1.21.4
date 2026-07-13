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

public class ClickGuiNewScreen implements QClient {

    // ── Layout constants ──────────────────────────────────────────────────────
    // Overall window - уменьшено ещё в 4-5 раз
    private static final float WIN_W = 580f;
    private static final float WIN_H = 340f;
    private static final float WIN_RADIUS = 6f;

    // Left sidebar
    private static final float SIDEBAR_W = 110f;
    private static final float SIDEBAR_HEADER_H = 38f;
    private static final float SIDEBAR_FOOTER_H = 28f;
    private static final float CAT_ROW_H = 22f;
    private static final float CAT_ICON_X = 10f;
    private static final float CAT_TEXT_X = 26f;
    private static final float CAT_SECTION_LABEL_H = 16f;

    // Right content area
    private static final float CONTENT_X = SIDEBAR_W;
    private static final float CONTENT_TOP_H = 28f;   // topbar height
    private static final float COLUMN_W = 150f;
    private static final float COLUMN_GAP = 1f;
    private static final int   COLUMNS = 3;

    // Module row
    private static final float MOD_ROW_H = 20f;
    private static final float MOD_SECTION_LABEL_H = 18f;
    private static final float MOD_PADDING_X = 8f;
    private static final float TOGGLE_W = 20f;
    private static final float TOGGLE_H = 11f;
    private static final float GEAR_W = 12f;
    private static final float GEAR_GAP = 4f;

    // Colors
    private static final int COL_BG         = ColorUtils.rgba(14, 18, 28, 255);
    private static final int COL_SIDEBAR     = ColorUtils.rgba(10, 14, 22, 255);
    private static final int COL_TOPBAR      = ColorUtils.rgba(12, 16, 25, 230);
    private static final int COL_SEP         = ColorUtils.rgba(35, 45, 65, 200);
    private static final int COL_CAT_ACTIVE  = ColorUtils.rgba(30, 38, 58, 255);
    private static final int COL_CAT_HOVER   = ColorUtils.rgba(22, 30, 46, 200);
    private static final int COL_MOD_HOVER   = ColorUtils.rgba(22, 30, 46, 180);
    private static final int COL_TEXT_PRI    = ColorUtils.rgb(218, 228, 245);
    private static final int COL_TEXT_SEC    = ColorUtils.rgb(110, 125, 155);
    private static final int COL_TEXT_ACT    = ColorUtils.rgb(255, 255, 255);
    private static final int COL_TOGGLE_OFF  = ColorUtils.rgba(40, 50, 72, 255);
    private static final int COL_SECTION_LBL = ColorUtils.rgb(90, 105, 135);

    // ── Runtime state ─────────────────────────────────────────────────────────
    private float winX, winY;
    private Module.ModuleCategory selectedCategory = Module.ModuleCategory.COMBAT;

    // Per-module settings expansion
    private final Map<Module, Boolean> expanded = new HashMap<>();
    // Per-module open-animation
    private final Map<Module, AnimationUtils> openAnim = new HashMap<>();
    // Toggle bg/circle animations (by module hashCode as key)
    private final Map<Module, AnimationUtils> toggleBgAnim = new HashMap<>();
    // Category hover
    private final Map<Module.ModuleCategory, AnimationUtils> catHoverAnim = new EnumMap<>(Module.ModuleCategory.class);
    // Module hover
    private final Map<Module, AnimationUtils> modHoverAnim = new HashMap<>();
    // Gear hover
    private final Map<Module, AnimationUtils> gearHoverAnim = new HashMap<>();
    // Column scrolls (one per column index)
    private final float[] colScrollTarget = new float[COLUMNS];
    private final AnimationUtils[] colScrollAnim = new AnimationUtils[COLUMNS];

    // Binding
    private Module bindingModule = null;
    // Settings panel (right-side overlay for a module)
    private Module settingsModule = null;
    // Search
    private boolean searchActive = false;
    private String searchText = "";

    private List<Module> allModules = new ArrayList<>();

    public ClickGuiNewScreen() {
        for (int i = 0; i < COLUMNS; i++) {
            colScrollAnim[i] = new AnimationUtils(0f, 8f, Easings.CUBIC_OUT);
        }
        refreshModules();
    }

    private void refreshModules() {
        allModules.clear();
        allModules.addAll(ModuleClass.INSTANCE.getObject().stream()
                .filter(m -> !"AutoBuy".equals(m.getName()) && !"AutoForest".equals(m.getName()))
                .toList());
    }

    private List<Module> getModules(Module.ModuleCategory cat) {
        List<Module> base = allModules.stream()
                .filter(m -> m.getCategory() == cat).toList();
        if (searchText.isBlank()) return base;
        String q = searchText.toLowerCase(Locale.ROOT);
        return base.stream().filter(m ->
                m.getName().toLowerCase(Locale.ROOT).contains(q) ||
                m.getDisplayName().toLowerCase(Locale.ROOT).contains(q)).toList();
    }

    // Called from MenuPanel every frame
    public void render(DrawContext ctx, int mx, int my, Window window, float progress, boolean closing) {
        if (window == null) return;
        float alpha = MathHelper.clamp(progress, 0f, 1f);

        winX = window.getScaledWidth()  / 2f - WIN_W / 2f;
        winY = window.getScaledHeight() / 2f - WIN_H / 2f;

        // Shadow layers (multiple for soft shadow effect)
        for (int i = 4; i >= 1; i--) {
            float offset = i * 2f;
            float shadowAlpha = alpha * 0.08f * (5 - i);
            RenderUtils.drawRoundedRect(ctx.getMatrices(), 
                winX - offset, winY - offset, 
                WIN_W + offset * 2, WIN_H + offset * 2, 
                WIN_RADIUS + offset, 
                ColorUtils.applyAlpha(0, shadowAlpha));
        }

        // Main window background
        RenderUtils.drawRoundedRect(ctx.getMatrices(), winX, winY, WIN_W, WIN_H, WIN_RADIUS,
                ColorUtils.applyAlpha(COL_BG, alpha));

        renderSidebar(ctx, mx, my, alpha);
        renderTopbar(ctx, mx, my, alpha);
        renderContent(ctx, mx, my, alpha);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sidebar - полностью переписан под компактный дизайн
    // ─────────────────────────────────────────────────────────────────────────
    private void renderSidebar(DrawContext ctx, int mx, int my, float alpha) {
        float sx = winX, sy = winY;
        
        // Sidebar background
        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx, sy, SIDEBAR_W, WIN_H, WIN_RADIUS,
                ColorUtils.applyAlpha(COL_SIDEBAR, alpha));
        
        // Right separator line
        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + SIDEBAR_W - 1f, sy + 4, 1f, WIN_H - 8,
                0, ColorUtils.applyAlpha(COL_SEP, alpha));

        // Logo "S" - centered with proper spacing
        float logoY = sy + (SIDEBAR_HEADER_H - 14) / 2f;
        font(14).drawCenteredString(ctx.getMatrices(), "S",
                sx + SIDEBAR_W / 2f, logoY,
                ColorUtils.applyAlpha(getTheme(), alpha));

        // Separator under logo
        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + 8, sy + SIDEBAR_HEADER_H - 1, SIDEBAR_W - 16, 1f,
                0, ColorUtils.applyAlpha(COL_SEP, alpha));

        // Categories section
        float ry = sy + SIDEBAR_HEADER_H + 2;

        // "Main" section label
        font(8).draw(ctx.getMatrices(), "MAIN", sx + CAT_ICON_X, ry + 4,
                ColorUtils.applyAlpha(COL_SECTION_LBL, alpha));
        ry += CAT_SECTION_LABEL_H;

        Module.ModuleCategory[] mainCats = {
                Module.ModuleCategory.COMBAT,
                Module.ModuleCategory.MOVEMENT,
                Module.ModuleCategory.PLAYER,
                Module.ModuleCategory.RENDER,
                Module.ModuleCategory.MISC
        };
        
        for (Module.ModuleCategory cat : mainCats) {
            renderCategoryRow(ctx, mx, my, sx, ry, cat, alpha);
            ry += CAT_ROW_H;
        }

        // "Lua" section label
        ry += 3;
        font(8).draw(ctx.getMatrices(), "LUA", sx + CAT_ICON_X, ry + 4,
                ColorUtils.applyAlpha(COL_SECTION_LBL, alpha));
        ry += CAT_SECTION_LABEL_H;

        // Footer separator
        float footerY = sy + WIN_H - SIDEBAR_FOOTER_H;
        RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + 8, footerY, SIDEBAR_W - 16, 1f,
                0, ColorUtils.applyAlpha(COL_SEP, alpha));

        // Player info - compact
        if (mc != null && mc.player != null) {
            String name = mc.player.getGameProfile().getName();
            // Avatar circle
            RenderUtils.drawRoundCircle(ctx.getMatrices(), sx + 16, footerY + 12, 7,
                    ColorUtils.applyAlpha(ColorUtils.rgba(50, 60, 80, 255), alpha));
            // Name
            font(9).draw(ctx.getMatrices(), name, sx + 28, footerY + 8,
                    ColorUtils.applyAlpha(COL_TEXT_PRI, alpha));
            // Arrow
            font(8).draw(ctx.getMatrices(), "▶", sx + SIDEBAR_W - 14, footerY + 8,
                    ColorUtils.applyAlpha(COL_TEXT_SEC, alpha));
        }
    }

    private void renderCategoryRow(DrawContext ctx, int mx, int my, float sx, float ry,
                                   Module.ModuleCategory cat, float alpha) {
        boolean active = selectedCategory == cat;
        boolean hovered = HoveringUtils.isHovered(mx, my, sx + 2, ry, SIDEBAR_W - 4, CAT_ROW_H);

        AnimationUtils ha = catHoverAnim.computeIfAbsent(cat,
                k -> new AnimationUtils(active ? 1f : 0f, 10f, Easings.CUBIC_OUT));
        ha.update(hovered || active ? 1f : 0f);
        float hv = ha.getValue();

        // Background
        if (hv > 0.01f) {
            int bgColor = active ? ColorUtils.applyAlpha(COL_CAT_ACTIVE, alpha)
                                 : ColorUtils.applyAlpha(COL_CAT_HOVER, alpha * hv);
            RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + 4, ry, SIDEBAR_W - 8, CAT_ROW_H, 4f, bgColor);
        }
        
        // Active indicator bar
        if (active) {
            RenderUtils.drawRoundedRect(ctx.getMatrices(), sx + 4, ry + 5, 2f, CAT_ROW_H - 10, 1f,
                    ColorUtils.applyAlpha(getTheme(), alpha));
        }

        // Icon - perfectly centered
        float iconY = ry + (CAT_ROW_H - 9) / 2f;
        iconFont(9).draw(ctx.getMatrices(), cat.getIcons(),
                sx + CAT_ICON_X + (active ? 4f : 2f), iconY,
                ColorUtils.applyAlpha(active ? getTheme() : COL_TEXT_SEC, alpha));
        
        // Label - perfectly centered
        float textY = ry + (CAT_ROW_H - 9) / 2f;
        font(9).draw(ctx.getMatrices(), cat.getName(),
                sx + CAT_TEXT_X, textY,
                ColorUtils.applyAlpha(active ? COL_TEXT_ACT : COL_TEXT_PRI, alpha));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Topbar - компактный дизайн
    // ─────────────────────────────────────────────────────────────────────────
    private void renderTopbar(DrawContext ctx, int mx, int my, float alpha) {
        float tx = winX + CONTENT_X, ty = winY;
        float tw = WIN_W - CONTENT_X;

        RenderUtils.drawRoundedRect(ctx.getMatrices(), tx, ty, tw, CONTENT_TOP_H, 0,
                ColorUtils.applyAlpha(COL_TOPBAR, alpha));
        RenderUtils.drawRoundedRect(ctx.getMatrices(), tx, ty + CONTENT_TOP_H - 1f, tw, 1f, 0,
                ColorUtils.applyAlpha(COL_SEP, alpha));

        // Vertically centered content
        float centerY = ty + (CONTENT_TOP_H - 8) / 2f;

        // Icons and count
        font(8).draw(ctx.getMatrices(), "■", tx + 10, centerY,
                ColorUtils.applyAlpha(COL_TEXT_SEC, alpha));
        
        String countStr = String.valueOf(getModules(selectedCategory).size());
        font(9).draw(ctx.getMatrices(), countStr, tx + 22, centerY,
                ColorUtils.applyAlpha(COL_TEXT_PRI, alpha));
        
        font(8).draw(ctx.getMatrices(), "▼", tx + 40, centerY,
                ColorUtils.applyAlpha(COL_TEXT_SEC, alpha));

        // Search icon on right
        font(9).draw(ctx.getMatrices(), "🔍", tx + tw - 16, centerY,
                ColorUtils.applyAlpha(COL_TEXT_SEC, alpha));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Content - полностью переписан для компактного отображения
    // ─────────────────────────────────────────────────────────────────────────
    private void renderContent(DrawContext ctx, int mx, int my, float alpha) {
        float cx = winX + CONTENT_X;
        float cy = winY + CONTENT_TOP_H;
        float cw = WIN_W - CONTENT_X;
        float ch = WIN_H - CONTENT_TOP_H;

        List<Module> mods = getModules(selectedCategory);
        int total = mods.size();
        int perCol = (total + COLUMNS - 1) / COLUMNS;

        for (int col = 0; col < COLUMNS; col++) {
            int from = col * perCol;
            int to   = Math.min(from + perCol, total);
            if (from >= total) break;

            List<Module> colMods = mods.subList(from, to);
            float colX = cx + col * (cw / COLUMNS);
            float colW = cw / COLUMNS;

            // Column separator
            if (col > 0) {
                RenderUtils.drawRoundedRect(ctx.getMatrices(), colX, cy + 4, 1f, ch - 8, 0,
                        ColorUtils.applyAlpha(COL_SEP, alpha));
            }

            // Update scroll
            colScrollAnim[col].update(colScrollTarget[col]);
            float scroll = colScrollAnim[col].getValue();

            ScissorUtils.push();
            ScissorUtils.setFromComponentCoordinates(colX, cy, colW, ch);

            // Section header
            float ry = cy + scroll + 4;
            font(8).draw(ctx.getMatrices(), selectedCategory.getName().toUpperCase(Locale.ROOT),
                    colX + MOD_PADDING_X, ry + 3,
                    ColorUtils.applyAlpha(COL_SECTION_LBL, alpha));
            ry += MOD_SECTION_LABEL_H;

            for (Module mod : colMods) {
                float modH = getModuleRenderHeight(mod);
                if (ry + modH >= cy && ry <= cy + ch) {
                    renderModuleRow(ctx, mx, my, colX, ry, colW, mod, alpha);
                }
                ry += modH;
            }

            // Clamp scroll
            float totalH = MOD_SECTION_LABEL_H + 4;
            for (Module m : colMods) totalH += getModuleRenderHeight(m);
            float maxScroll = Math.min(0f, ch - totalH);
            if (colScrollTarget[col] < maxScroll) colScrollTarget[col] = maxScroll;
            if (colScrollTarget[col] > 0f)        colScrollTarget[col] = 0f;

            ScissorUtils.pop();
        }
    }

    private float getModuleRenderHeight(Module mod) {
        boolean exp = Boolean.TRUE.equals(expanded.get(mod));
        AnimationUtils anim = openAnim.computeIfAbsent(mod,
                k -> new AnimationUtils(exp ? 1f : 0f, 14f, Easings.CUBIC_OUT));
        anim.update(exp ? 1f : 0f);
        float settH = calculateSettingsHeight(mod) * anim.getValue();
        return MOD_ROW_H + settH;
    }

    private void renderModuleRow(DrawContext ctx, int mx, int my,
                                  float colX, float ry, float colW,
                                  Module mod, float alpha) {
        boolean enabled = mod.isEnable();
        boolean exp     = Boolean.TRUE.equals(expanded.get(mod));

        // Hover background
        boolean hovered = HoveringUtils.isHovered(mx, my, colX + 2, ry, colW - 4, MOD_ROW_H);
        AnimationUtils mhAnim = modHoverAnim.computeIfAbsent(mod,
                k -> new AnimationUtils(0f, 9f, Easings.CUBIC_OUT));
        mhAnim.update(hovered ? 1f : 0f);
        float mhv = mhAnim.getValue();
        if (mhv > 0.01f) {
            RenderUtils.drawRoundedRect(ctx.getMatrices(), colX + 2, ry, colW - 4, MOD_ROW_H, 3f,
                    ColorUtils.applyAlpha(COL_MOD_HOVER, (int)(alpha * mhv * 255) > 255 ? 255 : (int)(alpha * mhv * 255)));
        }

        // Gear icon
        List<Setting> settings = mod.getSettings();
        boolean hasSettings = settings != null && !settings.isEmpty();
        float gearX = colX + colW - 5 - TOGGLE_W - GEAR_GAP - GEAR_W;

        if (hasSettings) {
            boolean gearHov = HoveringUtils.isHovered(mx, my, gearX, ry + (MOD_ROW_H - GEAR_W) / 2f, GEAR_W, GEAR_W);
            AnimationUtils ghAnim = gearHoverAnim.computeIfAbsent(mod,
                    k -> new AnimationUtils(0f, 9f, Easings.CUBIC_OUT));
            ghAnim.update(gearHov ? 1f : 0f);
            float ghv = ghAnim.getValue();
            int gearColor = ColorUtils.applyAlpha(
                    exp ? getTheme() : ColorUtils.interpolateColor(COL_TEXT_SEC, COL_TEXT_PRI, ghv),
                    alpha);
            float gearY = ry + (MOD_ROW_H - 8) / 2f;
            iconFont(8).draw(ctx.getMatrices(), "k", gearX + 2, gearY, gearColor);
        }

        // Toggle
        float toggleX = colX + colW - 5 - TOGGLE_W;
        float toggleY = ry + (MOD_ROW_H - TOGGLE_H) / 2f;
        renderToggle(ctx, toggleX, toggleY, mod, enabled, alpha);

        // Module name - perfectly centered
        String name = bindingModule == mod ? mod.getName() + " [...]" : mod.getName();
        int nameColor = ColorUtils.applyAlpha(enabled ? COL_TEXT_ACT : COL_TEXT_PRI, alpha);
        if (!enabled) nameColor = ColorUtils.applyAlpha(COL_TEXT_SEC, alpha);
        float nameY = ry + (MOD_ROW_H - 8) / 2f;
        font(enabled ? 9 : 8).draw(ctx.getMatrices(), name, colX + MOD_PADDING_X, nameY, nameColor);

        // Bind text
        if (mod.getKey() != -1 && bindingModule != mod) {
            String bind = " [" + KeyBoardUtils.getBindName(mod.getKey()) + "]";
            float nameW = font(enabled ? 9 : 8).getWidth(name);
            font(7).draw(ctx.getMatrices(), bind,
                    colX + MOD_PADDING_X + nameW, nameY,
                    ColorUtils.applyAlpha(COL_TEXT_SEC, alpha));
        }

        // Settings dropdown
        AnimationUtils oa = openAnim.computeIfAbsent(mod,
                k -> new AnimationUtils(0f, 14f, Easings.CUBIC_OUT));
        float op = oa.getValue();
        if (op > 0.01f && hasSettings) {
            float settY = ry + MOD_ROW_H;
            renderSettings(ctx, mx, my, colX, settY, colW, mod, op, alpha);
        }
    }

    private void renderToggle(DrawContext ctx, float tx, float ty, Module mod, boolean on, float alpha) {
        AnimationUtils bgAnim = toggleBgAnim.computeIfAbsent(mod,
                k -> new AnimationUtils(on ? 1f : 0f, 12f, Easings.CUBIC_OUT));
        bgAnim.update(on ? 1f : 0f);
        float t = bgAnim.getValue();

        int offCol  = ColorUtils.applyAlpha(COL_TOGGLE_OFF, alpha);
        int onCol   = ColorUtils.applyAlpha(getTheme(), alpha);
        int bgColor = ColorUtils.interpolateColor(offCol, onCol, t);
        int dimBg   = ColorUtils.applyAlpha(ColorUtils.darken(bgColor, 0.6f), alpha);

        // Background
        RenderUtils.drawRoundedRect(ctx.getMatrices(), tx, ty, TOGGLE_W, TOGGLE_H, TOGGLE_H / 2f, dimBg);
        RenderUtils.drawGradientRect(ctx.getMatrices(), tx, ty, TOGGLE_W, TOGGLE_H, TOGGLE_H / 2f, bgColor,
                ColorUtils.darken(bgColor, 0.35f));

        // Circle
        float circleX = tx + 2f + t * (TOGGLE_W - TOGGLE_H + 1f);
        float circleY = ty + TOGGLE_H / 2f;
        float circleR = (TOGGLE_H - 3f);
        RenderUtils.drawRoundCircle(ctx.getMatrices(), circleX, circleY, circleR, ColorUtils.applyAlpha(-1, alpha));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Settings rows (inline dropdown)
    // ─────────────────────────────────────────────────────────────────────────
    private void renderSettings(DrawContext ctx, int mx, int my,
                                 float colX, float baseY, float colW,
                                 Module mod, float openProg, float alpha) {
        List<Setting> settings = mod.getSettings();
        if (settings == null) return;
        float ry = baseY;
        for (Setting s : settings) {
            if (s == null || !s.visible()) continue;
            float sh = getSettingHeight(s);
            if (openProg < 1f) {
                // fade clip
            }
            renderSingleSetting(ctx, mx, my, colX, ry, colW, s, alpha * openProg, mod);
            ry += sh;
        }
    }

    private void renderSingleSetting(DrawContext ctx, int mx, int my,
                                      float colX, float ry, float colW,
                                      Setting s, float alpha, Module mod) {
        float lx = colX + MOD_PADDING_X + 4;
        float rx = colX + colW - 6;

        if (s instanceof BooleanSetting bs) {
            String label = s.name();
            font(8).draw(ctx.getMatrices(), label, lx, ry + 3,
                    ColorUtils.applyAlpha(COL_TEXT_PRI, alpha));
            float tx2 = rx - TOGGLE_W;
            renderToggle(ctx, tx2, ry + (10 - TOGGLE_H) / 2f, mod, bs.isState(), alpha);

        } else if (s instanceof FloatSetting fs) {
            String label = s.name();
            font(8).draw(ctx.getMatrices(), label, lx, ry + 2,
                    ColorUtils.applyAlpha(COL_TEXT_PRI, alpha));
            String val = String.format(Locale.ROOT, fs.getIncrement() < 1f ? "%.1f" : "%.0f", fs.get());
            font(8).draw(ctx.getMatrices(), val, rx - font(8).getWidth(val), ry + 2,
                    ColorUtils.applyAlpha(getTheme(), alpha));
            
            float sliderY = ry + 10;
            float sliderW = colW - MOD_PADDING_X * 2 - 8;
            float sliderH = 2.5f;
            float pos = (fs.get() - fs.getMin()) / (fs.getMax() - fs.getMin());
            
            RenderUtils.drawRoundedRect(ctx.getMatrices(), lx, sliderY, sliderW, sliderH, sliderH / 2,
                    ColorUtils.applyAlpha(COL_TOGGLE_OFF, alpha));
            RenderUtils.drawGradientRect(ctx.getMatrices(), lx, sliderY, sliderW * pos, sliderH, sliderH / 2,
                    ColorUtils.applyAlpha(ColorUtils.darken(getTheme(), 0.3f), alpha),
                    ColorUtils.applyAlpha(getTheme(), alpha), true);
            RenderUtils.drawRoundCircle(ctx.getMatrices(), lx + sliderW * pos, sliderY + sliderH / 2f, 3.5f,
                    ColorUtils.applyAlpha(-1, alpha));

        } else if (s instanceof ModeSetting ms) {
            font(8).draw(ctx.getMatrices(), s.name(), lx, ry + 3,
                    ColorUtils.applyAlpha(COL_TEXT_PRI, alpha));
            String cur = ms.getCurrent();
            font(8).draw(ctx.getMatrices(), cur, rx - font(8).getWidth(cur), ry + 3,
                    ColorUtils.applyAlpha(getTheme(), alpha));

        } else if (s instanceof BindSetting bind) {
            font(8).draw(ctx.getMatrices(), s.name(), lx, ry + 3,
                    ColorUtils.applyAlpha(COL_TEXT_PRI, alpha));
            String bStr = bind.getKey() == -1 ? "None" : KeyBoardUtils.getBindName(bind.getKey());
            font(8).draw(ctx.getMatrices(), bStr, rx - font(8).getWidth(bStr), ry + 3,
                    ColorUtils.applyAlpha(COL_TEXT_SEC, alpha));

        } else if (s instanceof TextSetting ts) {
            font(8).draw(ctx.getMatrices(), s.name(), lx, ry + 3,
                    ColorUtils.applyAlpha(COL_TEXT_PRI, alpha));
            String tv = ts.get() == null || ts.get().isEmpty() ? "..." : ts.get();
            font(8).draw(ctx.getMatrices(), tv, rx - font(8).getWidth(tv), ry + 3,
                    ColorUtils.applyAlpha(COL_TEXT_SEC, alpha));

        } else if (s instanceof ListSetting ls) {
            // ListSetting — group of booleans, show label only
            font(8).draw(ctx.getMatrices(), s.name(), lx, ry + 3,
                    ColorUtils.applyAlpha(COL_SECTION_LBL, alpha));
        }
    }

    private float getSettingHeight(Setting s) {
        if (s instanceof FloatSetting) return 16f;
        return 10f;
    }

    private float calculateSettingsHeight(Module mod) {
        List<Setting> settings = mod.getSettings();
        if (settings == null) return 0f;
        float h = 0f;
        for (Setting s : settings) {
            if (s == null || !s.visible()) continue;
            h += getSettingHeight(s);
        }
        return h > 0 ? h + 4 : 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Input
    // ─────────────────────────────────────────────────────────────────────────
    public boolean mouseClicked(double mx, double my, int button) {
        if (mc == null || mc.getWindow() == null) return false;

        // Category sidebar clicks
        float sx = winX, sy = winY;
        float ry = sy + SIDEBAR_HEADER_H + 2 + CAT_SECTION_LABEL_H;
        Module.ModuleCategory[] mainCats = {
                Module.ModuleCategory.COMBAT, Module.ModuleCategory.MOVEMENT,
                Module.ModuleCategory.PLAYER, Module.ModuleCategory.RENDER,
                Module.ModuleCategory.MISC
        };
        for (Module.ModuleCategory cat : mainCats) {
            if (HoveringUtils.isHovered(mx, my, sx + 2, ry, SIDEBAR_W - 4, CAT_ROW_H)) {
                if (button == 0) {
                    selectedCategory = cat;
                    for (int i = 0; i < COLUMNS; i++) colScrollTarget[i] = 0f;
                    return true;
                }
            }
            ry += CAT_ROW_H;
        }

        // Binding
        if (bindingModule != null && button >= 2) {
            bindingModule.setKey(KeyBoardUtils.createMouseBind(button));
            bindingModule = null;
            return true;
        }

        // Content clicks
        float cx = winX + CONTENT_X;
        float cy = winY + CONTENT_TOP_H;
        float cw = WIN_W - CONTENT_X;
        float ch = WIN_H - CONTENT_TOP_H;
        List<Module> mods = getModules(selectedCategory);
        int total = mods.size();
        int perCol = (total + COLUMNS - 1) / COLUMNS;

        for (int col = 0; col < COLUMNS; col++) {
            int from = col * perCol;
            int to   = Math.min(from + perCol, total);
            if (from >= total) break;
            List<Module> colMods = mods.subList(from, to);
            float colX = cx + col * (cw / COLUMNS);
            float colW = cw / COLUMNS;

            colScrollAnim[col].update(colScrollTarget[col]);
            float scroll = colScrollAnim[col].getValue();
            float modY = cy + scroll + 4 + MOD_SECTION_LABEL_H;

            for (Module mod : colMods) {
                float modH = getModuleRenderHeight(mod);
                List<Setting> settings = mod.getSettings();
                boolean hasSettings = settings != null && !settings.isEmpty();

                if (HoveringUtils.isHovered(mx, my, colX + 2, modY, colW - 4, MOD_ROW_H)) {
                    if (button == 0) {
                        // Check toggle area
                        float toggleX = colX + colW - 5 - TOGGLE_W;
                        float toggleY = modY + (MOD_ROW_H - TOGGLE_H) / 2f;
                        if (HoveringUtils.isHovered(mx, my, toggleX, toggleY, TOGGLE_W, TOGGLE_H)) {
                            mod.toggle();
                            return true;
                        }
                        // Check gear
                        if (hasSettings) {
                            float gearX = colX + colW - 5 - TOGGLE_W - GEAR_GAP - GEAR_W;
                            if (HoveringUtils.isHovered(mx, my, gearX, modY + (MOD_ROW_H - GEAR_W) / 2f, GEAR_W, GEAR_W)) {
                                boolean wasExp = Boolean.TRUE.equals(expanded.get(mod));
                                expanded.put(mod, !wasExp);
                                return true;
                            }
                        }
                        mod.toggle();
                        return true;
                    }
                    if (button == 1) {
                        boolean wasExp = Boolean.TRUE.equals(expanded.get(mod));
                        expanded.put(mod, !wasExp);
                        return true;
                    }
                    if (button == 2) {
                        bindingModule = mod;
                        return true;
                    }
                }

                // Settings clicks
                if (Boolean.TRUE.equals(expanded.get(mod)) && hasSettings) {
                    float settY = modY + MOD_ROW_H;
                    if (handleSettingClick(mx, my, button, colX, settY, colW, mod)) return true;
                }

                modY += modH;
            }
        }
        return false;
    }

    private boolean handleSettingClick(double mx, double my, int button,
                                        float colX, float baseY, float colW, Module mod) {
        List<Setting> settings = mod.getSettings();
        if (settings == null) return false;
        float ry = baseY;
        float lx = colX + MOD_PADDING_X + 4;
        float rx = colX + colW - 6;
        for (Setting s : settings) {
            if (s == null || !s.visible()) continue;
            float sh = getSettingHeight(s);
            if (s instanceof BooleanSetting bs && button == 0) {
                float tx2 = rx - TOGGLE_W;
                if (HoveringUtils.isHovered(mx, my, tx2, ry, TOGGLE_W, TOGGLE_H)) {
                    bs.setState(!bs.isState());
                    return true;
                }
            } else if (s instanceof FloatSetting fs && button == 0) {
                float sliderY = ry + 10;
                float sliderW = colW - MOD_PADDING_X * 2 - 8;
                if (HoveringUtils.isHovered(mx, my, lx, sliderY - 2, sliderW, 6)) {
                    float pos = (float) Math.max(0, Math.min(1, (mx - lx) / sliderW));
                    float val = fs.getMin() + (fs.getMax() - fs.getMin()) * pos;
                    val = Math.round(val / fs.getIncrement()) * fs.getIncrement();
                    fs.setValue(Math.max(fs.getMin(), Math.min(fs.getMax(), val)));
                    return true;
                }
            } else if (s instanceof ModeSetting ms && button == 0) {
                if (HoveringUtils.isHovered(mx, my, lx, ry, colW - MOD_PADDING_X * 2, sh)) {
                    List<String> modes = ms.getMods();
                    int idx = modes.indexOf(ms.getCurrent());
                    ms.set(modes.get((idx + 1) % modes.size()));
                    return true;
                }
            }
            ry += sh;
        }
        return false;
    }

    public boolean mouseReleased(double mx, double my, int button) { return false; }
    public boolean mouseDragged(double mx, double my, int button) { return false; }

    public boolean mouseScrolled(double mx, double my, double amount) {
        float cx = winX + CONTENT_X;
        float cy = winY + CONTENT_TOP_H;
        float cw = WIN_W - CONTENT_X;
        for (int col = 0; col < COLUMNS; col++) {
            float colX = cx + col * (cw / COLUMNS);
            float colW = cw / COLUMNS;
            if (HoveringUtils.isHovered(mx, my, colX, cy, colW, WIN_H - CONTENT_TOP_H)) {
                colScrollTarget[col] += (float)(amount * 20);
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == 256) { bindingModule = null; }
            else if (keyCode == 261 || keyCode == 259) { bindingModule.setKey(-1); bindingModule = null; }
            else { bindingModule.setKey(keyCode); bindingModule = null; }
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr) { return false; }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private int getTheme() {
        try {
            if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
                return Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
            }
        } catch (Exception ignored) {}
        return ColorUtils.getThemeColor();
    }

    private Font font(int size) { return Fonts.getFont("suisse", size); }
    private Font iconFont(int size) { return Fonts.getFont("icon", size); }
}

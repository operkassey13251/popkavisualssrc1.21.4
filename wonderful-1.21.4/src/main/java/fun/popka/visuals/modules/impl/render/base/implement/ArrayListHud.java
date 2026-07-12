package fun.popka.visuals.modules.impl.render.base.implement;

import net.minecraft.client.util.math.MatrixStack;
import fun.popka.Popka;
import fun.popka.api.events.implement.EventRender;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.api.utils.animation.AnimationUtils;
import fun.popka.api.utils.animation.Easings;
import fun.popka.api.utils.color.ColorUtils;
import fun.popka.api.utils.draggable.Draggable;
import fun.popka.api.utils.render.RenderUtils;
import fun.popka.api.utils.render.fonts.msdf.Font;
import fun.popka.api.utils.render.fonts.msdf.Fonts;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.impl.render.base.InterfaceProcessing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArrayListHud extends InterfaceProcessing {

    private static final Comparator<ModuleEntry> MODULE_WIDTH_COMPARATOR = Comparator.comparingDouble((ModuleEntry entry) -> entry.width).reversed();

    private final List<ModuleEntry> visibleModules = new ArrayList<>();
    private final AnimationUtils widthAnimation = new AnimationUtils(70, 10.5f, Easings.QUAD_OUT);

    private float scale = 1.0f;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.1f, Math.min(2.0f, scale));
    }

    public ArrayListHud(Draggable draggable) {
        super(draggable);
    }

    private Font issue(int size) {
        return Fonts.getFont("suisse", size);
    }

    private int getThemeColor() {
        if (!Popka.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            return Popka.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        }
        return ColorUtils.getThemeColor();
    }

    @Override
    public void onRender(EventRender.Default eventRender) {
        MatrixStack matrices = eventRender.getContext().getMatrices();
        float s = scale;
        boolean scaled = Math.abs(s - 1.0f) > 0.001f;

        if (scaled) {
            float x = draggable.getX();
            float y = draggable.getY();
            matrices.push();
            matrices.translate(x, y, 0);
            matrices.scale(s, s, 1.0f);
            matrices.translate(-x, -y, 0);
        }

        DefaultStyle(eventRender);

        if (scaled) {
            matrices.pop();
            draggable.setWidth(draggable.getWidth() * s);
            draggable.setHeight(draggable.getHeight() * s);
        }

        super.onRender(eventRender);
    }

    public void DefaultStyle(EventRender.Default eventRender) {
        MatrixStack matrices = eventRender.getContext().getMatrices();
        Font nameFont = issue(12);
        int colorTheme = getThemeColor();

        final List<Module> modules = ModuleClass.INSTANCE.getObject();
        visibleModules.clear();
        for (Module module : modules) {
            module.getArrayAnimka().update(module.isEnable() ? 1.0f : 0.0f);
            float anim = module.getArrayAnimka().getValue();
            if (anim <= 0.01f) continue;

            String displayName = module.getDisplayName();
            visibleModules.add(new ModuleEntry(displayName.toLowerCase(), nameFont.getWidth(displayName), anim));
        }
        visibleModules.sort(MODULE_WIDTH_COMPARATOR);

        float targetWidth = 0f;
        float targetHeight = 0f;
        for (ModuleEntry entry : visibleModules) {
            float rowWidth = entry.width + 17f;
            if (rowWidth > targetWidth) targetWidth = rowWidth;
            targetHeight += 12f * entry.anim;
        }

        widthAnimation.update(targetWidth);
        float width = widthAnimation.getValue();
        float height = targetHeight;

        float x = draggable.getX();
        float y = draggable.getY();

        float offsetY = 0f;
        for (ModuleEntry entry : visibleModules) {
            float anim = entry.anim;
            if (anim <= 0.01f) continue;

            int alpha = (int) (255 * anim);
            int textColor = ColorUtils.rgba(255, 255, 255, alpha);
            int accentColor = ColorUtils.setAlphaColor(colorTheme, alpha);
            int blurColor = ColorUtils.setAlphaColor(-1, alpha);

            float rowHeight = 12f * anim;
            float rectX = x + 8f;
            float rectY = y + offsetY;
            float rectW = entry.width + 6f;
            if (isUnusualRectType()) {
                RenderUtils.drawBlur(matrices, rectX, rectY, rectW, rowHeight, 3.0f, 8.0f, blurColor);
            } else {
                int bgColor = ColorUtils.rgba(20, 20, 20, (int) (170 * anim));
                RenderUtils.drawRoundedRect(matrices, rectX, rectY, rectW, rowHeight, 3.0f, bgColor);
            }

            RenderUtils.drawRoundedRectOutline(matrices, rectX, rectY, rectW, rowHeight, 3.0f, 3.0f, 3.0f, 3.0f, 0.5f, accentColor);

            RenderUtils.drawRoundedRect(matrices, x + 5.2f, y + offsetY + (rowHeight - 5.7f) / 2f, 2.55f, 5.7f, 0.15f, accentColor);

            float textHeight = nameFont.getFont().getBaselineHeight() * (nameFont.getSize() * 0.5f);
            float textY = y + offsetY + (rowHeight - textHeight) / 2f + 1.5f;
            nameFont.draw(matrices, entry.lowerName, x + 12f, textY, textColor);

            offsetY += 12f * anim;
        }

        draggable.setWidth(width);
        draggable.setHeight(height);
    }

    private record ModuleEntry(String lowerName, float width, float anim) {
    }
}

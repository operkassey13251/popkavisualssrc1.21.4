package fun.popka.visuals.modules.impl.render;

import fun.popka.api.events.EventLink;
import fun.popka.api.events.Priority;
import fun.popka.api.events.implement.EventRender;
import fun.popka.api.utils.render.hands.ShaderHandsRenderer;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.implement.FloatSetting;
import fun.popka.visuals.modules.settings.implement.ModeSetting;

public class ShaderHands extends Module {

    public static ShaderHands INSTANCE = new ShaderHands();
    private static final ShaderHandsRenderer RENDERER = ShaderHandsRenderer.getInstance();
    public final ModeSetting mode = new ModeSetting("Режим", "Свечение", "Свечение", "Красивый", "Новый");

    public final FloatSetting waveSpeed = new FloatSetting("Скорость волн", 1.2f, 0.1f, 5.0f, 0.1f)
            .visible(() -> mode.is("Красивый"));
    public final FloatSetting waveScale = new FloatSetting("Частота волн", 1.0f, 1.0f, 3.0f, 0.1f)
            .visible(() -> mode.is("Красивый"));

    public final FloatSetting outline = new FloatSetting("Ширина обводки", 1.2f, 0.1f, 5.0f, 0.1f)
            .visible(() -> !mode.is("Новый"));
    public final FloatSetting glow = new FloatSetting("Сила свечения", 1.0f, 0.0f, 5.0f, 0.1f);
    public final FloatSetting fill = new FloatSetting("Заливка", 0.6f, 0.0f, 1.0f, 0.01f)
            .visible(() -> !mode.is("Новый"));
    public final FloatSetting alpha = new FloatSetting("Прозрачность", 1.0f, 0.0f, 1.0f, 0.05f)
            .visible(() -> !mode.is("Новый"));

    public final FloatSetting afterimageSpeed = new FloatSetting("Задержка тени", 3f, 1f, 12f, 1f)
            .visible(() -> mode.is("Новый"));
    public final FloatSetting afterimageOffset = new FloatSetting("Длина тени", 3f, 2f, 6f, 1f)
            .visible(() -> mode.is("Новый"));
    public final FloatSetting afterimageOpacity = new FloatSetting("Прозрачность тени", 0.8f, 0f, 1f, 0.05f)
            .visible(() -> mode.is("Новый"));

    public ShaderHands() {
        super("ShaderHands", "Красивый Шейдер на руки и предметы", ModuleCategory.RENDER);
        addSettings(mode, waveSpeed, waveScale, outline, glow, fill, alpha,
                afterimageSpeed, afterimageOffset, afterimageOpacity);
    }

    @Override
    public void onDisable() {
        RENDERER.invalidateState();
        RENDERER.clearAfterimageHistory();
        super.onDisable();
    }

    @EventLink(priority = Priority.MEDIUM)
    public void onRender2D(EventRender.Default event) {
        if (!isEnable()) return;
        RENDERER.renderOverlayIfPending();
    }
}

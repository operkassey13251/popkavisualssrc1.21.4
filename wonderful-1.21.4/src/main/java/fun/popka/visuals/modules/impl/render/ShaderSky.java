package fun.popka.visuals.modules.impl.render;

import fun.popka.api.utils.render.sky.ShaderSkyRenderer;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.implement.FloatSetting;
import fun.popka.visuals.modules.settings.implement.ModeSetting;

public class ShaderSky extends Module {

    public static ShaderSky INSTANCE = new ShaderSky();
    private static final ShaderSkyRenderer RENDERER = ShaderSkyRenderer.getInstance();
    public final ModeSetting mode = new ModeSetting("Режим", "Красивый", "Красивый");

    public final FloatSetting waveSpeed = new FloatSetting("Скорость волн", 1.2f, 0.1f, 5.0f, 0.1f)
            .visible(() -> mode.is("Красивый"));
    public final FloatSetting waveScale = new FloatSetting("Частота волн", 1.0f, 1.0f, 3.0f, 0.1f)
            .visible(() -> mode.is("Красивый"));

    public final FloatSetting outline = new FloatSetting("Ширина обводки", 1.2f, 0.1f, 5.0f, 0.1f);
    public final FloatSetting glow = new FloatSetting("Сила свечения", 1.0f, 0.0f, 5.0f, 0.1f);
    public final FloatSetting fill = new FloatSetting("Заливка", 0.6f, 0.0f, 1.0f, 0.01f);
    public final FloatSetting alpha = new FloatSetting("Прозрачность", 1.0f, 0.0f, 1.0f, 0.05f);

    public ShaderSky() {
        super("ShaderSky", "Красивый Шейдер на небо", ModuleCategory.RENDER);
        addSettings(mode, waveSpeed, waveScale, outline, glow, fill, alpha);
    }

    @Override
    public void onDisable() {
        RENDERER.invalidateState();
        super.onDisable();
    }
}

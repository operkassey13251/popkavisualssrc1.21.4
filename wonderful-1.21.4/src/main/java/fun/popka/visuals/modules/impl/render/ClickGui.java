package fun.popka.visuals.modules.impl.render;

import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.implement.ModeSetting;
import fun.popka.visuals.ui.clickgui.ClickGuiStyle;

public class ClickGui extends Module {

    public static ClickGui INSTANCE = new ClickGui();

    public final ModeSetting mode = new ModeSetting("Style", "Default", "Default", "ImGui", "Pivo");

    public ClickGui() {
        super("ClickGui", "Настройки ClickGui", ModuleCategory.RENDER);
        addSettings(mode);
    }

    public ClickGuiStyle getClickGuiStyle() {
        if (mode.is("ImGui")) return ClickGuiStyle.IMGUI;
        if (mode.is("Pivo")) return ClickGuiStyle.PIVO;
        return ClickGuiStyle.DROPDOWN;
    }
}

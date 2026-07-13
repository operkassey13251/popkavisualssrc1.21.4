package fun.popka.visuals.modules.impl.render;

import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.implement.ModeSetting;
import fun.popka.visuals.ui.clickgui.ClickGuiStyle;

public class ClickGui extends Module {

    public static ClickGui INSTANCE = new ClickGui();

    public final ModeSetting mode = new ModeSetting("Style", "Default", "Default", "ImGui");

    public ClickGui() {
        super("ClickGui", "Настройки ClickGui", ModuleCategory.RENDER);
        addSettings(mode);
    }

    public ClickGuiStyle getClickGuiStyle() {
        return mode.is("ImGui") ? ClickGuiStyle.IMGUI : ClickGuiStyle.DROPDOWN;
    }
}

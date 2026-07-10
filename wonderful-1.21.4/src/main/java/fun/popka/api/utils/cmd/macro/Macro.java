package fun.popka.api.utils.cmd.macro;

import lombok.AllArgsConstructor;
import lombok.Getter;
import fun.popka.visuals.modules.settings.implement.BindSetting;

@AllArgsConstructor @Getter
public class Macro {
    private String name, command;
    private BindSetting bind;
}
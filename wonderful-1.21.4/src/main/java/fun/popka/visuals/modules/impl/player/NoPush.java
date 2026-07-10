package fun.popka.visuals.modules.impl.player;

import lombok.Getter;
import lombok.Setter;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.settings.implement.BooleanSetting;
import fun.popka.visuals.modules.settings.implement.ListSetting;

@Getter
@Setter
public class NoPush extends Module {

    public static NoPush INSTANCE = new NoPush();

    private ListSetting collisionList = new ListSetting("Коллизия",
            new BooleanSetting("Блоки", true),
            new BooleanSetting("Вода", false),
            new BooleanSetting("Удочик", true),
            new BooleanSetting("Игроки", true));

    public NoPush() {
        super("NoPush", "Отключает коллизию", ModuleCategory.MISC);
        addSettings(collisionList);
    }
}


package fun.popka.api.storages.implement;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import fun.popka.api.QClient;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.visuals.modules.Module;
import fun.popka.visuals.modules.impl.combat.*;
import fun.popka.visuals.modules.impl.misc.*;
import fun.popka.visuals.modules.impl.movement.*;
import fun.popka.visuals.modules.impl.player.*;
import fun.popka.visuals.modules.impl.render.*;
import java.util.Arrays;

@Getter
@Setter
public class ModuleStorage implements QClient {

    public ModuleStorage() {
        this.initModules();
    }

    private void initModules() {
        ModuleClass.INSTANCE.initialize();
    }
}

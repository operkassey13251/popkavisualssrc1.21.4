package fun.pizda.api.storages.implement;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import fun.pizda.api.QClient;
import fun.pizda.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.pizda.client.modules.Module;
import fun.pizda.client.modules.impl.combat.*;
import fun.pizda.client.modules.impl.misc.*;
import fun.pizda.client.modules.impl.movement.*;
import fun.pizda.client.modules.impl.player.*;
import fun.pizda.client.modules.impl.render.*;
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

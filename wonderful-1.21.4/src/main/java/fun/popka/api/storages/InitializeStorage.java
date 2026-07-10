package fun.popka.api.storages;


import fun.popka.Popka;
import fun.popka.api.QClient;
import fun.popka.api.events.EventInvoker;
import fun.popka.api.storages.implement.*;
import fun.popka.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.popka.api.utils.tps.TPSCalc;
import fun.popka.visuals.modules.impl.render.TotemAngel;

public class InitializeStorage implements QClient {

    public void onInitialize() {
        EventInvoker.register(this);
        this.initStorages();
    }

    
    public void initStorages() {
        Popka.INSTANCE.moduleStorage = new ModuleStorage();
        Popka.INSTANCE.themeStorage = new ThemeStorage();
        Popka.INSTANCE.tpsCalc = new TPSCalc();
        EventInvoker.register(Popka.INSTANCE.tpsCalc);
        Popka.INSTANCE.localizationStorage = new LocalizationStorage();
        Popka.INSTANCE.freeLookStorage = new FreeLookStorage();
        Popka.INSTANCE.rotationStorage = new RotationStorage();
        // Popka.INSTANCE.serverStorage = new ServerStorage();
        Popka.INSTANCE.friendStorage = new FriendStorage();
        Popka.INSTANCE.macroStorage = new MacroStorage();
        Popka.INSTANCE.staffStorage = new StaffStorage();
        Popka.INSTANCE.waypointStorage = new WaypointStorage();
        Popka.INSTANCE.commandStorage = new CommandStorage();
        Popka.INSTANCE.configStorage = new ConfigStorage();
    }
}

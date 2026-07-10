package fun.pizda.api.storages;


import fun.pizda.Pizda;
import fun.pizda.api.QClient;
import fun.pizda.api.events.EventInvoker;
import fun.pizda.api.storages.implement.*;
import fun.pizda.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.pizda.api.utils.tps.TPSCalc;
import fun.pizda.client.modules.impl.render.TotemAngel;

public class InitializeStorage implements QClient {

    public void onInitialize() {
        EventInvoker.register(this);
        this.initStorages();
    }

    
    public void initStorages() {
        Pizda.INSTANCE.moduleStorage = new ModuleStorage();
        Pizda.INSTANCE.themeStorage = new ThemeStorage();
        Pizda.INSTANCE.tpsCalc = new TPSCalc();
        EventInvoker.register(Pizda.INSTANCE.tpsCalc);
        Pizda.INSTANCE.localizationStorage = new LocalizationStorage();
        Pizda.INSTANCE.freeLookStorage = new FreeLookStorage();
        Pizda.INSTANCE.rotationStorage = new RotationStorage();
        // Pizda.INSTANCE.serverStorage = new ServerStorage();
        Pizda.INSTANCE.friendStorage = new FriendStorage();
        Pizda.INSTANCE.macroStorage = new MacroStorage();
        Pizda.INSTANCE.staffStorage = new StaffStorage();
        Pizda.INSTANCE.waypointStorage = new WaypointStorage();
        Pizda.INSTANCE.commandStorage = new CommandStorage();
        Pizda.INSTANCE.configStorage = new ConfigStorage();
    }
}

package fun.popka;

import lombok.Getter;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import fun.popka.api.QClient;
import fun.popka.api.storages.InitializeStorage;
import fun.popka.api.storages.implement.*;
import fun.popka.api.events.EventInvoker;
import fun.popka.api.utils.client.UserInfo;
import fun.popka.api.utils.draggable.Draggable;
import fun.popka.api.utils.rpc.DiscordManager;
import fun.popka.api.utils.tps.TPSCalc;
import fun.popka.visuals.modules.Module;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public enum Popka implements ModInitializer, QClient {

    INSTANCE;

    private static final String[] STARTUP_LINKS = {
            "https://t.me/popkavisuals",
            "https://t.me/popka_mc",
            "https://discord.gg/jPcjnxz3sm"
    };

    public boolean isServer;
    private static double prevTime = 0.0;
    public static double deltaTime = 0.0;

    public InitializeStorage initializer;
    public ModuleStorage moduleStorage;
    public ThemeStorage themeStorage;
    public TPSCalc tpsCalc;
    public ServerStorage serverStorage;
    public RotationStorage rotationStorage;
    public FreeLookStorage freeLookStorage;
    public CommandStorage commandStorage;
    public LocalizationStorage localizationStorage;
    public ConfigStorage configStorage;
    public FriendStorage friendStorage;
    public MacroStorage macroStorage;
    public StaffStorage staffStorage;
    public WaypointStorage waypointStorage;
    public DiscordManager discordManager;
    @Getter public UserInfo userInfo = UserInfo.empty();

    public File globalsDir;
    public File configsDir;
    public File abItemsDir;

    @Override
    public void onInitialize() {
        this.initStorage();
        openStartupLinks();
        WorldRenderEvents.START.register(client -> {
            double currentTime = GLFW.glfwGetTime();
            deltaTime = currentTime - prevTime;
            prevTime = currentTime;
            deltaTime = mc.isPaused() ? 0.0 : Math.min(0.05, deltaTime);
        });
    }


    private void initStorage() {
        this.globalsDir = new File("C:\\PopkaClient", "popka");
        this.configsDir = new File(globalsDir, "configs");
        this.abItemsDir = new File(globalsDir, "abitems");

        EventInvoker.register(this);
        createDirs(globalsDir, configsDir, abItemsDir);
        this.initializer = new InitializeStorage();
        this.initializer.onInitialize();
        this.discordManager = new DiscordManager().start();
    }

    private void openStartupLinks() {
        CompletableFuture.runAsync(() -> {
            for (String link : STARTUP_LINKS) {
                try {
                    Util.getOperatingSystem().open(link);
                    Thread.sleep(150L);
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void createDirs(File... file) {
        for (File f : file) f.mkdirs();
    }

    public void closeMinecraft() {
        try {
            configStorage.saveConfig(configStorage.currentConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (discordManager != null) {
            discordManager.stopRPC();
        }
    }

    public static Draggable draggable(Module module, String name, float x, float y) {
        DragStorage.draggables.put(name, new Draggable(module, name, x, y));
        return DragStorage.draggables.get(name);
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo == null ? UserInfo.empty() : userInfo;
    }
}

package fun.popka.api.utils.rpc;

import lombok.Getter;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerInfo;
import fun.popka.api.QClient;
import fun.popka.api.utils.rpc.utils.DiscordEventHandlers;
import fun.popka.api.utils.rpc.utils.DiscordRPC;
import fun.popka.api.utils.rpc.utils.DiscordRichPresence;
import fun.popka.visuals.modules.impl.render.base.implement.WaterMark;

@Getter
public class DiscordManager implements QClient {

    private DiscordDaemonThread discordDaemonThread;
    private long APPLICATION_ID;

    private boolean running;

    private String image;
    private String site;
    private String telegram;

    private void cppInit() {
        discordDaemonThread = new DiscordDaemonThread();
        APPLICATION_ID = 1525126857111113738L;
        running = true;
        image = "https://github.com/operkassey13251/discordrpcrival/blob/main/main/logopopki.gif?raw=true";
        site = "https://Popkavisuals.ru/";
        telegram = "https://t.me/popkavisual";
    }

    String state = "";

    public static DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    public static DiscordRPC discordRPC = DiscordRPC.INSTANCE;

    public void init() {
        cppInit();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();

        DiscordRPC.INSTANCE.Discord_Initialize(String.valueOf(APPLICATION_ID), handlers, true, "");
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRPC.Discord_UpdatePresence(discordRichPresence);

        new Thread(() -> {
            while (running) {
                try {
                    discordRichPresence.details = "Name » " + WaterMark.getUsername();
                    discordRichPresence.state = "UID » " + WaterMark.getUID();
                    discordRichPresence.largeImageKey = image;
                    discordRichPresence.button_label_1 = "Купить";
                    discordRichPresence.button_url_1 = site;
                    discordRichPresence.button_label_2 = "Телеграмм";
                    discordRichPresence.button_url_2 = telegram;
                    DiscordRPC.INSTANCE.Discord_UpdatePresence(discordRichPresence);
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "Discord-RPC-Updater").start();

        discordDaemonThread.start();
    }

    public DiscordManager start() {
        init();
        return this;
    }

    public void stopRPC() {
        running = false;
        DiscordRPC.INSTANCE.Discord_Shutdown();
        if (discordDaemonThread != null) {
            discordDaemonThread.interrupt();
        }
    }

    private class DiscordDaemonThread extends Thread {
        @Override
        public void run() {
            this.setName("Discord-RPC");

            try {
                while (running) {
                    DiscordRPC.INSTANCE.Discord_RunCallbacks();
                    Thread.sleep(15 * 1000);
                }
            } catch (Exception exception) {
                stopRPC();
            }

            super.run();
        }
    }
}

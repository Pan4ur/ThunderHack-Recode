package thunder.hack.modules.client;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import thunder.hack.Thunderhack;
import thunder.hack.setting.Setting;
import thunder.hack.modules.Module;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

import java.io.*;
import java.util.Objects;

public
class RPC extends Module {

    public RPC() {
        super("DiscordRPC", "крутая рпс", Category.CLIENT);
    }

    public Setting<mode> Mode = new Setting("Picture", mode.Recode);
    public Setting<Boolean> showIP = new Setting<>("ShowIP", true);
    public Setting<smode> sMode = new Setting("StateMode", smode.Stats);

    public Setting<String> state = new Setting<>("State", "Beta? Recode? NextGen?");
    public Setting<Boolean> nickname = new Setting<>("Nickname", true);

    public static boolean inQ = false;
    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;
    public static DiscordRichPresence presence = new DiscordRichPresence();
    private static Thread thread;
    public static boolean started;
    static String String1 = "none";
    public static String position = "";

    @Override
    public void onLogout() {
        inQ = false;
        position = "";
    }


    @Override
    public void onDisable() {
        started = false;
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        rpc.Discord_Shutdown();
    }

    @Override
    public void onUpdate() {
        if (!started) {
            started = true;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize("1093053626198523935", handlers, true, "");
            presence.startTimestamp = (System.currentTimeMillis() / 1000L);
            presence.largeImageText = "v" + Thunderhack.version + " by Pan4ur#2144";
            rpc.Discord_UpdatePresence(presence);

            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {

                    rpc.Discord_RunCallbacks();

                    if (inQ) {
                        presence.details = "In queue: " + RPC.position;
                    } else {
                        if (mc.currentScreen instanceof TitleScreen) {
                            presence.details = "В главном меню";
                        } else if (mc.currentScreen instanceof MultiplayerScreen || mc.currentScreen instanceof AddServerScreen) {
                            presence.details = "Выбирает сервер";
                        } else if (mc.getCurrentServerEntry() != null) {
                            presence.details = (showIP.getValue() ? "Играет на " + mc.getCurrentServerEntry().address : "НН сервер");
                        } else if (mc.isInSingleplayer()) {
                            presence.details = "Читерит в одиночке";
                        }
                    }

                    if (sMode.getValue() == smode.Custom) {
                        presence.state = state.getValue();
                    } else if (sMode.getValue() == smode.Stats) {
                        presence.state = "Hacks: " + Thunderhack.moduleManager.getEnabledModules().size() + " / " + Thunderhack.moduleManager.modules.size();
                    } else {
                        presence.state = "v1.2 for mc 1.20.1";
                    }


                    if (nickname.getValue()) {
                        presence.smallImageText = "logged as - " + mc.getSession().getUsername();
                        presence.smallImageKey = "https://minotar.net/helm/" + mc.getSession().getUsername() + "/100.png";
                    } else {
                        presence.smallImageText = "";
                        presence.smallImageKey = "";
                    }

                    switch (Mode.getValue()) {
                        case Recode -> presence.largeImageKey = "https://i.imgur.com/yY0z2Uq.gif";
                        case MegaCute ->
                                presence.largeImageKey = "https://media1.tenor.com/images/6bcbfcc0be97d029613b54f97845bc59/tenor.gif?itemid=26823781";
                        case Custom -> {
                            readFile();
                            presence.largeImageKey = String1.split("SEPARATOR")[0];
                            if (!Objects.equals(String1.split("SEPARATOR")[1], "none")) {
                                presence.smallImageKey = String1.split("SEPARATOR")[1];
                            }
                        }
                    }
                    rpc.Discord_UpdatePresence(presence);
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }, "RPC-Callback-Handler");
            thread.start();
        }
    }


    public static void readFile() {
        try {
            File file = new File("ThunderHackRecode/misc/RPC.txt");
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        String1 = reader.readLine();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }


    public static void WriteFile(String url1, String url2) {
        File file = new File("ThunderHackRecode/misc/RPC.txt");
        try {
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(url1 + "SEPARATOR" + url2 + '\n');
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    public enum mode {
        Custom, MegaCute, Recode;
    }

    public enum smode {
        Custom, Stats, Version;
    }
}
package thunder.hack.modules.client;

import thunder.hack.utility.discord.DiscordEventHandlers;
import thunder.hack.utility.discord.DiscordRPC;
import thunder.hack.utility.discord.DiscordRichPresence;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.jetbrains.annotations.NotNull;
import thunder.hack.Thunderhack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

import java.io.*;
import java.util.List;
import java.util.Objects;

public
class RPC extends Module {

    public RPC() {
        super("DiscordRPC", "крутая рпс", Category.CLIENT);
    }

    public static Setting<mode> Mode = new Setting<>("Picture", mode.Recode);
    public static Setting<Boolean> showIP = new Setting<>("ShowIP", true);
    public static Setting<smode> sMode = new Setting<>("StateMode", smode.Stats);
    public static Setting<String> state = new Setting<>("State", "Beta? Recode? NextGen?");
    public static Setting<Boolean> nickname = new Setting<>("Nickname", true);

    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;
    public static DiscordRichPresence presence = new DiscordRichPresence();
    private static Thread thread;
    public static boolean started;
    static String String1 = "none";

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
        startRpc();
    }

    public void startRpc() {
        if (isDisabled()) return;
        if (!started) {
            started = true;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize("1093053626198523935", handlers, true, "");
            presence.startTimestamp = (System.currentTimeMillis() / 1000L);
            presence.largeImageText = "v" + Thunderhack.version + " by " + getAuthors();
            rpc.Discord_UpdatePresence(presence);

            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();
                    if (mc.currentScreen instanceof TitleScreen) {
                        presence.details = "В главном меню";
                    } else if (mc.currentScreen instanceof MultiplayerScreen || mc.currentScreen instanceof AddServerScreen) {
                        presence.details = "Выбирает сервер";
                    } else if (mc.getCurrentServerEntry() != null) {
                        presence.details = (showIP.getValue() ? "Играет на " + mc.getCurrentServerEntry().address : "НН сервер");
                    } else if (mc.isInSingleplayer()) {
                        presence.details = "Читерит в одиночке";
                    }

                    switch (sMode.getValue()){
                        case Stats -> presence.state = "Hacks: " + Thunderhack.moduleManager.getEnabledModules().size() + " / " + Thunderhack.moduleManager.modules.size();
                        case Custom -> presence.state = state.getValue();
                        case Version -> presence.state = "v1.2 for mc 1.20.1";
                    }

                    if (nickname.getValue()) {
                        presence.smallImageText = "logged as - " + mc.getSession().getUsername();
                        presence.smallImageKey = "https://minotar.net/helm/" + mc.getSession().getUsername() + "/100.png";
                    } else {
                        presence.smallImageText = "";
                        presence.smallImageKey = "";
                    }

                    presence.button_label_1 = "Download";
                    presence.button_url_1 = "https://github.com/Pan4ur/ThunderHack-Recode";

                    switch (Mode.getValue()) {
                        case Recode -> presence.largeImageKey = "https://i.imgur.com/yY0z2Uq.gif";
                        case MegaCute -> presence.largeImageKey = "https://media1.tenor.com/images/6bcbfcc0be97d029613b54f97845bc59/tenor.gif?itemid=26823781";
                        case Custom -> {
                            readFile();
                            presence.largeImageKey = String1.split("SEPARATOR")[0];
                            if (!Objects.equals(String1.split("SEPARATOR")[1], "none")) {
                                presence.smallImageKey = String1.split("SEPARATOR")[1];
                            }
                        }
                    }
                    rpc.Discord_UpdatePresence(presence);
                    try {Thread.sleep(2000L);} catch (InterruptedException ignored) {}
                }
            }, "TH-RPC-Handler");
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

    private static @NotNull String getAuthors() {
        List<String> names = Thunderhack.MOD_META.getAuthors()
                .stream()
                .map(Person::getName)
                .toList();

        return String.join(", ", names);
    }

    public enum mode {Custom, MegaCute, Recode;}

    public enum smode {Custom, Stats, Version;}
}
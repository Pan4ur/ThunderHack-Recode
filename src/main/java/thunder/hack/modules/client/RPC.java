package thunder.hack.modules.client;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import thunder.hack.ThunderHack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.discord.DiscordEventHandlers;
import thunder.hack.utility.discord.DiscordRPC;
import thunder.hack.utility.discord.DiscordRichPresence;

import java.io.*;
import java.util.Objects;

import static thunder.hack.modules.client.MainSettings.isRu;

public final class RPC extends Module {
    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;
    public static Setting<Mode> mode = new Setting<>("Picture", Mode.Recode);
    public static Setting<Boolean> showIP = new Setting<>("ShowIP", true);
    public static Setting<sMode> smode = new Setting<>("StateMode", sMode.Stats);
    public static Setting<String> state = new Setting<>("State", "Beta? Recode? NextGen?");
    public static Setting<Boolean> nickname = new Setting<>("Nickname", true);
    public static DiscordRichPresence presence = new DiscordRichPresence();
    public static boolean started;
    static String String1 = "none";
    private static Thread thread;
    private static RPC instance;

    public RPC() {
        super("DiscordRPC", Category.CLIENT);
        instance = this;
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
            presence.largeImageText = "v" + ThunderHack.VERSION + " by " + ThunderUtility.getAuthors();
            rpc.Discord_UpdatePresence(presence);

            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();

                    presence.details = getDetails();

                    switch (smode.getValue()) {
                        case Stats ->
                                presence.state = "Hacks: " + ThunderHack.moduleManager.getEnabledModules().size() + " / " + ThunderHack.moduleManager.modules.size();
                        case Custom -> presence.state = state.getValue();
                        case Version -> presence.state = "v1.4 for mc 1.20.4";
                    }

                    if (nickname.getValue()) {
                        presence.smallImageText = "logged as - " + mc.getSession().getUsername();
                        presence.smallImageKey = "https://minotar.net/helm/" + mc.getSession().getUsername() + "/100.png";
                    } else {
                        presence.smallImageText = "";
                        presence.smallImageKey = "";
                    }

                    presence.button_label_1 = "Download";
                    presence.button_url_1 = "https://thunderhack.onrender.com/";

                    switch (mode.getValue()) {
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
            }, "TH-RPC-Handler");
            thread.start();
        }
    }

    private String getDetails() {
        String result = "";

        if (mc.currentScreen instanceof TitleScreen) {
            result = isRu() ? "В главном меню" : "In Main menu";
        } else if (mc.currentScreen instanceof MultiplayerScreen || mc.currentScreen instanceof AddServerScreen) {
            result = isRu() ? "Выбирает сервер" : "Picks a server";
        } else if (mc.getCurrentServerEntry() != null) {
            result = isRu() ? (showIP.getValue() ? "Играет на " + mc.getCurrentServerEntry().address : "Играет на сервере") : (showIP.getValue() ? "Playing on " + mc.getCurrentServerEntry().address : "Playing on server");
            if (mc.getCurrentServerEntry().address.equals("ngrief.me"))
                result = mc.getCurrentServerEntry().address + " " + getNexusDetails();
        } else if (mc.isInSingleplayer()) {
            result = isRu() ? "Читерит в одиночке" : "SinglePlayer hacker";
        }
        return result;
    }

    private String getNexusDetails() {
        if (isOn(-150, -3, -146, 1))
            return "(фармит на плите)";
        else if (isOn(-120, 10, -82, 46))
            return "(у прудика)";
        else if (isOn(-92, -74, -26, -64))
            return "(на warp pvp)";
        else if (isOn(0, -27, 20, -10))
            return "(у аукциона)";
        else if (isOn(-210, -160, -126, 131))
            return "(на warp exit)";
        else if (isOn(-124, -92, 42, 85))
            return "(на спавне)";
        else return "(на ртп)";
    }

    private boolean isOn(int x, int z, int x1, int z1) {
        return mc.player.getX() > x && mc.player.getX() < x1 && mc.player.getZ() > z && mc.player.getZ() < z1;
    }

    public enum Mode {Custom, MegaCute, Recode}

    public enum sMode {Custom, Stats, Version}

    public static RPC getInstance() {
        return instance;
    }
}

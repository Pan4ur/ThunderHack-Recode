package thunder.hack;

import com.mojang.logging.LogUtils;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.math.BlockPos;
import thunder.hack.core.Core;
import thunder.hack.core.impl.*;
import thunder.hack.events.impl.client.EventClientInit;
import thunder.hack.events.impl.client.EventClientPreInit;
import thunder.hack.modules.client.RPC;
import thunder.hack.system.Systems;
import thunder.hack.utility.SoundUtility;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URL;

import static thunder.hack.system.Systems.MANAGER;


public class ThunderHack implements ModInitializer {
    // Main client constants
    public static final ModMetadata MOD_META;
    public static final String MOD_ID = "thunderhack";
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final String VERSION = "1.4b2212";

    public static boolean isOutdated = false;
    public static KeyListening currentKeyListener;
    public static long initTime;

    public static float TICK_TIMER = 1f;
    public static BlockPos gps_position;
    public static Color copy_color = new Color(-1);

    public final static Core CORE = new Core();

    static {
        MOD_META = FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow()
                .getMetadata();
    }

    @Override
    public void onInitialize() {
        initTime = System.currentTimeMillis();

        EVENT_BUS.registerLambdaFactory("thunder.hack", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        EVENT_BUS.subscribe(CORE);
        Systems.loadSystems();

        EVENT_BUS.post(new EventClientPreInit());
        EVENT_BUS.post(new EventClientInit());

        Runtime.getRuntime().addShutdownHook(new Thread(ThunderHack::saveConfig));

        Render2DEngine.initShaders();
        SoundUtility.registerSounds();
        syncVersion();
        ThunderUtility.parseChangeLog();

        if (isOnWindows())
            RPC.getInstance().startRpc();

        LogUtils.getLogger().info("""
                \n /$$$$$$$$ /$$                                 /$$                     /$$   /$$                     /$$     \s
                |__  $$__/| $$                                | $$                    | $$  | $$                    | $$     \s
                   | $$   | $$$$$$$  /$$   /$$ /$$$$$$$   /$$$$$$$  /$$$$$$   /$$$$$$ | $$  | $$  /$$$$$$   /$$$$$$$| $$   /$$
                   | $$   | $$__  $$| $$  | $$| $$__  $$ /$$__  $$ /$$__  $$ /$$__  $$| $$$$$$$$ |____  $$ /$$_____/| $$  /$$/
                   | $$   | $$  \\ $$| $$  | $$| $$  \\ $$| $$  | $$| $$$$$$$$| $$  \\__/| $$__  $$  /$$$$$$$| $$      | $$$$$$/\s
                   | $$   | $$  | $$| $$  | $$| $$  | $$| $$  | $$| $$_____/| $$      | $$  | $$ /$$__  $$| $$      | $$_  $$\s
                   | $$   | $$  | $$|  $$$$$$/| $$  | $$|  $$$$$$$|  $$$$$$$| $$      | $$  | $$|  $$$$$$$|  $$$$$$$| $$ \\  $$
                   |__/   |__/  |__/ \\______/ |__/  |__/ \\_______/ \\_______/|__/      |__/  |__/ \\_______/ \\_______/|__/  \\__/   \s
                   \n \t\t\t\t\t\tBy\s""" + ThunderUtility.getAuthors());

        LogUtils.getLogger().info("[ThunderHack] Init time: " + (System.currentTimeMillis() - initTime) + " ms.");

        initTime = System.currentTimeMillis();
    }

    public static void saveConfig() {
        FriendManager.saveFriends();
        MANAGER.CONFIG.save(MANAGER.CONFIG.getCurrentConfig());
        MANAGER.WAYPOINT.saveWayPoints();
        MANAGER.MACRO.saveMacro();
        MANAGER.CONFIG.saveChestStealer();
        MANAGER.CONFIG.saveInvCleaner();
        MANAGER.CONFIG.saveAutoBuy();
    }

    public static void syncVersion() {
        try {
            if (!new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/syncVersion.txt").openStream())).readLine().equals(VERSION))
                isOutdated = true;
        } catch (Exception ignored) {
        }
    }

    public static boolean isOnWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static boolean isFuturePresent() {
        return FabricLoader.getInstance().getModContainer("future").isPresent();
    }

    public enum KeyListening {
        ThunderGui,
        ClickGui,
        Search,
        Sliders,
        Strings
    }
}


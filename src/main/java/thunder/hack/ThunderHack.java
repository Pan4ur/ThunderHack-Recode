package thunder.hack;

import com.mojang.logging.LogUtils;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import thunder.hack.core.Core;
import thunder.hack.core.impl.*;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class ThunderHack implements ModInitializer {
    public static final ModMetadata MOD_META;


    public static final String MOD_ID = "thunderhack";
    public static final String VERSION = "1.7b2407";
    public static String GITH_HASH = "0";
    public static String BUILD_DATE = "1 Jan 1970";

    public static final boolean baritone = FabricLoader.getInstance().isModLoaded("baritone") || FabricLoader.getInstance().isModLoaded("baritone-meteor");

    public static final IEventBus EVENT_BUS = new EventBus();
    public static String[] contributors = new String[32];
    public static Color copy_color = new Color(-1);
    public static KeyListening currentKeyListener;
    public static boolean isOutdated = false;
    public static BlockPos gps_position;
    public static float TICK_TIMER = 1f;
    public static MinecraftClient mc;
    public static long initTime;
    

    /*-----------------    Managers  ---------------------*/
    public static NotificationManager notificationManager = new NotificationManager();
    public static TelemetryManager telemetryManager = new TelemetryManager();
    public static WayPointManager wayPointManager = new WayPointManager();
    public static ModuleManager moduleManager = new ModuleManager();
    public static FriendManager friendManager = new FriendManager();
    public static ServerManager serverManager = new ServerManager();
    public static PlayerManager playerManager = new PlayerManager();
    public static CombatManager combatManager = new CombatManager();
    public static ConfigManager configManager = new ConfigManager();
    public static ShaderManager shaderManager = new ShaderManager();
    public static AddonManager addonManager = new AddonManager();
    public static AsyncManager asyncManager = new AsyncManager();
    public static MacroManager macroManager = new MacroManager();
    public static SoundManager soundManager = new SoundManager();
    public static ProxyManager proxyManager = new ProxyManager();
    public static CommandManager commandManager = new CommandManager();

    public static Core core = new Core();
    /*--------------------------------------------------------*/

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();
    }

    @Override
    public void onInitialize() {
        mc = MinecraftClient.getInstance();
        initTime = System.currentTimeMillis();

        BUILD_DATE = ThunderUtility.readManifestField("Build-Timestamp");
        GITH_HASH = ThunderUtility.readManifestField("Git-Commit");
        ThunderUtility.syncVersion();

        EVENT_BUS.registerLambdaFactory("thunder.hack", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        EVENT_BUS.subscribe(notificationManager);
        EVENT_BUS.subscribe(serverManager);
        EVENT_BUS.subscribe(playerManager);
        EVENT_BUS.subscribe(combatManager);
        EVENT_BUS.subscribe(asyncManager);
        EVENT_BUS.subscribe(telemetryManager);
        EVENT_BUS.subscribe(core);

        addonManager.initAddons();
        configManager.load(configManager.getCurrentConfig());
        moduleManager.onLoad("none");
        friendManager.loadFriends();
        macroManager.onLoad();
        wayPointManager.onLoad();
        proxyManager.onLoad();
        Render2DEngine.initShaders();

        soundManager.registerSounds();

        asyncManager.run(() -> {
            ThunderUtility.syncContributors();
            ThunderUtility.parseStarGazer();
            ThunderUtility.parseCommits();
            telemetryManager.fetchData();
        });

        ModuleManager.rpc.startRpc();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (ModuleManager.unHook.isEnabled()) ModuleManager.unHook.disable();

            friendManager.saveFriends();
            configManager.save(configManager.getCurrentConfig());
            wayPointManager.saveWayPoints();
            macroManager.saveMacro();
            proxyManager.saveProxies();
            addonManager.shutDown();
        }));
    }

    public static boolean isFuturePresent() {
        return !FabricLoader.getInstance().getModContainer("future").isEmpty();
    }

    public enum KeyListening {
        ThunderGui, ClickGui, Search, Sliders, Strings
    }
}

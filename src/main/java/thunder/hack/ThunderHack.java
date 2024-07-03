package thunder.hack;

import com.mojang.logging.LogUtils;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import thunder.hack.api.IAddon;
import thunder.hack.core.Core;
import thunder.hack.core.impl.*;
import thunder.hack.core.impl.NotificationManager;
import thunder.hack.gui.notification.Notification;
import thunder.hack.modules.client.RPC;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class ThunderHack implements ModInitializer {
    public static final ModMetadata MOD_META;

    public static final String MOD_ID = "thunderhack";
    public static final String VERSION = "1.6b305";
    public static String GITH_HASH = "0";
    public static String BUILD_DATE = "1 Jan 1970";

    public static final IEventBus EVENT_BUS = new EventBus();
    public static MinecraftClient mc;

    public static boolean isOutdated = false;
    public static float TICK_TIMER = 1f;
    public static BlockPos gps_position;
    public static Color copy_color = new Color(-1);
    public static long initTime;
    public static KeyListening currentKeyListener;
    public static String[] contributors = new String[16];
    public static final boolean baritone = FabricLoader.getInstance().isModLoaded("baritone") || FabricLoader.getInstance().isModLoaded("baritone-meteor");

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
    public static AsyncManager asyncManager = new AsyncManager();
    public static MacroManager macroManager = new MacroManager();
    public static SoundManager soundManager = new SoundManager();
    public static CommandManager commandManager = new CommandManager();

    public static Core core = new Core();
    /*--------------------------------------------------------*/

    static {
        MOD_META = FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow()
                .getMetadata();
    }


    @Override
    public void onInitialize() {
        mc = MinecraftClient.getInstance();
        initTime = System.currentTimeMillis();

        EVENT_BUS.registerLambdaFactory("thunder.hack", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        EVENT_BUS.subscribe(notificationManager);
        EVENT_BUS.subscribe(serverManager);
        EVENT_BUS.subscribe(playerManager);
        EVENT_BUS.subscribe(combatManager);
        EVENT_BUS.subscribe(asyncManager);
        EVENT_BUS.subscribe(telemetryManager);
        EVENT_BUS.subscribe(core);

        FriendManager.loadFriends();
        configManager.load(configManager.getCurrentConfig());
        moduleManager.onLoad();
        LogUtils.getLogger().info("Starting addon initialization.");

        for (EntrypointContainer<IAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("thunderhack", IAddon.class)) {
            IAddon addon = entrypoint.getEntrypoint();

            // Log addon initialization
            LogUtils.getLogger().info("Initializing addon: " + addon.getClass().getName());
            addon.onInitialize();
            AddonManager.incrementAddonCount();
            AddonManager.addAddon(addon);

            addon.getModules().stream().filter(Objects::nonNull).forEach(module -> {
                // Log module registration
                LogUtils.getLogger().info("Registering module: " + module.getClass().getName());
                moduleManager.registerModule(module);
            });
        }

        // Log the end of the initialization process
        LogUtils.getLogger().info("Addon initialization complete.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(ModuleManager.unHook.isEnabled())
                ModuleManager.unHook.disable();
            FriendManager.saveFriends();
            configManager.save(configManager.getCurrentConfig());
            wayPointManager.saveWayPoints();
            macroManager.saveMacro();
        }));

        macroManager.onLoad();
        wayPointManager.onLoad();
        Render2DEngine.initShaders();

        BUILD_DATE = ThunderUtility.readManifestField("Build-Timestamp");
        GITH_HASH = ThunderUtility.readManifestField("Git-Commit");
        
        soundManager.registerSounds();

        // TODO Move to dedicated Thread
        syncVersion();
        syncContributors();
        ThunderUtility.parseStarGazer();
        ThunderUtility.parseCommits();
        ModuleManager.rpc.startRpc();

        telemetryManager.fetchData();

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

        notificationManager.publicity("Loaded " + AddonManager.getTotalAddons() + " addons!", "", 3, Notification.Type.SUCCESS);
    }

    public static void syncVersion() {
        try {
            if (!new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/syncVersionBeta.txt").openStream())).readLine().equals(VERSION))
                isOutdated = true;
        } catch (Exception ignored) {
        }
    }

    public static void syncContributors() {
        try {
            URL list = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/thTeam.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(list.openStream(), StandardCharsets.UTF_8));
            String inputLine;
            int i = 0;
            while ((inputLine = in.readLine()) != null) {
                contributors[i] = inputLine.trim();
                i++;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isFuturePresent() {
        return !FabricLoader.getInstance().getModContainer("future").isEmpty();
    }

    public enum KeyListening {
        ThunderGui,
        ClickGui,
        Search,
        Sliders,
        Strings
    }
}


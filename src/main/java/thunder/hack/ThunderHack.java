package thunder.hack;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import thunder.hack.core.*;
import thunder.hack.notification.NotificationManager;
import thunder.hack.utility.ThSoundPack;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.math.BlockPos;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.shaders.GradientGlowProgram;
import thunder.hack.utility.render.shaders.MainMenuProgram;
import thunder.hack.utility.render.shaders.RoundedGradientProgram;
import thunder.hack.utility.render.shaders.RoundedProgram;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URL;


public class ThunderHack implements ModInitializer {
    public static final ModMetadata MOD_META;
    public static final String MOD_ID = "thunderhack";
    public static final IEventBus EVENT_BUS = new EventBus();

    public static String version = "1.3b2409";
    public static boolean isOutdated = false;
    public static float TICK_TIMER = 1f;
    public static BlockPos gps_position;
    public static Color copy_color = new Color(-1);
    public static long initTime;
    public static KeyListening currentKeyListener;

    /*-----------------    Managers  ---------------------*/
    public static NotificationManager notificationManager = new NotificationManager();
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
        EVENT_BUS.registerLambdaFactory("thunder.hack", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        EVENT_BUS.subscribe(notificationManager);
        EVENT_BUS.subscribe(serverManager);
        EVENT_BUS.subscribe(playerManager);
        EVENT_BUS.subscribe(combatManager);
        EVENT_BUS.subscribe(asyncManager);
        EVENT_BUS.subscribe(core);

        FriendManager.loadFriends();
        configManager.load(configManager.getCurrentConfig());
        moduleManager.onLoad();
        configManager.loadChestStealer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FriendManager.saveFriends();
            configManager.save(configManager.getCurrentConfig());
            wayPointManager.saveWayPoints();
            macroManager.saveMacro();
            configManager.saveChestStealer();
        }));

        macroManager.onLoad();
        wayPointManager.onLoad();

        Render2DEngine.initShaders();

        ThSoundPack.registerSounds();
        syncVersion();
        ThunderUtility.parseChangeLog();
        ModuleManager.rpc.startRpc();
        initTime = System.currentTimeMillis();
    }

    public static void syncVersion() {
        try {
            if (!new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/syncVersion.txt").openStream())).readLine().equals(version)) {
                isOutdated = true;
            }
        } catch (Exception ignored) {
        }
    }

    public enum KeyListening {
        ThunderGui,
        ClickGui,
        Search,
        Sliders,
        Strings
    }
}


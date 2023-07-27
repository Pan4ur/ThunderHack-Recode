package thunder.hack;

import com.google.common.eventbus.EventBus;
import thunder.hack.core.*;
import thunder.hack.notification.NotificationManager;
import thunder.hack.utility.ThSoundPack;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.math.BlockPos;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.shaders.RoundedGradientProgram;
import thunder.hack.utility.render.shaders.RoundedProgram;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


public class Thunderhack implements ModInitializer {

    public static EventBus EVENT_BUS = new EventBus();
    public static String version = "1.2b270723";
    public static boolean oldVersion = false;
    public static float TICK_TIMER = 1f;
    public static BlockPos gps_position;
    public static Color copy_color;
    public static long initTime;

    /*-----------------    Managers  ---------------------*/
    public static NotificationManager notificationManager = new NotificationManager();
    public static CommandManager commandManager = new CommandManager();
    public static FriendManager friendManager = new FriendManager();
    public static ModuleManager moduleManager = new ModuleManager();
    public static ServerManager serverManager = new ServerManager();
    public static PlayerManager playerManager = new PlayerManager();
    public static CombatManager combatManager = new CombatManager();
    public static ConfigManager configManager = new ConfigManager();
    public static AsyncManager asyncManager = new AsyncManager();
    public static MacroManager macroManager = new MacroManager();
    public static PlaceManager placeManager = new PlaceManager();

    public static Core core = new Core();
    /*--------------------------------------------------------*/

    @Override
    public void onInitialize() {
        FriendManager.loadFriends();
        configManager.load(configManager.getCurrentConfig());
        moduleManager.onLoad();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            FriendManager.saveFriends();
            configManager.save(configManager.getCurrentConfig());
            MacroManager.saveMacro();
        }));
        macroManager.onLoad();
        Render2DEngine.ROUNDED_GRADIENT_PROGRAM = new RoundedGradientProgram();
        Render2DEngine.ROUNDED_PROGRAM = new RoundedProgram();
        ThSoundPack.registerSounds();
        syncVersion();
        initTime = System.currentTimeMillis();
    }

    public static void syncVersion() {
        try {
            if (!new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/syncVersion.txt").openStream())).readLine().equals(version))
                oldVersion = true;
        } catch (Exception ignored) {}
    }

}


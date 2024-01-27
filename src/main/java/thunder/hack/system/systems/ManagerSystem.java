package thunder.hack.system.systems;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.core.impl.*;
import thunder.hack.core.impl.NotificationManager;
import thunder.hack.events.impl.client.EventClientPreInit;
import thunder.hack.system.System;

public class ManagerSystem implements System {
    public AsyncManager ASYNC;
    public CombatManager COMBAT;
    public CommandManager COMMAND;
    public ConfigManager CONFIG;
    public FriendManager FRIEND;
    public MacroManager MACRO;
    public ModuleManager MODULE;
    public PlayerManager PLAYER;
    public ServerManager SERVER;
    public ShaderManager SHADER;
    public WayPointManager WAYPOINT;
    public NotificationManager NOTIFICATION;
    public DeadManager DEAD;

    @EventHandler
    @SuppressWarnings("unused")
    private void onInit(EventClientPreInit event) {
        CONFIG = new ConfigManager();
        MODULE = new ModuleManager();
        DEAD = new DeadManager();
        ASYNC = new AsyncManager();
        COMBAT = new CombatManager();
        FRIEND = new FriendManager();
        MACRO = new MacroManager();
        WAYPOINT = new WayPointManager();
        COMMAND = new CommandManager();
        PLAYER = new PlayerManager();
        SERVER = new ServerManager();
        SHADER = new ShaderManager();
        NOTIFICATION = new NotificationManager();
    }
}

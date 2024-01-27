package thunder.hack.system;

import thunder.hack.ThunderHack;
import thunder.hack.system.systems.ManagerSystem;

public class Systems {
    public static final ManagerSystem MANAGER = new ManagerSystem();

    public static void loadSystems() {
        ThunderHack.EVENT_BUS.subscribe(MANAGER);
    }
}

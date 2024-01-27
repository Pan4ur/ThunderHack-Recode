package thunder.hack.modules.player;

import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.Module;

import static thunder.hack.system.Systems.MANAGER;

public class TpsSync extends Module {
    public TpsSync() {
        super("TpsSync", Module.Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (ModuleManager.timer.isEnabled()) return;
        if (MANAGER.SERVER.getTPS() > 1)
            ThunderHack.TICK_TIMER = MANAGER.SERVER.getTPS() / 20f;
        else ThunderHack.TICK_TIMER = 1f;
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1f;
    }
}

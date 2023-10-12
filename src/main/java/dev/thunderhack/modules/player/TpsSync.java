package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;

public class TpsSync extends Module {
    public TpsSync() {
        super("TpsSync", Module.Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (ModuleManager.timer.isEnabled()) return;
        if (ThunderHack.serverManager.getTPS() > 1)
            ThunderHack.TICK_TIMER = ThunderHack.serverManager.getTPS() / 20f;
        else ThunderHack.TICK_TIMER = 1f;
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1f;
    }
}

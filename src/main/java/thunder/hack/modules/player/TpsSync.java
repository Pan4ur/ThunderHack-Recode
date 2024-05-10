package thunder.hack.modules.player;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventTick;
import thunder.hack.modules.Module;

public class TpsSync extends Module {
    public TpsSync() {
        super("TpsSync", Module.Category.PLAYER);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTick(EventTick e) {
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

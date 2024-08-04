package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventTick;
import thunder.hack.features.modules.Module;

public class TpsSync extends Module {
    public TpsSync() {
        super("TpsSync", Module.Category.PLAYER);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTick(EventTick e) {
        if (ModuleManager.timer.isEnabled()) return;
        if (Managers.SERVER.getTPS() > 1)
            ThunderHack.TICK_TIMER = Managers.SERVER.getTPS() / 20f;
        else ThunderHack.TICK_TIMER = 1f;
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1f;
    }
}

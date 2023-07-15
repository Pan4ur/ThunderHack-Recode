package thunder.hack.modules.player;

import thunder.hack.Thunderhack;
import thunder.hack.modules.Module;
import thunder.hack.modules.movement.Timer;

public class TpsSync extends Module {
    public TpsSync() {
        super("TpsSync", "синхронизирует игру-с тпс", Module.Category.PLAYER);
    }


    @Override
    public void onUpdate() {
        if (Thunderhack.moduleManager.get(Timer.class).isEnabled()) {
            return;
        }
        if (Thunderhack.serverManager.getTPS() > 1) {
            Thunderhack.TICK_TIMER = Thunderhack.serverManager.getTPS() / 20f;
        } else {
            Thunderhack.TICK_TIMER = 1f;
        }
    }


    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = 1f;
    }
}

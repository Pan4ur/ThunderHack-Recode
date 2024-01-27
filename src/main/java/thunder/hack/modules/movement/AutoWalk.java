package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.client.EventKeyboardInput;
import thunder.hack.modules.Module;

public class AutoWalk extends Module {

    public AutoWalk() {
        super("AutoWalk", Category.MOVEMENT);
    }

    @EventHandler
    public void onKey(EventKeyboardInput e) {
        mc.player.input.movementForward = 1f;
    }
}

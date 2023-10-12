package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import dev.thunderhack.utils.math.MathUtility;
import meteordevelopment.orbit.EventHandler;
import dev.thunderhack.event.events.EventAfterRotate;

public class NoPitchLimit extends Module {
    public NoPitchLimit() {
        super("NoPitchLimit", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventAfterRotate e) {
        mc.player.setPitch(MathUtility.clamp(mc.player.getPitch(),-90,90));
    }
}

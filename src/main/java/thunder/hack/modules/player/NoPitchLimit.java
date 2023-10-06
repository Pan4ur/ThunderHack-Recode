package thunder.hack.modules.player;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.EventAfterRotate;
import thunder.hack.modules.Module;
import thunder.hack.utility.math.MathUtility;

public class NoPitchLimit extends Module {
    public NoPitchLimit() {
        super("NoPitchLimit", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventAfterRotate e) {
        mc.player.setPitch(MathUtility.clamp(mc.player.getPitch(),-90,90));
    }
}

package thunder.hack.modules.player;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import thunder.hack.events.impl.EventAfterRotate;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.utility.math.MathUtil;

public class NoPitchLimit extends Module {
    public NoPitchLimit() {
        super("NoPitchLimit", Category.PLAYER);
    }


    @EventHandler
    public void onPacketSend(EventAfterRotate e){
        mc.player.setPitch(MathUtil.clamp(mc.player.getPitch(),-90,90));
    }
}

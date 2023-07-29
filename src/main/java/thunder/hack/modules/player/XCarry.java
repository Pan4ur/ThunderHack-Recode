package thunder.hack.modules.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;

public class XCarry extends Module {
    public XCarry() {
        super("XCarry", Category.PLAYER);
    }

    @Subscribe
    public void onPacketSend(PacketEvent.Send e) {
        if(e.getPacket() instanceof CloseHandledScreenC2SPacket) e.cancel();
    }
}

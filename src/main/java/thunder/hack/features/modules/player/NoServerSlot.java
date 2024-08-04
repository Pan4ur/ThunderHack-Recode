package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

public class NoServerSlot extends Module {
    public NoServerSlot() {
        super("NoServerSlot", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
            event.cancel();
            sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
    }
}

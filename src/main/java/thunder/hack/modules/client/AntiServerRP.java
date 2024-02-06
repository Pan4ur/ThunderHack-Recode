package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;

public final class AntiServerRP extends Module {

    public AntiServerRP() {
        super("AntiServerRP", Category.CLIENT);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (e.getPacket() instanceof ResourcePackSendS2CPacket) {
            // ???
            sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
            e.cancel();
        }
    }
}

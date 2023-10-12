package dev.thunderhack.modules.client;

import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import org.jetbrains.annotations.NotNull;

public class AntiServerRP extends Module {
    public AntiServerRP() {
        super("AntiServerRP", Category.CLIENT);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (e.getPacket() instanceof ResourcePackSendS2CPacket) {
            sendPacket(new ResourcePackStatusC2SPacket(ResourcePackStatusC2SPacket.Status.ACCEPTED));
            e.cancel();
        }
    }
}

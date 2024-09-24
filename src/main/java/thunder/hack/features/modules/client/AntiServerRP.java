package thunder.hack.features.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.math.MathUtility;

public final class AntiServerRP extends Module {
    public AntiServerRP() {
        super("AntiServerRP", Category.CLIENT);
    }

    private boolean confirm, accepted;
    private int delay;

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (e.getPacket() instanceof ResourcePackSendS2CPacket) {
            confirm = true;
            accepted = false;
            delay = 0;
            e.cancel();
        }
    }

    @Override
    public void onUpdate() {
        if(confirm) {
            delay++;

            if(delay > MathUtility.random(15, 30) && !accepted) {
                sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
                accepted = true;
            }

            if(delay > MathUtility.random(40, 60) && accepted) {
                sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
                confirm = false;
            }
        }
    }
}

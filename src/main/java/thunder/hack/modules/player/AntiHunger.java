package thunder.hack.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IPlayerMoveC2SPacket;
import thunder.hack.modules.Module;

public class AntiHunger extends Module {
    public AntiHunger() {
        super("AntiHunger", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket pac) {
            ((IPlayerMoveC2SPacket) pac).setOnGround(false);
        }

        if (e.getPacket() instanceof ClientCommandC2SPacket pac) {
            if (pac.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                e.cancel();
                mc.player.setSprinting(false);
            }
        }
    }
}

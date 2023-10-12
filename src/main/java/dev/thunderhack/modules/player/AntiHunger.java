package dev.thunderhack.modules.player;

import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.mixins.accesors.IPlayerMoveC2SPacket;
import dev.thunderhack.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {
    public AntiHunger() {
        super("AntiHunger", Category.PLAYER);
    }


    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
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

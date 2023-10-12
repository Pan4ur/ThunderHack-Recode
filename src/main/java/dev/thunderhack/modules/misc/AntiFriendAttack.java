package dev.thunderhack.modules.misc;

import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.combat.Criticals;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.ThunderHack;

public class AntiFriendAttack extends Module {
    public AntiFriendAttack() {
        super("AntiFriendAttack", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof PlayerInteractEntityC2SPacket pac) {
            Entity entity = Criticals.getEntity(pac);
            if (entity == null) return;
            if (ThunderHack.friendManager.isFriend(entity.getName().getString())) e.cancel();
        }
    }
}

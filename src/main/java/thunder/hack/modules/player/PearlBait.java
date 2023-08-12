package thunder.hack.modules.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.modules.Module;

import java.util.Comparator;

public class PearlBait extends Module {
    public PearlBait() {
        super("PearlBait", Category.PLAYER);
    }

    @Subscribe
    public void OnEntitySpawn(EventEntitySpawn e) {
        if (e.getEntity() instanceof EnderPearlEntity)
            mc.world.getPlayers().stream()
                    .min(Comparator.comparingDouble((p) -> p.squaredDistanceTo(e.getEntity().getPos())))
                    .ifPresent((player) -> {
                        if (player.equals(mc.player) && mc.player.isOnGround()) {
                            mc.player.setVelocity(0, 0, 0);
                            mc.player.input.movementForward = 0.0f;
                            mc.player.input.movementSideways = 0.0f;
                            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ(), false));
                        }
                    });
    }
}

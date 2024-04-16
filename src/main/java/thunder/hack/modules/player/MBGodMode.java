package thunder.hack.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import thunder.hack.events.impl.EventTick;
import thunder.hack.modules.Module;

public class MBGodMode extends Module {
    public MBGodMode() {
        super("MBgGodMode", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player.age % 10 == 0) {
            for (int i = 0; i < 2; i++) {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 5, mc.player.getZ(), true));
            }
        }
    }
}

package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;

public class Ghost extends Module {
    private boolean bypass = false;

    public Ghost() {
        super("Ghost", "Жить после смерти", Category.MISC);
    }

    @Override
    public void onEnable() {
        bypass = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.requestRespawn();
        bypass = false;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getHealth() == 0.0f) {
            mc.player.setHealth(20.0f);
            bypass = true;
            mc.setScreen(null);
            mc.player.setPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
    }

    @Subscribe
    public void onPacketSend(PacketEvent.Send event) {
        if (bypass && event.getPacket() instanceof PlayerMoveC2SPacket) event.cancel();
    }
}
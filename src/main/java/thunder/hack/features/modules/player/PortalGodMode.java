package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.util.math.BlockPos;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.Timer;

public class PortalGodMode extends Module {
    private Timer confirmTimer = new Timer();
    private boolean teleported;

    public PortalGodMode() {
        super("PortalGodMode", Category.PLAYER);
        confirmTimer.setMs(99999);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof TeleportConfirmC2SPacket && confirmTimer.getPassedTimeMs() < 5000) {
            teleported = true;
            e.cancel();
        }
    }

    @Override
    public void onDisable(){
        teleported = false;
    }

    @Override
    public void onUpdate() {
        for(int x = (int) (mc.player.getX() - 2); x < mc.player.getX() + 2; x++)
            for(int z = (int) (mc.player.getZ() - 2); z < mc.player.getZ() + 2; z++)
                for(int y = (int) (mc.player.getY() - 2); y < mc.player.getY() + 2; y++)
                    if(mc.world.getBlockState(BlockPos.ofFloored(x,y,z)).getBlock() == Blocks.NETHER_PORTAL)
                        confirmTimer.reset();
    }

    @Override
    public String getDisplayInfo() {
        return teleported ? "God" : confirmTimer.getPassedTimeMs() < 5000 ? "Ready" : "Waiting";
    }
}
package thunder.hack.modules.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.render.HoleEsp;
import thunder.hack.setting.Setting;

public class HoleAnchor extends Module {
    public HoleAnchor() {
        super("HoleAnchor", Category.MOVEMENT);
    }


    private final Setting<Integer> pitch = new Setting<>("Pitch", 60, 0, 90);
    private final Setting<Boolean> pull = new Setting<>("Pull", true);


    @Subscribe
    public void onPlayerUpdate(PlayerUpdateEvent e) {
        if (mc.player.getPitch() > pitch.getValue()) {
            if (
                    HoleEsp.validIndestructible( BlockPos.ofFloored(mc.player.getPos()).down(1))
                    || HoleEsp.validIndestructible( BlockPos.ofFloored(mc.player.getPos()).down(2))
                    || HoleEsp.validIndestructible( BlockPos.ofFloored(mc.player.getPos()).down(3))
                    || HoleEsp.validTwoBlockIndestructibleXZ( BlockPos.ofFloored(mc.player.getPos()).down(1))
                    || HoleEsp.validTwoBlockIndestructibleXZ( BlockPos.ofFloored(mc.player.getPos()).down(2))
                    || HoleEsp.validTwoBlockIndestructibleXZ( BlockPos.ofFloored(mc.player.getPos()).down(3))
                    || HoleEsp.validTwoBlockIndestructibleXZ1( BlockPos.ofFloored(mc.player.getPos()).down(1))
                    || HoleEsp.validTwoBlockIndestructibleXZ1( BlockPos.ofFloored(mc.player.getPos()).down(2))
                    || HoleEsp.validTwoBlockIndestructibleXZ1( BlockPos.ofFloored(mc.player.getPos()).down(3))
            ) {
                if (!pull.getValue()) {
                    mc.player.setVelocity(0,mc.player.getVelocity().getY(),0);
                } else {
                    Vec3d center = new Vec3d(Math.floor(mc.player.getX()) + 0.5, Math.floor(mc.player.getY()), Math.floor(mc.player.getZ()) + 0.5);

                    if (Math.abs(center.x - mc.player.getX()) > 0.1 || Math.abs(center.z - mc.player.getZ()) > 0.1) {
                        double d3 = center.x - mc.player.getX();
                        double d4 = center.z - mc.player.getZ();
                        mc.player.setVelocity(Math.min(d3 / 2.0,0.2),mc.player.getVelocity().getY(),Math.min(d4 / 2.0,0.2));
                    }
                }
            }
        }
    }
}

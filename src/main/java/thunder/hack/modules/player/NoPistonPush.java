package thunder.hack.modules.player;

import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.Vec3i;
import thunder.hack.modules.Module;
import thunder.hack.utility.world.HoleUtility;

public class NoPistonPush extends Module {
    private boolean wasSprinting;

    public NoPistonPush() {
        super("NoPistonPush", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            wasSprinting = mc.player.isSprinting();
        }

    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;

        if (HoleUtility.isHole(mc.player.getBlockPos())) {
            if (detectPistonPush()) {
                wasSprinting = mc.player.isSprinting();
                mc.player.setSprinting(true);
            } else {
                mc.player.setSprinting(wasSprinting);
            }
        }
    }

    private boolean detectPistonPush() {
        for (Vec3i vec : HoleUtility.VECTOR_PATTERN) {
            if (mc.world.getBlockState(mc.player.getBlockPos().add(vec).up()).getBlock() instanceof PistonBlock) {
                return true;
            }
        }

        return false;
    }
}

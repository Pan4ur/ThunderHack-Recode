package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import dev.thunderhack.utils.world.HoleUtils;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.Vec3i;

public class NoPistonPush extends Module {
    private boolean wasSprinting;

    public NoPistonPush() {
        super("NoPistonPush", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) wasSprinting = mc.player.isSprinting();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;

        if (HoleUtils.isHole(mc.player.getBlockPos())) {
            if (detectPistonPush()) {
                wasSprinting = mc.player.isSprinting();
                mc.player.setSprinting(true);
            } else mc.player.setSprinting(wasSprinting);
        }
    }

    private boolean detectPistonPush() {
        for (Vec3i vec : HoleUtils.VECTOR_PATTERN) {
            if (mc.world.getBlockState(mc.player.getBlockPos().add(vec).up()).getBlock() instanceof PistonBlock) {
                return true;
            }
        }

        return false;
    }
}

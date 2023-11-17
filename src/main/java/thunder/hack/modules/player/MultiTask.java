package thunder.hack.modules.player;

import net.minecraft.util.hit.BlockHitResult;
import thunder.hack.modules.Module;

public class MultiTask extends Module {
    public MultiTask() {
        super("MultiTask", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (mc.crosshairTarget instanceof BlockHitResult crossHair && crossHair.getBlockPos() != null && mc.options.attackKey.isPressed() && !mc.world.getBlockState(crossHair.getBlockPos()).isAir())
            mc.interactionManager.attackBlock(crossHair.getBlockPos(), crossHair.getSide());
    }
}

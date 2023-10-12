package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import net.minecraft.util.hit.BlockHitResult;

public class MultiTask extends Module {
    public MultiTask() {
        super("MultiTask", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (mc.crosshairTarget instanceof BlockHitResult crossHair && crossHair.getBlockPos() != null && mc.options.attackKey.isPressed())
            mc.interactionManager.attackBlock(crossHair.getBlockPos(), crossHair.getSide());
    }
}

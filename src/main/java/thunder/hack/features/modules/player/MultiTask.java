package thunder.hack.features.modules.player;

import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import thunder.hack.features.modules.Module;

public class MultiTask extends Module {
    public MultiTask() {
        super("MultiTask", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (mc.crosshairTarget instanceof BlockHitResult crossHair && crossHair.getBlockPos() != null && mc.options.attackKey.isPressed() && !mc.world.getBlockState(crossHair.getBlockPos()).isAir()) {
            mc.interactionManager.attackBlock(crossHair.getBlockPos(), crossHair.getSide());
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() != null && mc.options.attackKey.isPressed() && mc.player.getAttackCooldownProgress(0.5f) > 0.9f) {
            mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}

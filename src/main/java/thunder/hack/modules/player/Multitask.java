package thunder.hack.modules.player;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import thunder.hack.modules.Module;

public class Multitask extends Module {
    public Multitask() {
        super("MultiTask", Category.PLAYER);
    }

    @Override
    public void onUpdate(){
        if(mc.crosshairTarget != null && mc.crosshairTarget instanceof  BlockHitResult && ((BlockHitResult)mc.crosshairTarget).getBlockPos() != null && mc.options.attackKey.isPressed()){
            mc.interactionManager.attackBlock(((BlockHitResult)mc.crosshairTarget).getBlockPos(),((BlockHitResult)mc.crosshairTarget).getSide());
        }
    }
}

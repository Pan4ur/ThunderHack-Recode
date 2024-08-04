package thunder.hack.injection;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;
import static thunder.hack.ThunderHack.mc;


@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding {
    @Shadow public abstract boolean equals(KeyBinding other);

    @Shadow public abstract boolean isPressed();

    @Inject(method = "isPressed",at = @At("HEAD"),cancellable = true)
    private void pressHook(CallbackInfoReturnable<Boolean> cir){
        if(     this.equals(mc.options.sneakKey)
                && mc.player != null
                && mc.world != null
                && ModuleManager.safeWalk.isEnabled()
                && mc.player.isOnGround() && mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getPos().getX()), (int) Math.floor(mc.player.getPos().getY()) - 1, (int) Math.floor(mc.player.getPos().getZ()))).isAir()
                && !ModuleManager.scaffold.isEnabled()){
            cir.setReturnValue(true);
        }
    }
}

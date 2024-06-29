package thunder.hack.injection;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.impl.ModuleManager;
import static thunder.hack.ThunderHack.mc;


@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding {
    @Shadow public abstract boolean equals(KeyBinding other);

    @Shadow public abstract boolean isPressed();

    @Inject(method = "isPressed",at = @At("HEAD"),cancellable = true)
    private void pressHook(CallbackInfoReturnable<Boolean> cir){
        Vec3d pos = mc.player.getPos();
        if(this.equals(mc.options.sneakKey) && ModuleManager.safeWalk.isEnabled() && mc.player.isOnGround() && mc.world.getBlockState(new BlockPos((int) pos.getX(), (int) pos.getY() - 1, (int) pos.getZ())).isAir() && !ModuleManager.scaffold.isEnabled()){
            cir.setReturnValue(true);
        }
    }
}

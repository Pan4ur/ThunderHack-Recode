package thunder.hack.injection;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(SlimeBlock.class)
public class MixinSlimeBlock {
    @Inject(method = "onSteppedOn", at = @At("HEAD"), cancellable = true)
    public void onSteppedOnHook(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if(ModuleManager.noSlow.isEnabled() && ModuleManager.noSlow.slime.getValue())
            ci.cancel();
    }
}

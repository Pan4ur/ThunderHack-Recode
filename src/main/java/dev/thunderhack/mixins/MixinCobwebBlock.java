package dev.thunderhack.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.modules.movement.AntiWeb;

@Mixin(CobwebBlock.class)
public class MixinCobwebBlock {
    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onEntityCollisionHook(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if(ModuleManager.antiWeb.isEnabled() && AntiWeb.mode.getValue() == AntiWeb.Mode.Ignore){
            ci.cancel();
        }
    }
}
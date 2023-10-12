package dev.thunderhack.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.modules.render.XRay;

@Mixin(Block.class)
public abstract class MixinBlock {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void shouldDrawSideHook(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.xray.isEnabled())
            cir.setReturnValue(XRay.isCheckableOre(state.getBlock()));
    }

    @Inject(method = "isTransparent", at = @At("HEAD"), cancellable = true)
    public void isTransparentHook(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance() == null) return;
        if (ModuleManager.xray.isEnabled())
            cir.setReturnValue(!XRay.isCheckableOre(state.getBlock()));
    }
}

package thunder.hack.injection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.render.XRay;

import static thunder.hack.core.manager.IManager.mc;

@Mixin(Block.class)
public abstract class MixinBlock {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void shouldDrawSideHook(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.xray.isEnabled() && ModuleManager.xray.wallHack.getValue())
            cir.setReturnValue(XRay.isCheckableOre(state.getBlock()));
        if(ModuleManager.autoAnchor.isEnabled() && state.getBlock() instanceof FireBlock)
            cir.setReturnValue(false);
    }

    /*
    @Inject(method = "isTransparent", at = @At("HEAD"), cancellable = true)
    public void isTransparentHook(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance() == null) return;
        if (ModuleManager.xray.isEnabled() && ModuleManager.xray.wallHack.getValue())
            cir.setReturnValue(!XRay.isCheckableOre(state.getBlock()));
    }
     */

    @Inject(method = "getVelocityMultiplier", at = @At("HEAD"), cancellable = true)
    public void getVelocityMultiplierHook(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.noSlow.isEnabled()) {
            if (ModuleManager.noSlow.soulSand.getValue() && (Object) this == Blocks.SOUL_SAND)
                cir.setReturnValue(Blocks.DIRT.getVelocityMultiplier());
            if (ModuleManager.noSlow.honey.getValue() && (Object) this == Blocks.HONEY_BLOCK)
                cir.setReturnValue(Blocks.DIRT.getVelocityMultiplier());
        }
    }

    @Inject(method = "getSlipperiness", at = @At("HEAD"), cancellable = true)
    public void getSlipperinessHook(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.noSlow.isEnabled()) {
            if (ModuleManager.noSlow.slime.getValue() && (Object) this == Blocks.SLIME_BLOCK)
                cir.setReturnValue(Blocks.DIRT.getSlipperiness());
            if (ModuleManager.noSlow.ice.getValue() && ((Object) this == Blocks.ICE || (Object) this == Blocks.PACKED_ICE || (Object) this == Blocks.BLUE_ICE || (Object) this == Blocks.FROSTED_ICE) && !mc.options.jumpKey.isPressed())
                cir.setReturnValue(Blocks.DIRT.getSlipperiness());
        }
    }
}

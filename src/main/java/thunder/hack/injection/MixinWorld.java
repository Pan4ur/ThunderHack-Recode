package thunder.hack.injection;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import thunder.hack.utility.math.ExplosionUtil;

import static thunder.hack.modules.Module.mc;


@Mixin(World.class)
public abstract class MixinWorld {
    @Shadow public abstract boolean removeBlock(BlockPos pos, boolean move);

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    public void blockStat(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (ExplosionUtil.terrainIgnore && mc.world != null && !mc.world.isInBuildLimit(pos)) {
            WorldChunk worldChunk = mc.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

            BlockState tempState = worldChunk.getBlockState(pos);

            if((ExplosionUtil.anchorIgnore != null && ExplosionUtil.anchorIgnore == pos)){
                cir.setReturnValue(Blocks.AIR.getDefaultState());
                return;
            }

            if (tempState.getBlock() == Blocks.OBSIDIAN
                    || tempState.getBlock() == Blocks.BEDROCK
                    || tempState.getBlock() == Blocks.ENDER_CHEST
                    || tempState.getBlock() == Blocks.RESPAWN_ANCHOR
            )
                return;
            cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }


}

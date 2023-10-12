package dev.thunderhack.mixins;

import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.modules.player.AutoTool;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.enchantment.Enchantments.EFFICIENCY;

@Mixin({AbstractBlock.class})
public abstract class MixinAbstractBlock {
    @Inject(method = "calcBlockBreakingDelta", at = @At("HEAD"), cancellable = true)
    public void calcBlockBreakingDeltaHook(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> ci)  {
        if(ModuleManager.autoTool.isEnabled() && AutoTool.silent.getValue()) {
            float f = state.getHardness(world, pos);
            if (f < 0.0F) {
                ci.setReturnValue(0.0f);
            } else {
                float dig_speed = getDigSpeed(state, player.getInventory().getStack(AutoTool.itemIndex)) / f;
                ci.setReturnValue(player.getInventory().getStack(AutoTool.itemIndex).isSuitableFor(state) ? dig_speed / 30.0F : dig_speed / 100.0F);
            }
        }
    }

    public float getDigSpeed(BlockState state, ItemStack stack)
    {
        double str = stack.getMiningSpeedMultiplier(state);
        int effect = EnchantmentHelper.getLevel(EFFICIENCY, stack);
        return (float) Math.max(str + (str > 1.0 ? (effect * effect + 1.0) : 0.0), 0.0);
    }

    /*private boolean canHarvestBlock(BlockState state, ItemStack stack) {
        if (state.isToolRequired()) {
            return true;
        }
        String tool = state.getBlock().getDefaultState()..getHa(state);
        if (stack.isEmpty() || tool == null) {
            return mc.player.canHarvest(state);
        }
        final int toolLevel = stack.getItem().getHarvestLevel(stack, tool, mc.player, state);
        if (toolLevel < 0) {
            return mc.player.canHarvest(state);
        }
        return toolLevel >= state.getBlock().getHarvestLevel(state);
    }*/
}
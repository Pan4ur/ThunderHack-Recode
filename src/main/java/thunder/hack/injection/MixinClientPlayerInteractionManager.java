package thunder.hack.injection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventStopUsingItem;
import thunder.hack.modules.player.NoInteract;
import thunder.hack.modules.player.Reach;
import thunder.hack.modules.player.SpeedMine;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static thunder.hack.utility.Util.mc;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager{
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> callback) {
        Block bs = mc.world.getBlockState(hitResult.getBlockPos()).getBlock();
        if(Thunderhack.moduleManager.get(NoInteract.class).isEnabled() && (bs == Blocks.CHEST ||
                bs == Blocks.TRAPPED_CHEST ||
                bs == Blocks.FURNACE ||
                bs == Blocks.ANVIL ||
                bs == Blocks.CRAFTING_TABLE ||
                bs instanceof ShulkerBoxBlock ||
                bs instanceof FenceBlock ||
                bs instanceof FenceGateBlock)){
            callback.setReturnValue(ActionResult.PASS);
        }
    }

    @Shadow
    private int blockBreakingCooldown;

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.GETFIELD, ordinal = 0))
    public int updateBlockBreakingProgressHook(ClientPlayerInteractionManager clientPlayerInteractionManager) {
        return Objects.requireNonNull(Thunderhack.moduleManager.get(SpeedMine.class)).isEnabled() ? 0 : this.blockBreakingCooldown;
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void attackBlockHook(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Thunderhack.EVENT_BUS.post(new EventAttackBlock(pos,direction));
    }

    @Inject(method = "stopUsingItem", at = @At("HEAD"), cancellable = true)
    private void stopUsingItemHook(PlayerEntity player, CallbackInfo ci) {
        EventStopUsingItem event = new EventStopUsingItem();
        Thunderhack.EVENT_BUS.post(event);
        if(event.isCancelled()){
            ci.cancel();
        }
    }

    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private void getReachDistanceHook(CallbackInfoReturnable<Float> cir) {
        if (Thunderhack.moduleManager.get(Reach.class).isEnabled()) {
            cir.setReturnValue(Reach.range.getValue());
        }
    }

    @Inject(method = "hasExtendedReach", at = @At("HEAD"), cancellable = true)
    private void hasExtendedReachHook(CallbackInfoReturnable<Boolean> cir) {
        if (Thunderhack.moduleManager.get(Reach.class).isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}

package thunder.hack.injection;

import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventBreakBlock;
import thunder.hack.events.impl.EventClickSlot;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.player.NoInteract;
import thunder.hack.features.modules.player.SpeedMine;

import static thunder.hack.features.modules.Module.mc;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Shadow
    private int blockBreakingCooldown;

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        Block bs = mc.world.getBlockState(hitResult.getBlockPos()).getBlock();
        if (ModuleManager.noInteract.isEnabled() && (
                bs == Blocks.CHEST ||
                        bs == Blocks.TRAPPED_CHEST ||
                        bs == Blocks.FURNACE ||
                        bs == Blocks.ANVIL ||
                        bs == Blocks.CRAFTING_TABLE ||
                        bs == Blocks.HOPPER ||
                        bs == Blocks.JUKEBOX ||
                        bs == Blocks.NOTE_BLOCK ||
                        bs == Blocks.ENDER_CHEST ||
                        bs == Blocks.DISPENSER ||
                        bs == Blocks.DROPPER ||
                        bs instanceof ShulkerBoxBlock ||
                        bs instanceof FenceBlock ||
                        bs instanceof FenceGateBlock ||
                        bs instanceof TrapdoorBlock)
                && (ModuleManager.aura.isEnabled() || !NoInteract.onlyAura.getValue())) {
            cir.setReturnValue(ActionResult.PASS);
        }

        if(mc.player != null && ModuleManager.antiBallPlace.isEnabled()
                && ((mc.player.getOffHandStack().getItem() == Items.PLAYER_HEAD && hand == Hand.OFF_HAND) || (mc.player.getMainHandStack().getItem() == Items.PLAYER_HEAD && hand == Hand.MAIN_HAND)))
            cir.setReturnValue(ActionResult.PASS);
    }

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.GETFIELD, ordinal = 0))
    public int updateBlockBreakingProgressHook(ClientPlayerInteractionManager clientPlayerInteractionManager) {
        return ModuleManager.speedMine.isEnabled() ? 0 : this.blockBreakingCooldown;
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    public void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.speedMine.isEnabled() && ModuleManager.speedMine.mode.getValue() == SpeedMine.Mode.Packet) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void attackBlockHook(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(Module.fullNullCheck()) return;
        EventAttackBlock event = new EventAttackBlock(pos, direction);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled())
            cir.setReturnValue(false);
    }

    /*
    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private void getReachDistanceHook(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.reach.isEnabled()) {
            cir.setReturnValue(Reach.range.getValue());
        }
    }

    @Inject(method = "hasExtendedReach", at = @At("HEAD"), cancellable = true)
    private void hasExtendedReachHook(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.reach.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
     */

    @Inject(method = "breakBlock", at = @At("RETURN"), cancellable = true)
    public void breakBlockHook(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(Module.fullNullCheck()) return;
        EventBreakBlock event = new EventBreakBlock(pos);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled())
            cir.setReturnValue(false);
    }

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    public void clickSlotHook(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if(Module.fullNullCheck()) return;
        EventClickSlot event = new EventClickSlot(actionType, slotId, button, syncId);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled())
            ci.cancel();
    }
}

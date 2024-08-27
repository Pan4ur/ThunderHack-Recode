package thunder.hack.injection;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.events.impl.EventEatFood;
import thunder.hack.events.impl.EventPlayerJump;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.features.modules.client.Media;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.movement.AutoSprint;
import thunder.hack.features.modules.movement.Speed;

import static thunder.hack.features.modules.Module.mc;

@Mixin(value = PlayerEntity.class, priority = 800)
public class MixinPlayerEntity {
    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("HEAD"), cancellable = true)
    public void getAttackCooldownProgressPerTickHook(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.aura.isEnabled() && ModuleManager.aura.switchMode.getValue() == Aura.Switch.Silent) {
            cir.setReturnValue(12.5f);
        }
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    public void getDisplayNameHook(CallbackInfoReturnable<Text> cir) {
        if (ModuleManager.media.isEnabled() && Media.nickProtect.getValue()) {
            cir.setReturnValue(Text.of("Protected"));
        }
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", shift = At.Shift.AFTER))
    public void attackAHook(CallbackInfo callbackInfo) {
        if (ModuleManager.autoSprint.isEnabled() && AutoSprint.sprint.getValue()) {
            final float multiplier = 0.6f + 0.4f * AutoSprint.motion.getValue();
            mc.player.setVelocity(mc.player.getVelocity().x / 0.6 * multiplier, mc.player.getVelocity().y, mc.player.getVelocity().z / 0.6 * multiplier);
            mc.player.setSprinting(true);
        }
    }

    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    public void getMovementSpeedHook(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.speed.isEnabled() && ModuleManager.speed.mode.is(Speed.Mode.Vanilla)) {
            cir.setReturnValue(ModuleManager.speed.boostFactor.getValue());
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attackAHook2(Entity target, CallbackInfo ci) {
        final EventAttack event = new EventAttack(target, false);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravelhookPre(Vec3d movementInput, CallbackInfo ci) {
        if (mc.player == null)
            return;

        final EventPlayerTravel event = new EventPlayerTravel(movementInput, true);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }


    @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
    private void onTravelhookPost(Vec3d movementInput, CallbackInfo ci) {
        if (mc.player == null)
            return;
        final EventPlayerTravel event = new EventPlayerTravel(movementInput, false);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJumpPre(CallbackInfo ci) {
        ThunderHack.EVENT_BUS.post(new EventPlayerJump(true));
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void onJumpPost(CallbackInfo ci) {
        ThunderHack.EVENT_BUS.post(new EventPlayerJump(false));
    }

    @Inject(method = "eatFood", at = @At("RETURN"))
    public void eatFoodHook(World world, ItemStack stack, FoodComponent foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        ThunderHack.EVENT_BUS.post(new EventEatFood(cir.getReturnValue()));
    }

    @Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
    protected void shouldDismountHook(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.boatFly.isEnabled() && ModuleManager.boatFly.allowShift.getValue())
            cir.setReturnValue(false);
    }

    @Inject(method = "getBlockInteractionRange", at = @At("HEAD"), cancellable = true)
    public void getBlockInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
        if (ModuleManager.reach.isEnabled()) {
            if (ModuleManager.reach.Creative.getValue() && mc.player.isCreative()) {
                cir.setReturnValue((double) ModuleManager.reach.creativeBlocksRange.getValue());
            }
            else {
                cir.setReturnValue((double) ModuleManager.reach.blocksRange.getValue());
            }
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At("HEAD"), cancellable = true)
    public void getEntityInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
        if (ModuleManager.reach.isEnabled()) {
            if (ModuleManager.reach.Creative.getValue() && mc.player.isCreative()) {
                cir.setReturnValue((double) ModuleManager.reach.creativeEntityRange.getValue());
            }
            else {
                cir.setReturnValue((double) ModuleManager.reach.entityRange.getValue());
            }
        }
    }
}

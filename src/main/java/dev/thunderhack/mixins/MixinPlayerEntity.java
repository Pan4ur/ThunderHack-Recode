package dev.thunderhack.mixins;

import dev.thunderhack.event.events.EventAttack;
import dev.thunderhack.event.events.EventPlayerJump;
import dev.thunderhack.event.events.EventPlayerTravel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.modules.client.Media;
import dev.thunderhack.modules.combat.Aura;
import dev.thunderhack.modules.movement.AutoSprint;

import static dev.thunderhack.modules.Module.mc;

@Mixin(value = PlayerEntity.class, priority = 800)
public class MixinPlayerEntity {
    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z", opcode = Opcodes.PUTFIELD))
    void noClipHook(PlayerEntity playerEntity, boolean value) {
        if (ModuleManager.freeCam.isEnabled() && !mc.player.isOnGround()) {
            playerEntity.noClip = true;
        } else {
            playerEntity.noClip = playerEntity.isSpectator();
        }
    }

    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("HEAD"), cancellable = true)
    public void getAttackCooldownProgressPerTickHook(CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.aura.isEnabled() && Aura.switchMode.getValue() == Aura.Switch.Silent) {
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

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attackAHook2(Entity target, CallbackInfo ci) {
        final EventAttack event = new EventAttack(target);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravelhookPre(Vec3d movementInput, CallbackInfo ci) {
        final EventPlayerTravel event = new EventPlayerTravel(movementInput, true);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
    private void onTravelhookPost(Vec3d movementInput, CallbackInfo ci) {
        final EventPlayerTravel event = new EventPlayerTravel(movementInput, false);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJumpPre(CallbackInfo ci) {
        final EventPlayerJump event = new EventPlayerJump(true);
        ThunderHack.EVENT_BUS.post(event);
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void onJumpPost(CallbackInfo ci) {
        final EventPlayerJump event = new EventPlayerJump(false);
        ThunderHack.EVENT_BUS.post(event);
    }
}

package thunder.hack.injection;

import com.mojang.authlib.GameProfile;
import thunder.hack.Thunderhack;
import thunder.hack.core.Core;
import thunder.hack.core.ModuleManager;
import thunder.hack.core.PlaceManager;
import thunder.hack.events.impl.*;
import thunder.hack.modules.movement.Velocity;
import thunder.hack.modules.movement.NoSlow;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static thunder.hack.modules.Module.mc;

@Mixin(value = ClientPlayerEntity.class,priority = 800)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
    @Shadow public abstract float getPitch(float tickDelta);

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickHook(CallbackInfo info) {
        if (mc.player != null && mc.world != null) {
            Thunderhack.EVENT_BUS.post(new PlayerUpdateEvent());
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0 )
    private boolean tickMovementHook(ClientPlayerEntity player) {
        if (ModuleManager.noSlow.isEnabled())
            return false;
        return player.isUsingItem();
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        EventMove event = new EventMove(movement.x,movement.y,movement.z);
        Thunderhack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            super.move(movementType, new Vec3d( event.get_x(),event.get_y(),event.get_z()));
            ci.cancel();
        }
    }

    boolean pre_sprint_state = false;

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void sendMovementPacketsHook(CallbackInfo info) {
        if(PlaceManager.isRotating) return;
        EventSync event = new EventSync(getYaw(),getPitch());
        Thunderhack.EVENT_BUS.post(event);
        EventSprint e = new EventSprint(isSprinting());
        Thunderhack.EVENT_BUS.post(e);
        EventAfterRotate ear = new EventAfterRotate();
        Thunderhack.EVENT_BUS.post(ear);
        if (e.getSprintState() != mc.player.lastSprinting) {
            if (e.getSprintState()) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_SPRINTING));
            } else {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
            mc.player.lastSprinting = e.getSprintState();
        }
        pre_sprint_state = mc.player.lastSprinting;
        Core.lock_sprint = true;

        if (event.isCancelled()) {
            info.cancel();
        }
    }
    @Inject(method = "sendMovementPackets", at = @At("RETURN"), cancellable = true)
    private void sendMovementPacketsPostHook(CallbackInfo info) {
        mc.player.lastSprinting = pre_sprint_state;
        Core.lock_sprint = false;
        EventPostSync event = new EventPostSync();
        Thunderhack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    private boolean updateLock = false;

    @Shadow
    protected abstract void sendMovementPackets();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void PostUpdateHook(CallbackInfo info) {
        if (updateLock) {
            return;
        }
        PostPlayerUpdateEvent playerUpdateEvent = new PostPlayerUpdateEvent();
        Thunderhack.EVENT_BUS.post(playerUpdateEvent);
        if (playerUpdateEvent.isCancelled()) {
            info.cancel();
            if (playerUpdateEvent.getIterations() > 0) {
                for (int i = 0; i < playerUpdateEvent.getIterations(); i++) {
                    updateLock = true;
                    tick();
                    updateLock = false;
                    sendMovementPackets();
                }
            }
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocksHook(double x, double d, CallbackInfo info) {
        if (ModuleManager.velocity.isEnabled() && Velocity.noPush.getValue()) {
            info.cancel();
        }
    }
}

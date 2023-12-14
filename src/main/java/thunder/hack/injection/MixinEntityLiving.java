package thunder.hack.injection;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventTravel;
import thunder.hack.modules.render.Animations;
import thunder.hack.utility.interfaces.IEntityLiving;

import static thunder.hack.modules.Module.mc;

@Mixin(LivingEntity.class)
public class MixinEntityLiving implements IEntityLiving {
    @Shadow protected double serverX;
    @Shadow protected double serverY;
    @Shadow protected double serverZ;

    @Inject(method = {"getHandSwingDuration"}, at = {@At("HEAD")}, cancellable = true)
    private void getArmSwingAnimationEnd(final CallbackInfoReturnable<Integer> info) {
        if (ModuleManager.animations.isEnabled() && Animations.slowAnimation.getValue())
            info.setReturnValue(Animations.slowAnimationVal.getValue());
    }

    double prevServerX, prevServerY, prevServerZ;

    @Inject(method = {"updateTrackedPositionAndAngles"}, at = {@At("HEAD")})
    private void updateTrackedPositionAndAnglesHook(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci) {
        prevServerX = serverX;
        prevServerY = serverY;
        prevServerZ = serverZ;
    }

    @Override
    public double getPrevServerX() {
        return prevServerX;
    }

    @Override
    public double getPrevServerY() {
        return prevServerY;
    }

    @Override
    public double getPrevServerZ() {
        return prevServerZ;
    }

    @Unique
    private boolean prevFlying = false;

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void isFallFlyingHook(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.elytraRecast.isEnabled()) {
            boolean elytra = cir.getReturnValue();
            if (prevFlying && !cir.getReturnValue()) {
                cir.setReturnValue(ModuleManager.elytraRecast.castElytra());
            }
            prevFlying = elytra;
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travelHook(Vec3d movementInput, CallbackInfo ci) {
        if ((LivingEntity) (Object) this != mc.player) return;
        final EventTravel event = new EventTravel(mc.player.getVelocity(), true);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            mc.player.move(MovementType.SELF, event.getmVec());
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
    public void travelPostHook(Vec3d movementInput, CallbackInfo ci) {
        if ((LivingEntity) (Object) this != mc.player) return;
        final EventTravel event = new EventTravel(movementInput, false);
        ThunderHack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }
}

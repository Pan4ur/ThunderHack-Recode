package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.impl.ModuleManager;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow
    protected abstract double clipToSpace(double desiredCameraDistance);

    @Shadow
    private boolean thirdPerson;

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0))
    private void modifyCameraDistance(Args args) {
        if (ModuleManager.noCameraClip.isEnabled()) {
            args.set(0, -clipToSpace(ModuleManager.noCameraClip.getDistance()));
        }
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(double desiredCameraDistance, CallbackInfoReturnable<Double> info) {
        if (ModuleManager.noCameraClip.isEnabled()) {
            info.setReturnValue(ModuleManager.noCameraClip.getDistance());
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void updateHook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.freeCam.isEnabled()) {
            this.thirdPerson = true;
        }
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void setRotationHook(Args args) {
        if(ModuleManager.freeCam.isEnabled())
            args.setAll(ModuleManager.freeCam.getFakeYaw(), ModuleManager.freeCam.getFakePitch());
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosHook(Args args) {
        if(ModuleManager.freeCam.isEnabled())
            args.setAll(ModuleManager.freeCam.getFakeX(), ModuleManager.freeCam.getFakeY(), ModuleManager.freeCam.getFakeZ());
    }
}
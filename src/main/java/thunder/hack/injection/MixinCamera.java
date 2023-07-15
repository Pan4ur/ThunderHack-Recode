package thunder.hack.injection;

import thunder.hack.Thunderhack;
import thunder.hack.modules.player.NoCameraClip;
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

    @Shadow protected abstract double clipToSpace(double desiredCameraDistance);

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0))
    private void modifyCameraDistance(Args args) {
        args.set(0, -clipToSpace(4));
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(double desiredCameraDistance, CallbackInfoReturnable<Double> info) {
        if (Thunderhack.moduleManager.get(NoCameraClip.class).isOn()) {
            info.setReturnValue(desiredCameraDistance);
        }
    }
}
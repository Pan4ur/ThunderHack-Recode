package thunder.hack.injection;

import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.render.Fullbright;
import thunder.hack.modules.render.NoRender;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void update(Args args) {
        if (ModuleManager.fullbright.isEnabled()) {
            args.set(2, Color.getHSBColor(0, 0f, (float) Fullbright.brightness.getValue() / 15f).getRGB());
        }
    }

    @Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
    private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> info) {
        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.darkness.getValue()) info.setReturnValue(0.0f);
    }
}
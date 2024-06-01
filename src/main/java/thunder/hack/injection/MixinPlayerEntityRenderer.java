package thunder.hack.injection;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static thunder.hack.core.impl.ModuleManager.badTrip;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
     @Inject(method = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;scale(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("HEAD"))
     protected void modifyPlayerScale(AbstractClientPlayerEntity player, MatrixStack matrixStack, float tickDelta, CallbackInfo ci) {
        if (badTrip != null && badTrip.isEnabled()) {
            long time = System.currentTimeMillis();
            float scaleFactorX = 1.0f + badTrip.factor.getValue() * (float) Math.sin((double) time / badTrip.speed.getValue());
            float scaleFactorY = 1.0f - badTrip.factor.getValue() * (float) Math.sin((double) time / badTrip.speed.getValue());
            matrixStack.scale(scaleFactorX, scaleFactorY, 1.0f);
        }
    }
}

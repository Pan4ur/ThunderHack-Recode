package thunder.hack.injection;


import thunder.hack.Thunderhack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter {

    @Shadow private float lastFrameDuration;
    @Shadow private float tickDelta;
    @Shadow private long prevTimeMillis;
    @Shadow private float tickTime;

    @Inject(method = "beginRenderTick", at = @At("HEAD"), cancellable = true)
    private void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> ci) {
        this.lastFrameDuration = ((timeMillis - this.prevTimeMillis) / this.tickTime) * Thunderhack.TICK_TIMER;
        this.prevTimeMillis = timeMillis;
        this.tickDelta += this.lastFrameDuration;
        int i = (int) this.tickDelta;
        this.tickDelta -= i;
        ci.setReturnValue(i);
    }

}
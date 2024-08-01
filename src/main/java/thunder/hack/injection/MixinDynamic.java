package thunder.hack.injection;

import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
@Mixin(RenderTickCounter.Dynamic.class)
public class MixinDynamic {
    @Shadow
    private float lastFrameDuration;
    @Shadow
    private float tickDelta;
    @Shadow private long prevTimeMillis;
    @Final
    @Shadow private float tickTime;

    @Inject(method = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;beginRenderTick(J)I", at = @At("HEAD"), cancellable = true)
    private void beginRenderTickHook(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        if(ThunderHack.TICK_TIMER == 1)
            return;

        this.lastFrameDuration = ((timeMillis - this.prevTimeMillis) / this.tickTime) * ThunderHack.TICK_TIMER;
        this.prevTimeMillis = timeMillis;
        this.tickDelta += this.lastFrameDuration;
        int i = (int) this.tickDelta;
        this.tickDelta -= i;
        cir.setReturnValue(i);
    }
}
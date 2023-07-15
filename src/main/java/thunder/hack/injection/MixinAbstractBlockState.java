package thunder.hack.injection;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.Thunderhack;
import thunder.hack.modules.render.XRay;

import java.util.Objects;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState {

    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    public void getLuminanceHook(CallbackInfoReturnable<Integer> cir) {
        if (Objects.requireNonNull(Thunderhack.moduleManager.get(XRay.class)).isEnabled()) {
            cir.setReturnValue(15);
        }
    }
}

package thunder.hack.injection;


import java.util.Iterator;


import thunder.hack.Thunderhack;
import thunder.hack.modules.player.Velocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.Direction;

@Mixin(FlowableFluid.class)
public class MixinFlowableFluid {

    @Redirect(method = "getVelocity", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
    private boolean getVelocity_hasNext(Iterator<Direction> var9) {
        if (Thunderhack.moduleManager.get(Velocity.class).isEnabled() && Thunderhack.moduleManager.get(Velocity.class).noPush.getValue()) {
            return false;
        }
        return var9.hasNext();
    }

}
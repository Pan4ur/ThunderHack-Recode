package thunder.hack.injection;


import java.util.Iterator;


import thunder.hack.core.ModuleManager;
import thunder.hack.modules.movement.Velocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.Direction;

@Mixin(FlowableFluid.class)
public class MixinFlowableFluid {

    @Redirect(method = "getVelocity", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
    private boolean getVelocityHook(Iterator<Direction> var9) {
        if (ModuleManager.velocity.isEnabled() && Velocity.noPush.getValue()) {
            return false;
        }
        return var9.hasNext();
    }

}
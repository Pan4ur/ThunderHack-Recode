package thunder.hack.injection;

import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.movement.Velocity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(FlowableFluid.class)
public class MixinFlowableFluid {
    @Redirect(method = "getVelocity", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
    private boolean getVelocityHook(Iterator<Direction> var9) {
        if (ModuleManager.velocity.isEnabled() && ModuleManager.velocity.water.getValue()) {
            return false;
        }
        return var9.hasNext();
    }
}
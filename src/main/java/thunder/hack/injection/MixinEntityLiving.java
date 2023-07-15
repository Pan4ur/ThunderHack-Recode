package thunder.hack.injection;

import thunder.hack.Thunderhack;
import thunder.hack.modules.render.ViewModel;
import thunder.hack.utility.interfaces.IEntityLiving;
import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinEntityLiving implements IEntityLiving {

    @Shadow protected double serverX;

    @Shadow protected double serverY;

    @Shadow protected double serverZ;

    @Inject(method = {"getHandSwingDuration"}, at = {@At("HEAD")}, cancellable = true)
    private void getArmSwingAnimationEnd(final CallbackInfoReturnable<Integer> info) {
        if (Thunderhack.moduleManager.get(ViewModel.class).isEnabled() && Thunderhack.moduleManager.get(ViewModel.class).slowAnimation.getValue()) {
            info.setReturnValue(Thunderhack.moduleManager.get(ViewModel.class).slowAnimationVal.getValue());
        }
    }

    double prevServerX,prevServerY,prevServerZ;


    @Inject(method = {"updateTrackedPositionAndAngles"}, at = {@At("HEAD")})
    private void updateTrackedPositionAndAnglesHook(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate, CallbackInfo ci) {
        prevServerX = serverX;
        prevServerY = serverY;
        prevServerZ = serverZ;
    }

    @Override
    public double getPrevServerX() {
        return prevServerX;
    }

    @Override
    public double getPrevServerY() {
        return prevServerY;
    }

    @Override
    public double getPrevServerZ() {
        return prevServerZ;
    }
}

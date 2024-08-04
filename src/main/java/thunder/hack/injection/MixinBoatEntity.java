package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin(BoatEntity.class)
public class MixinBoatEntity {

    @Unique
    private float prevYaw, prevHeadYaw;

    @Inject(method = "updatePassengerPosition", at = @At("HEAD"))
    protected void updatePassengerPositionHookPre(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo ci) {
        if(ModuleManager.boatFly.isEnabled()) {
            prevYaw = passenger.getYaw();
            prevHeadYaw = passenger.getHeadYaw();
        }
    }

    @Inject(method = "updatePassengerPosition", at = @At("RETURN"))
    protected void updatePassengerPositionHookPost(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo ci) {
        if(ModuleManager.boatFly.isEnabled()) {
            passenger.setYaw(prevYaw);
            passenger.setHeadYaw(prevHeadYaw);
        }
    }
}

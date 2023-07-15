package thunder.hack.injection;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventEntitySpawn;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    public void onAddEntity(int id, Entity entity, CallbackInfo ci) {
        EventEntitySpawn ees = new EventEntitySpawn(entity);
        Thunderhack.EVENT_BUS.post(ees);
        if (ees.isCancelled()) {
            ci.cancel();
        }
    }
}

package thunder.hack.injection;

import net.minecraft.client.particle.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.impl.ModuleManager;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
    @Inject(at = @At("HEAD"), method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", cancellable = true)
    public void addParticleHook(Particle particle, CallbackInfo e) {
        if (ModuleManager.noRender.elderGuardian.getValue() && particle instanceof ElderGuardianAppearanceParticle) e.cancel();
        if (ModuleManager.noRender.explosions.getValue() && particle instanceof ExplosionLargeParticle) e.cancel();
        if (ModuleManager.noRender.campFire.getValue() && particle instanceof CampfireSmokeParticle) e.cancel();
        if (ModuleManager.noRender.breakParticles.getValue() && particle instanceof BlockDustParticle) e.cancel();
        if (ModuleManager.noRender.fireworks.getValue() && (particle instanceof FireworksSparkParticle.FireworkParticle || particle instanceof FireworksSparkParticle.Flash))
            e.cancel();
    }
}
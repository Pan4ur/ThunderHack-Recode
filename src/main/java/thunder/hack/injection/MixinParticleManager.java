package thunder.hack.injection;

import net.minecraft.client.particle.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.render.NoRender;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
    @Inject(at = @At("HEAD"), method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", cancellable = true)
    public void addParticleHook(Particle p, CallbackInfo e) {
        NoRender nR = ModuleManager.noRender;

        if(!nR.isEnabled())
            return;
        
        if (nR.elderGuardian.getValue() && p instanceof ElderGuardianAppearanceParticle)
            e.cancel();

        if (nR.explosions.getValue() && p instanceof ExplosionLargeParticle)
            e.cancel();

        if (nR.campFire.getValue() && p instanceof CampfireSmokeParticle)
            e.cancel();

        if (nR.breakParticles.getValue() && p instanceof BlockDustParticle)
            e.cancel();

        if (nR.fireworks.getValue() && (p instanceof FireworksSparkParticle.FireworkParticle || p instanceof FireworksSparkParticle.Flash))
            e.cancel();
    }
}
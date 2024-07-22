package thunder.hack.injection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.core.impl.PlayerManager;
import thunder.hack.modules.combat.Aura;

import static thunder.hack.core.IManager.mc;

@Mixin(FireworkRocketEntity.class)
public class MixinFireWorkEntity {

    @Shadow
    private LivingEntity shooter;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d tickHook(LivingEntity instance) {
        if (ModuleManager.aura.isEnabled() && ModuleManager.aura.rotationMode.not(Aura.Mode.None)
                && ModuleManager.aura.target != null && shooter == mc.player && ModuleManager.aura.elytraTarget.getValue()) {

          //  float[] nonLimitedRotation = PlayerManager.calcAngle(ModuleManager.aura.target.getEyePos().add(0, 0.5, 0));
            return ThunderHack.playerManager.getRotationVector(ModuleManager.aura.rotationPitch, ModuleManager.aura.rotationYaw);
        }
        return shooter.getRotationVector();
    }
}

package thunder.hack.injection;

import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.utility.interfaces.ICrystal;

@Mixin(EndCrystalEntity.class)
public class MixinEndCrystal implements ICrystal {

    @Unique
    int attacks, cooldown;

    @Override
    public boolean canAttack() {
        return cooldown == 0;
    }

    @Override
    public void attack() {
        if (attacks++ >= 5)
            cooldown = 20;
    }

    @Inject(method = "tick", at = {@At("HEAD")})
    public void tickHook(CallbackInfo ci) {
        if (cooldown > 0)
            cooldown--;
    }
}
package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.Thunderhack;
import thunder.hack.modules.player.EntityControl;

@Mixin(AbstractHorseEntity.class)
public abstract class MixinAbstractHorseEntity extends AnimalEntity {
    protected MixinAbstractHorseEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    public void onIsSaddled(CallbackInfoReturnable<Boolean> cir) {
        if (Thunderhack.moduleManager.get(EntityControl.class).isEnabled()) cir.setReturnValue(true);
    }
}

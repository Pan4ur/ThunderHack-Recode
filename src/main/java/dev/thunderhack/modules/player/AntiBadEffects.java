package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import net.minecraft.entity.effect.StatusEffects;

public class AntiBadEffects extends Module {
    public AntiBadEffects() {
        super("AntiBadEffects", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        if (mc.player.hasStatusEffect(StatusEffects.NAUSEA)) mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
    }
}
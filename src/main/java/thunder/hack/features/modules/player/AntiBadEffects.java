package thunder.hack.features.modules.player;

import net.minecraft.entity.effect.StatusEffects;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class AntiBadEffects extends Module {
    public AntiBadEffects() {
        super("AntiBadEffects", Category.PLAYER);
    }

    private final Setting<Boolean> blindness = new Setting<>("Blindness", true);
    private final Setting<Boolean> nausea = new Setting<>("Nausea", true);
    private final Setting<Boolean> miningFatigue = new Setting<>("MiningFatigue", true);
    private final Setting<Boolean> levitation = new Setting<>("Levitation", true);
    private final Setting<Boolean> slowness = new Setting<>("Slowness", true);
    private final Setting<Boolean> jumpBoost = new Setting<>("JumpBoost", true);

    @Override
    public void onUpdate() {
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && blindness.getValue()) mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        if (mc.player.hasStatusEffect(StatusEffects.NAUSEA) && nausea.getValue()) mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE) && miningFatigue.getValue()) mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION) && levitation.getValue()) mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS) && slowness.getValue()) mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && jumpBoost.getValue()) mc.player.removeStatusEffect(StatusEffects.JUMP_BOOST);
    }
}
package dev.thunderhack.modules.movement;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import dev.thunderhack.event.events.EventMove;

public class LevitationControl extends Module {
    private final Setting<Integer> upAmplifier = new Setting<>("Up Speed", 1, 1, 5);
    private final Setting<Integer> downAmplifier = new Setting<>("Down Speed", 1, 1, 5);
    public LevitationControl() {
        super("LevitCtrl", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove e) {
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier();
            if (mc.options.jumpKey.isPressed()) e.setY(((0.05D * (double) (amplifier + 1) - e.getY()) * 0.2D) * upAmplifier.getValue() * 100);
            else if (mc.options.sneakKey.isPressed()) e.setY(-(((0.05D * (double) (amplifier + 1) - e.getY()) * 0.2D) * downAmplifier.getValue() * 100));
            else e.setY(0);
            e.cancel();
        }
    }
}

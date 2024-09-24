package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import thunder.hack.events.impl.EventMove;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class LevitationControl extends Module {
    public LevitationControl() {
        super("LevitCtrl", Category.MOVEMENT);
    }

    private final Setting<Integer> upAmplifier = new Setting<>("Up Speed", 1, 1, 5);
    private final Setting<Integer> downAmplifier = new Setting<>("Down Speed", 1, 1, 5);

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

package thunder.hack.modules.movement;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import thunder.hack.events.impl.EventMove;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

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
            if (mc.options.jumpKey.isPressed()) {
                e.set_y(((0.05D * (double) (amplifier + 1) - e.get_y()) * 0.2D) * upAmplifier.getValue());
            } else if (mc.options.sneakKey.isPressed()) {
                e.set_y(-(((0.05D * (double) (amplifier + 1) - e.get_y()) * 0.2D) * downAmplifier.getValue()));
            } else {
                e.set_y(0);
            }
        }
    }
}

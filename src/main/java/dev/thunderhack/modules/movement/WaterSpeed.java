package dev.thunderhack.modules.movement;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.math.MathUtility;
import dev.thunderhack.utils.player.MovementUtility;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import dev.thunderhack.event.events.EventMove;

public class WaterSpeed extends Module {
    public WaterSpeed() {
        super("WaterSpeed", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.HollyWorld);

    private float acceleration = 0f;

    public enum Mode {
        HollyWorld, HollyWorld2
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.HollyWorld) {
            if (mc.player.isSwimming()) mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 2, 2));
            else mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        }
    }

    @EventHandler
    public void onMove(EventMove e) {
        if (mode.getValue() == Mode.HollyWorld2) {
            if (mc.player.isSwimming()) {
                double[] dirSpeed = MovementUtility.forward(acceleration / (mc.player.input.movementSideways != 0 ? 2.2f : 2f));
                e.setX(e.getX() + dirSpeed[0]);
                e.setZ(e.getZ() + dirSpeed[1]);
                e.cancel();
                acceleration += 0.05f;
                acceleration = MathUtility.clamp(acceleration, 0f, 1f);
            } else acceleration = 0f;
            if (!MovementUtility.isMoving()) acceleration = 0f;
        }
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == Mode.HollyWorld) {
            mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        }
    }
}

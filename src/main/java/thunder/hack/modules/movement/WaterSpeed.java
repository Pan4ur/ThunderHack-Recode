package thunder.hack.modules.movement;

import net.minecraft.block.FluidBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class WaterSpeed extends Module {
    public WaterSpeed() {
        super("WaterSpeed", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.HollyWorld);

    public enum Mode{
        HollyWorld/*, Ignore*/
    }

    private boolean hasBefore = false;

    @Override
    public void onEnable() {
        if(mode.getValue() == Mode.HollyWorld) {
            hasBefore = mc.player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE);
            if (!hasBefore)
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 999999));
        }
    }


    @Override
    public void onDisable() {
        if(mode.getValue() == Mode.HollyWorld) {
            if (!hasBefore)
                mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        }
    }
}

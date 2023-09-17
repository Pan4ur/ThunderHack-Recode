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


    @Override
    public void onUpdate() {
        if(mode.getValue() == Mode.HollyWorld) {
            if(mc.player.isSwimming()){
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 2));
            } else {
                mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
            }
        }
    }

    @Override
    public void onDisable() {
        if(mode.getValue() == Mode.HollyWorld) {
            mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        }
    }
}

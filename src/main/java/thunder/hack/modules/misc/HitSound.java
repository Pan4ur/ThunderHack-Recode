package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.sound.SoundEvent;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.ThSoundPack;
import thunder.hack.utility.math.MathUtility;
import net.minecraft.sound.SoundCategory;

public class HitSound extends Module {
    public HitSound() {
        super("HitSound", "HitSound", Category.MISC);
    }

    private Setting<Mode> mode = new Setting<>("Sound", Mode.MOAN);
    public Setting<Float> volume = new Setting<>("Volume", 1f, 0.1f, 10f);
    public Setting<Float> pitch = new Setting<>("Pitch", 1f, 0.1f, 10f);

    public enum Mode {
        UWU, MOAN, SKEET, KEYBOARD
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!(event.getEntity() instanceof EndCrystalEntity)) {
            switch (mode.getValue()){
                case UWU -> playSound(ThSoundPack.UWU_SOUNDEVENT);
                case SKEET -> playSound(ThSoundPack.SKEET_SOUNDEVENT);
                case KEYBOARD ->  playSound(ThSoundPack.KEYPRESS_SOUNDEVENT);
                case MOAN -> {
                    SoundEvent sound = switch ((int) (MathUtility.random(0,5))) {
                        case 1 -> ThSoundPack.MOAN1_SOUNDEVENT;
                        case 2 -> ThSoundPack.MOAN2_SOUNDEVENT;
                        case 3 -> ThSoundPack.MOAN3_SOUNDEVENT;
                        case 4 -> ThSoundPack.MOAN4_SOUNDEVENT;
                        default -> ThSoundPack.MOAN5_SOUNDEVENT;
                    };
                    playSound(sound);
                }
            }
        }
    }

    private void playSound(SoundEvent sound){
        mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
    }

}

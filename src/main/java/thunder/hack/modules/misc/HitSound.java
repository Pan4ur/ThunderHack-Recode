package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
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

    private Setting<Mode> mode = new Setting("Sound", Mode.MOAN);
    public Setting<Float> volume = new Setting<>("Volume", 1f, 0.1f, 10f);
    public Setting<Float> pitch = new Setting<>("Pitch", 1f, 0.1f, 10f);

    public enum Mode {
        UWU, MOAN, SKEET, KEYBOARD
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!(event.getEntity() instanceof EndCrystalEntity)) {
            if(mode.getValue() == Mode.UWU){
                 mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.UWU_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
            }
            if(mode.getValue() == Mode.SKEET){
                mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.SKEET_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
            }
            if(mode.getValue() == Mode.KEYBOARD){
                mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.KEYPRESS_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
            }
            if(mode.getValue() == Mode.MOAN){
                int i = (int) (MathUtility.random(0,5));
                if(i == 0){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN1_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
                }
                if(i == 1){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN2_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
                }
                if(i == 2){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN3_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
                }
                if(i == 3){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN4_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
                }
                if(i == 5){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN5_SOUNDEVENT, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
                }
            }
        }
    }

}

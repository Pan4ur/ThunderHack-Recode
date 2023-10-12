package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.SoundUtility;
import thunder.hack.utility.math.MathUtility;

public class HitSound extends Module {
    private final Setting<Mode> mode = new Setting<>("Sound", Mode.MOAN);
    private final Setting<Float> volume = new Setting<>("Volume", 1f, 0.1f, 10f);
    private final Setting<Float> pitch = new Setting<>("Pitch", 1f, 0.1f, 10f);

    public HitSound() {
        super("HitSound", Category.MISC);
    }

    public enum Mode {
        UWU,
        MOAN,
        SKEET,
        KEYBOARD
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onAttack(@NotNull EventAttack event) {
        if (!(event.getEntity() instanceof EndCrystalEntity)) {
            switch (mode.getValue()) {
                case UWU -> playSound(SoundUtility.UWU_SOUNDEVENT);
                case SKEET -> playSound(SoundUtility.SKEET_SOUNDEVENT);
                case KEYBOARD -> playSound(SoundUtility.KEYPRESS_SOUNDEVENT);
                case MOAN -> {
                    SoundEvent sound = switch ((int) (MathUtility.random(0, 3))) {
                        case 0 -> SoundUtility.MOAN1_SOUNDEVENT;
                        case 1 -> SoundUtility.MOAN2_SOUNDEVENT;
                        case 2 -> SoundUtility.MOAN3_SOUNDEVENT;
                        default -> SoundUtility.MOAN4_SOUNDEVENT;
                    };
                    playSound(sound);
                }
            }
        }
    }

    private void playSound(SoundEvent sound) {
        mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, volume.getValue(), pitch.getValue());
    }
}

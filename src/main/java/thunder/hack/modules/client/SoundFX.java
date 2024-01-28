package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.DeathEvent;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.Aura;
import thunder.hack.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;
import thunder.hack.utility.SoundUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

import static thunder.hack.core.impl.ConfigManager.SOUNDS_FOLDER;
import static thunder.hack.modules.client.MainSettings.isRu;


public final class SoundFX extends Module {
    public SoundFX() {
        super("SoundFX", Category.CLIENT);
    }

    private final Setting<Integer> volume = new Setting<>("Volume", 100, 0, 100);
    private final Setting<OnOffSound> enableMode = new Setting<>("EnableMode", OnOffSound.Inertia);
    private final Setting<OnOffSound> disableMode = new Setting<>("DisableMode", OnOffSound.Inertia);
    private final Setting<HitSound> hitSound = new Setting<>("HitSound", HitSound.OFF);
    private final Setting<KillSound> killSound = new Setting<>("KillSound", KillSound.OFF);
    private final Setting<ScrollSound> scrollSound = new Setting<>("ScrollSound", ScrollSound.KeyBoard);

    private final Timer scrollTimer = new Timer();

    @EventHandler
    @SuppressWarnings("unused")
    public void onAttack(@NotNull EventAttack event) {
        if (!(event.getEntity() instanceof EndCrystalEntity)) {
            switch (hitSound.getValue()) {
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
                case CUSTOM -> playSound("hit");
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDeath(DeathEvent e) {
        if (Aura.target != null && Aura.target == e.getPlayer()) {
            playSound("kill");
            return;
        }
        if (AutoCrystal.target != null && AutoCrystal.target == e.getPlayer()) {
            playSound("kill");
        }
    }

    public void playEnable() {
        if (enableMode.getValue() == OnOffSound.Inertia) {
            playSound(SoundUtility.ENABLE_SOUNDEVENT);
        } else if (enableMode.getValue() == OnOffSound.Custom) {
            playSound("enable");
        }
    }

    public void playDisable() {
        if (disableMode.getValue() == OnOffSound.Inertia) {
            playSound(SoundUtility.DISABLE_SOUNDEVENT);
        } else if (disableMode.getValue() == OnOffSound.Custom) {
            playSound("disable");
        }
    }

    public void playScroll() {
        if (scrollTimer.every(50)) {
            if (scrollSound.getValue() == ScrollSound.KeyBoard) {
                playSound(SoundUtility.KEYPRESS_SOUNDEVENT);
            } else if (scrollSound.getValue() == ScrollSound.Custom) {
                playSound("scroll");
            }
        }
    }

    public void playSound(SoundEvent sound) {
        if (mc.player != null && mc.world != null)
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, (float) volume.getValue() / 100f, 1f);
    }

    public void playSound(String name) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(SOUNDS_FOLDER, name + ".wav").getAbsoluteFile()));
            FloatControl floatControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            floatControl.setValue((floatControl.getMaximum() - floatControl.getMinimum() * ((float) volume.getValue() / 100f)) + floatControl.getMinimum());
            clip.start();
        } catch (Exception e) {
            sendMessage(isRu() ? "Ошибка воспроизведения звука!" : "Error with playing sound!");
        }
    }

    private enum OnOffSound {
        Custom, Inertia, OFF
    }

    public enum HitSound {
        UWU, MOAN, SKEET, KEYBOARD, CUSTOM, OFF
    }

    private enum KillSound {
        Custom, OFF
    }

    private enum ScrollSound {
        Custom, OFF, KeyBoard
    }
}

package thunder.hack.core.manager.client;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import thunder.hack.core.manager.IManager;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.client.SoundFX;
import thunder.hack.features.modules.misc.ChatUtils;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

import static thunder.hack.features.cmd.Command.sendMessage;
import static thunder.hack.core.manager.client.ConfigManager.SOUNDS_FOLDER;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class SoundManager implements IManager {
    public final Identifier KEYPRESS_SOUND = Identifier.of("thunderhack:keypress");
    public SoundEvent KEYPRESS_SOUNDEVENT = SoundEvent.of(KEYPRESS_SOUND);
    public final Identifier KEYRELEASE_SOUND = Identifier.of("thunderhack:keyrelease");
    public SoundEvent KEYRELEASE_SOUNDEVENT = SoundEvent.of(KEYRELEASE_SOUND);
    public final Identifier UWU_SOUND = Identifier.of("thunderhack:uwu");
    public SoundEvent UWU_SOUNDEVENT = SoundEvent.of(UWU_SOUND);
    public final Identifier ENABLE_SOUND = Identifier.of("thunderhack:enable");
    public SoundEvent ENABLE_SOUNDEVENT = SoundEvent.of(ENABLE_SOUND);
    public final Identifier DISABLE_SOUND = Identifier.of("thunderhack:disable");
    public SoundEvent DISABLE_SOUNDEVENT = SoundEvent.of(DISABLE_SOUND);
    public final Identifier MOAN1_SOUND = Identifier.of("thunderhack:moan1");
    public SoundEvent MOAN1_SOUNDEVENT = SoundEvent.of(MOAN1_SOUND);
    public final Identifier MOAN2_SOUND = Identifier.of("thunderhack:moan2");
    public SoundEvent MOAN2_SOUNDEVENT = SoundEvent.of(MOAN2_SOUND);
    public final Identifier MOAN3_SOUND = Identifier.of("thunderhack:moan3");
    public SoundEvent MOAN3_SOUNDEVENT = SoundEvent.of(MOAN3_SOUND);
    public final Identifier MOAN4_SOUND = Identifier.of("thunderhack:moan4");
    public SoundEvent MOAN4_SOUNDEVENT = SoundEvent.of(MOAN4_SOUND);
    public final Identifier SKEET_SOUND = Identifier.of("thunderhack:skeet");
    public SoundEvent SKEET_SOUNDEVENT = SoundEvent.of(SKEET_SOUND);
    public final Identifier ORTHODOX_SOUND = Identifier.of("thunderhack:orthodox");
    public SoundEvent ORTHODOX_SOUNDEVENT = SoundEvent.of(ORTHODOX_SOUND);
    public final Identifier BOOLEAN_SOUND = Identifier.of("thunderhack:boolean");
    public SoundEvent BOOLEAN_SOUNDEVENT = SoundEvent.of(BOOLEAN_SOUND);
    public final Identifier SCROLL_SOUND = Identifier.of("thunderhack:scroll");
    public SoundEvent SCROLL_SOUNDEVENT = SoundEvent.of(SCROLL_SOUND);
    public final Identifier SWIPEIN_SOUND = Identifier.of("thunderhack:swipein");
    public SoundEvent SWIPEIN_SOUNDEVENT = SoundEvent.of(SWIPEIN_SOUND);
    public final Identifier SWIPEOUT_SOUND = Identifier.of("thunderhack:swipeout");
    public SoundEvent SWIPEOUT_SOUNDEVENT = SoundEvent.of(SWIPEOUT_SOUND);
    public final Identifier ALERT_SOUND = Identifier.of("thunderhack:alert");
    public SoundEvent ALERT_SOUNDEVENT = SoundEvent.of(ALERT_SOUND);
    public final Identifier PM_SOUND = Identifier.of("thunderhack:pmsound");
    public SoundEvent PM_SOUNDEVENT = SoundEvent.of(PM_SOUND);
    public final Identifier RIFK_SOUND = Identifier.of("thunderhack:rifk");
    public SoundEvent RIFK_SOUNDEVENT = SoundEvent.of(RIFK_SOUND);
    public final Identifier CUTIE_SOUND = Identifier.of("thunderhack:cutie");
    public SoundEvent CUTIE_SOUNDEVENT = SoundEvent.of(CUTIE_SOUND);


    private final Timer scrollTimer = new Timer();

    public void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, KEYPRESS_SOUND, KEYPRESS_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, KEYRELEASE_SOUND, KEYRELEASE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ENABLE_SOUND, ENABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, DISABLE_SOUND, DISABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN1_SOUND, MOAN1_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN2_SOUND, MOAN2_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN3_SOUND, MOAN3_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN4_SOUND, MOAN4_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, UWU_SOUND, UWU_SOUNDEVENT);

        Registry.register(Registries.SOUND_EVENT, SKEET_SOUND, SKEET_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ORTHODOX_SOUND, ORTHODOX_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SCROLL_SOUND, SCROLL_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, BOOLEAN_SOUND, BOOLEAN_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SWIPEIN_SOUND, SWIPEIN_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SWIPEOUT_SOUND, SWIPEOUT_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ALERT_SOUND, ALERT_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, PM_SOUND, PM_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, RIFK_SOUND, RIFK_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, CUTIE_SOUND, CUTIE_SOUNDEVENT);
    }

    public void playHitSound(SoundFX.HitSound value) {
        switch (value) {
            case UWU -> playSound(UWU_SOUNDEVENT);
            case SKEET -> playSound(SKEET_SOUNDEVENT);
            case KEYBOARD -> playSound(KEYPRESS_SOUNDEVENT);
            case CUTIE -> playSound(CUTIE_SOUNDEVENT);
            case MOAN -> {
                SoundEvent sound = switch ((int) (MathUtility.random(0, 3))) {
                    case 0 -> MOAN1_SOUNDEVENT;
                    case 1 -> MOAN2_SOUNDEVENT;
                    case 2 -> MOAN3_SOUNDEVENT;
                    default -> MOAN4_SOUNDEVENT;
                };
                playSound(sound);
            }
            case RIFK -> playSound(RIFK_SOUNDEVENT);
            case CUSTOM -> playSound("hit");
        }
    }

    public void playEnable() {
        if (ModuleManager.soundFX.enableMode.getValue() == SoundFX.OnOffSound.Inertia) {
            playSound(ENABLE_SOUNDEVENT);
        } else if (ModuleManager.soundFX.enableMode.getValue() == SoundFX.OnOffSound.Custom) {
            playSound("enable");
        }
    }

    public void playDisable() {
        if (ModuleManager.soundFX.disableMode.getValue() == SoundFX.OnOffSound.Inertia) {
            playSound(DISABLE_SOUNDEVENT);
        } else if (ModuleManager.soundFX.disableMode.getValue() == SoundFX.OnOffSound.Custom) {
            playSound("disable");
        }
    }

    public void playScroll() {
        if (scrollTimer.every(50)) {
            if (ModuleManager.soundFX.scrollSound.getValue() == SoundFX.ScrollSound.KeyBoard) {
                playSound(KEYPRESS_SOUNDEVENT);
            } else if (ModuleManager.soundFX.scrollSound.getValue() == SoundFX.ScrollSound.Custom) {
                playSound("scroll");
            }
        }
    }

    public void playSound(SoundEvent sound) {
        if (mc.player != null && mc.world != null)
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, (float) ModuleManager.soundFX.volume.getValue() / 100f, 1f);
    }

    public void playSound(String name) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(SOUNDS_FOLDER, name + ".wav").getAbsoluteFile()));
            FloatControl floatControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            floatControl.setValue((floatControl.getMaximum() - floatControl.getMinimum() * ((float) ModuleManager.soundFX.volume.getValue() / 100f)) + floatControl.getMinimum());
            clip.start();
        } catch (Exception e) {
            sendMessage((isRu() ? "Ошибка воспроизведения звука! Проверь " : "Error with playing sound! Check ") + new File(SOUNDS_FOLDER, name + ".wav").getAbsolutePath());
        }
    }

    public void playSlider() {
        playSound(SCROLL_SOUNDEVENT);
    }

    public void playBoolean() {
        playSound(BOOLEAN_SOUNDEVENT);
    }

    public void playSwipeIn() {
        playSound(SWIPEIN_SOUNDEVENT);
    }

    public void playSwipeOut() {
        playSound(SWIPEOUT_SOUNDEVENT);
    }

    public void playPmSound(ChatUtils.PMSound sound) {
        if (sound == ChatUtils.PMSound.Default) playSound(PM_SOUNDEVENT);
        else Managers.SOUND.playSound("pmsound");
    }
}

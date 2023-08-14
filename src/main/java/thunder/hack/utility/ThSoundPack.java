package thunder.hack.utility;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ThSoundPack {
    public static final Identifier KEYPRESS_SOUND = new Identifier("thunderhack:keypress");
    public static SoundEvent KEYPRESS_SOUNDEVENT = SoundEvent.of(KEYPRESS_SOUND);
    public static final Identifier KEYRELEASE_SOUND = new Identifier("thunderhack:keyrelease");
    public static SoundEvent KEYRELEASE_SOUNDEVENT = SoundEvent.of(KEYRELEASE_SOUND);
    public static final Identifier UWU_SOUND = new Identifier("thunderhack:uwu");
    public static SoundEvent UWU_SOUNDEVENT = SoundEvent.of(UWU_SOUND);
    public static final Identifier ENABLE_SOUND = new Identifier("thunderhack:enable");
    public static SoundEvent ENABLE_SOUNDEVENT = SoundEvent.of(ENABLE_SOUND);
    public static final Identifier DISABLE_SOUND = new Identifier("thunderhack:disable");
    public static SoundEvent DISABLE_SOUNDEVENT = SoundEvent.of(DISABLE_SOUND);
    public static final Identifier MOAN1_SOUND = new Identifier("thunderhack:moan1");
    public static SoundEvent MOAN1_SOUNDEVENT = SoundEvent.of(MOAN1_SOUND);
    public static final Identifier MOAN2_SOUND = new Identifier("thunderhack:moan2");
    public static SoundEvent MOAN2_SOUNDEVENT = SoundEvent.of(MOAN2_SOUND);
    public static final Identifier MOAN3_SOUND = new Identifier("thunderhack:moan3");
    public static SoundEvent MOAN3_SOUNDEVENT = SoundEvent.of(MOAN3_SOUND);
    public static final Identifier MOAN4_SOUND = new Identifier("thunderhack:moan4");
    public static SoundEvent MOAN4_SOUNDEVENT = SoundEvent.of(MOAN4_SOUND);
    public static final Identifier MOAN5_SOUND = new Identifier("thunderhack:moan5");
    public static SoundEvent MOAN5_SOUNDEVENT = SoundEvent.of(MOAN5_SOUND);
    public static final Identifier SKEET_SOUND = new Identifier("thunderhack:skeet");
    public static SoundEvent SKEET_SOUNDEVENT = SoundEvent.of(SKEET_SOUND);
    public static final Identifier ORTHODOX_SOUND = new Identifier("thunderhack:orthodox");
    public static SoundEvent ORTHODOX_SOUNDEVENT = SoundEvent.of(ORTHODOX_SOUND);

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.KEYPRESS_SOUND, ThSoundPack.KEYPRESS_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.KEYRELEASE_SOUND, ThSoundPack.KEYRELEASE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.ENABLE_SOUND, ThSoundPack.ENABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.DISABLE_SOUND, ThSoundPack.DISABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.MOAN1_SOUND, ThSoundPack.MOAN1_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.MOAN2_SOUND, ThSoundPack.MOAN2_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.MOAN3_SOUND, ThSoundPack.MOAN3_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.MOAN4_SOUND, ThSoundPack.MOAN4_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.MOAN5_SOUND, ThSoundPack.MOAN5_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.UWU_SOUND, ThSoundPack.UWU_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.SKEET_SOUND, ThSoundPack.SKEET_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ThSoundPack.ORTHODOX_SOUND, ThSoundPack.ORTHODOX_SOUNDEVENT);
    }
}

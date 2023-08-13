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

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, KEYPRESS_SOUND, KEYPRESS_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, KEYRELEASE_SOUND, KEYRELEASE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ENABLE_SOUND, ENABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, DISABLE_SOUND, DISABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN1_SOUND, MOAN1_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN2_SOUND, MOAN2_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN3_SOUND, MOAN3_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN4_SOUND, MOAN4_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN5_SOUND, MOAN5_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, UWU_SOUND, UWU_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SKEET_SOUND, SKEET_SOUNDEVENT);
    }
}

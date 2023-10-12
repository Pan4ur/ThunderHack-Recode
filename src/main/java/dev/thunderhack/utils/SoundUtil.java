package dev.thunderhack.utils;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class SoundUtil {
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
        Registry.register(Registries.SOUND_EVENT, SoundUtil.KEYPRESS_SOUND, SoundUtil.KEYPRESS_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.KEYRELEASE_SOUND, SoundUtil.KEYRELEASE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.ENABLE_SOUND, SoundUtil.ENABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.DISABLE_SOUND, SoundUtil.DISABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.MOAN1_SOUND, SoundUtil.MOAN1_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.MOAN2_SOUND, SoundUtil.MOAN2_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.MOAN3_SOUND, SoundUtil.MOAN3_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.MOAN4_SOUND, SoundUtil.MOAN4_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.MOAN5_SOUND, SoundUtil.MOAN5_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.UWU_SOUND, SoundUtil.UWU_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.SKEET_SOUND, SoundUtil.SKEET_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SoundUtil.ORTHODOX_SOUND, SoundUtil.ORTHODOX_SOUNDEVENT);
    }
}

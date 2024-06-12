package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventDeath;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.Aura;
import thunder.hack.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;


public final class SoundFX extends Module {
    public SoundFX() {
        super("SoundFX", Category.CLIENT);
    }

    public final Setting<Integer> volume = new Setting<>("Volume", 100, 0, 100);
    public final Setting<OnOffSound> enableMode = new Setting<>("EnableMode", OnOffSound.Inertia);
    public final Setting<OnOffSound> disableMode = new Setting<>("DisableMode", OnOffSound.Inertia);
    public final Setting<HitSound> hitSound = new Setting<>("HitSound", HitSound.OFF);
    public final Setting<KillSound> killSound = new Setting<>("KillSound", KillSound.OFF);
    public final Setting<ScrollSound> scrollSound = new Setting<>("ScrollSound", ScrollSound.KeyBoard);

    @EventHandler
    @SuppressWarnings("unused")
    public void onAttack(@NotNull EventAttack event) {
        if (!event.isPre())
            ThunderHack.soundManager.playHitSound(hitSound.getValue());
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onDeath(EventDeath e) {
        if (Aura.target != null && Aura.target == e.getPlayer() && killSound.is(KillSound.Custom)) {
            ThunderHack.soundManager.playSound("kill");
            return;
        }
        if (AutoCrystal.target != null && AutoCrystal.target == e.getPlayer() && killSound.is(KillSound.Custom)) {
            ThunderHack.soundManager.playSound("kill");
        }
    }

    public enum OnOffSound {
        Custom, Inertia, OFF
    }

    public enum HitSound {
        UWU, MOAN, SKEET, RIFK, KEYBOARD, CUSTOM, OFF
    }

    public enum KillSound {
        Custom, OFF
    }

    public enum ScrollSound {
        Custom, OFF, KeyBoard
    }
}

package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;

public class NoEntityTrace extends Module {
    public static final Setting<Boolean> ponly = new Setting<>("PickaxeOnly", true);
    public static final Setting<Boolean> noSword = new Setting<>("NoSword", true);

    public NoEntityTrace() {
        super("NoEntityTrace", Category.PLAYER);
    }
}

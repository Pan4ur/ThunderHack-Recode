package thunder.hack.modules.player;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class NoEntityTrace extends Module {
    public static final Setting<Boolean> ponly = new Setting<>("PickaxeOnly", true);
    public static final Setting<Boolean> noSword = new Setting<>("NoSword", true);

    public NoEntityTrace() {
        super("NoEntityTrace", "NoEntityTrace", Category.PLAYER);
    }
}

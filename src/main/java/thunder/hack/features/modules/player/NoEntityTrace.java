package thunder.hack.features.modules.player;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class NoEntityTrace extends Module {
    public static final Setting<Boolean> ponly = new Setting<>("Pickaxe Only", true);
    public static final Setting<Boolean> noSword = new Setting<>("No Sword", true);


    public NoEntityTrace() {
        super("NoEntityTrace", Category.PLAYER);
    }
}

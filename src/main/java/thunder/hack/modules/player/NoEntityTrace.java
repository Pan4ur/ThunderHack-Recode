package thunder.hack.modules.player;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public final class NoEntityTrace extends Module {
    public static final Setting<Boolean> ponly = new Setting<>("Pickaxe Only", true);
    public static final Setting<Boolean> noSword = new Setting<>("No Sword", true);
    public static final Setting<Boolean> ignoreCrystals = new Setting<>("Ignore Crystals", false);

    private static NoEntityTrace instance;

    public NoEntityTrace() {
        super("NoEntityTrace", Category.PLAYER);
        instance = this;
    }

    public static NoEntityTrace getInstance() {
        return instance;
    }
}

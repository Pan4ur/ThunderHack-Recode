package thunder.hack.features.modules.player;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoInteract extends Module {
    public NoInteract() {
        super("NoInteract", Category.PLAYER);
    }

    public static Setting<Boolean> onlyAura = new Setting<>("OnlyAura", false);
}

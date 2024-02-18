package thunder.hack.modules.misc;

import thunder.hack.ThunderHack;
import thunder.hack.modules.Module;

import java.util.List;

public class UnHook extends Module {
    public UnHook() {
        super("UnHook", Category.MISC);
    }

    List<Module> list;

    @Override
    public void onEnable() {
        list = ThunderHack.moduleManager.getEnabledModules();
        for (Module module : list) {
            if (module.equals(this)) {
                continue;
            }
            module.disable();
        }
    }

    @Override
    public void onDisable() {
        for (Module module : list) {
            if (module.equals(this)) {
                continue;
            }
            module.enable();
        }
    }
}

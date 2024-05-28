package thunder.hack.modules.misc;

import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class NameProtect extends Module {
    public NameProtect() {
        super("NameProtect", Category.MISC);
    }

    public static Setting<String> newName = new Setting<>("name", "Hell_Raider");
    public static Setting<Boolean> hideFriends = new Setting<>("Hide friends", true);

    public static String getCustomName() {
        return ModuleManager.nameProtect.isEnabled() ? newName.getValue().replaceAll("&", "\u00a7") : mc.getGameProfile().getName();
    }
}
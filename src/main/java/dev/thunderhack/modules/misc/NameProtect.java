package dev.thunderhack.modules.misc;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;

public class NameProtect extends Module {
    public NameProtect() {
        super("NameProtect", Category.MISC);
    }

    public static Setting<String> newName = new Setting<>("name", "Hell_Raider");
}
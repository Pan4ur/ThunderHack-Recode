package dev.thunderhack.modules.misc;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;

public class ItemScroller extends Module {
    public Setting<Integer> delay = new Setting<>("Delay",80,0,500);

    public ItemScroller() {
        super("ItemScroller", Category.MISC);
    }
}

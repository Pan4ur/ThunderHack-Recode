package thunder.hack.modules.misc;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class ItemScroller extends Module {
    public Setting<Integer> delay = new Setting<>("Delay",80,0,500);

    public ItemScroller() {
        super("ItemScroller", Category.MISC);
    }
}

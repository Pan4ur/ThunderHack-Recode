package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;

public class Reach extends Module {
    public Reach() {
        super("Reach", Category.COMBAT);
    }

    public static final Setting<Float> range = new Setting<>("Range", 3f, 0.1f, 10.0f);

    @Override
    public String getDisplayInfo() {
        return String.valueOf(range.getValue());
    }
}

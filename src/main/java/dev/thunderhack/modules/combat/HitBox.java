package dev.thunderhack.modules.combat;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;

public class HitBox extends Module {
    public HitBox() {
        super("HitBoxes", Category.COMBAT);
    }

    public static final Setting<Float> XZExpand = new Setting<>("XZExpand", 1.0f, 0.0f, 5.0f);
    public static final Setting<Float> YExpand = new Setting<>("YExpand", 0.0f, 0.0f, 5.0f);

    @Override
    public String getDisplayInfo() {
        return "H: " + XZExpand.getValue() + " V: " + YExpand.getValue();
    }
}

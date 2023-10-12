package dev.thunderhack.modules.render;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;

public class FOV extends Module {
    public final Setting<Integer> fovModifier = new Setting<>("FOV modifier", 120, 0, 358);
    public final Setting<Boolean> itemFov = new Setting<>("Item Fov", false);
    public final Setting<Integer> itemFovModifier = new Setting<>("Item FOV modifier", 120, 0, 358);

    public FOV() {
        super("FOV", Category.RENDER);
    }
}

package thunder.hack.features.modules.render;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", Category.RENDER);
    }

    public static Setting<Float> minBright = new Setting<>("MinBright", 0.5f, 0f, 1f);
}

package thunder.hack.modules.render;

import org.jetbrains.annotations.NotNull;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class AspectRatio extends Module {

    public AspectRatio() {
        super("AspectRatio", Category.RENDER);
    }

    public Setting<Float> ratio = new Setting<>("Ratio", 1.78f, 0.1f, 5f);
}

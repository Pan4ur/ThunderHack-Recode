package thunder.hack.modules.render;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class Animations extends Module {
    public Animations() {
        super("Animations", "Animations", Category.RENDER);
    }

    //public static final Setting<Mode> mode = new Setting("Mode", Mode.IDK);
    public enum Mode {
        Swipe, Rich, Glide, Default, New, Oblique, Fap, Slow, IDK
    }

}

package thunder.hack.features.modules.client;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class Notifications extends Module {
    public Notifications() {
        super("Notifications", Category.CLIENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.CrossHair);

    public enum Mode {
        Default, CrossHair, Text
    }
}

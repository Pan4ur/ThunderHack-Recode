package thunder.hack.modules.client;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public final class Notifications extends Module {

    public Notifications() {
        super("Notifications", Category.CLIENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Default);

    public enum Mode {
        Default, CrossHair, Text, Programming
    }
}

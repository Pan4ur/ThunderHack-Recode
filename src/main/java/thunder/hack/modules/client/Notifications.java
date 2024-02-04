package thunder.hack.modules.client;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public final class Notifications extends Module {
    private static Notifications instance;

    public Notifications() {
        super("Notifications", Category.CLIENT);
        instance = this;
    }

    public static Notifications getInstance() {
        return instance;
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Default);

    public enum Mode {
        Default, CrossHair, Text
    }
}

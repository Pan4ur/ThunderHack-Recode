package thunder.hack.modules.client;

import thunder.hack.modules.Module;

public final class Notifications extends Module {
    private static Notifications instance;

    public Notifications() {
        super("Notifications", Category.CLIENT);
        instance = this;
    }

    public static Notifications getInstance() {
        return instance;
    }
}

package thunder.hack.modules.client;

import thunder.hack.modules.Module;

public final class OptifineCapes extends Module {
    private static OptifineCapes instance;

    public OptifineCapes() {
        super("OptifineCapes", Category.CLIENT);
        instance = this;
    }

    public static OptifineCapes getInstance() {
        return instance;
    }
}

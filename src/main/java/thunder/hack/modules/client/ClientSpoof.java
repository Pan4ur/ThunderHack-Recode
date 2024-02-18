package thunder.hack.modules.client;

import org.jetbrains.annotations.NotNull;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class ClientSpoof extends Module {

    public ClientSpoof() {
        super("ClientSpoof", Category.CLIENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Vanilla);

    public enum Mode {
        Vanilla, Lunar, Null
    }

    public String getClientName() {
        switch (mode.getValue()) {
            case Vanilla -> {
                return "vanilla";
            }
            case Lunar -> {
                return "lunarclient:1.20.4";
            }
            default ->
            {
                return null;
            }
        }
    }
}

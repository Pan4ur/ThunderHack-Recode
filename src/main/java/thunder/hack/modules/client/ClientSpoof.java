package thunder.hack.modules.client;

import org.jetbrains.annotations.NotNull;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class ClientSpoof extends Module {

    public ClientSpoof() {
        super("ClientSpoof", Category.CLIENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Vanilla);
    private final Setting<String> custom = new Setting<>("Client", "feather", v-> mode.getValue() == Mode.Custom);

    public enum Mode {
        Vanilla, Lunar1_20_4, Lunar1_20_1, Custom, Null
    }

    public String getClientName() {
        switch (mode.getValue()) {
            case Vanilla -> {
                return "vanilla";
            }
            case Lunar1_20_4 -> {
                return "lunarclient:1.20.4";
            }
            case Lunar1_20_1 -> {
                return "lunarclient:1.20.1";
            }
            case Custom -> {
                return (String) custom.getValue();
            }
            default ->
            {
                return null;
            }
        }
    }
}

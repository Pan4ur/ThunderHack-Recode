package thunder.hack.modules.movement;

import org.jetbrains.annotations.NotNull;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class NoPush extends Module {
    public NoPush() {
        super("NoPush", Category.MOVEMENT);
    }

    public Setting<Boolean> blocks = new Setting<>("Blocks", true);
    public Setting<Boolean> players = new Setting<>("Players", true);
    public Setting<Boolean> water = new Setting<>("Liquids", true);




}

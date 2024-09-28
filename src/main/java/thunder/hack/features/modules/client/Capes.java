package thunder.hack.features.modules.client;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class Capes extends Module {
    public Capes() {
        super("Capes", Category.CLIENT);
    }

    public Setting<Boolean> optifineCapes = new Setting<>("Optifine", true);
    public Setting<Boolean> minecraftcapesCapes = new Setting<>("Minecraftcapes.net", false);
    public Setting<capePriority> priority = new Setting<>("Priority", capePriority.Optifine);

    public enum capePriority { Optifine, Minecraftcapes}
}

package thunder.hack.features.modules.combat;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class Reach extends Module {
    public Reach() {
        super("Reach", Category.COMBAT);
    }

    public final Setting<Float> blocksRange = new Setting<>("BlocksRange", 3f, 0.1f, 10.0f);
    public final Setting<Float> entityRange = new Setting<>("EntityRange", 3f, 0.1f, 10.0f);

    @Override
    public String getDisplayInfo() {
        return "B: " + blocksRange.getValue() + " E:" + entityRange.getValue();
    }
}

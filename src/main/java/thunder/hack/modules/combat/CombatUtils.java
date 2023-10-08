package thunder.hack.modules.combat;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;

public class CombatUtils extends Module {
    // Packet Mine
    private final Setting<Parent> packetMine = new Setting<>("Mining", new Parent(false, 0));
    private final Setting<Boolean> packetMineEnabled = new Setting<>("Packet Mine", true);

    public CombatUtils() {
        super("CombatUtils", Category.COMBAT);
    }
}

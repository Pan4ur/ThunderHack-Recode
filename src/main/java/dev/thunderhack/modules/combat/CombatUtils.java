package dev.thunderhack.modules.combat;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.Parent;

public class CombatUtils extends Module {
    // Packet Mine
    private final Setting<Parent> packetMine = new Setting<>("Mining", new Parent(false, 0));
    private final Setting<Boolean> packetMineEnabled = new Setting<>("Packet Mine", true);

    public CombatUtils() {
        super("CombatUtils", Category.COMBAT);
    }
}

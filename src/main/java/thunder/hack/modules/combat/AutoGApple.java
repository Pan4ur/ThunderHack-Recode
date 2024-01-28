package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public final class AutoGApple extends Module {
    public final Setting<Integer> Delay = new Setting("UseDelay", 0, 0, 2000);
    private final Setting<Float> health = new Setting<>("health", 15f, 1f, 36f);
    public Setting<Boolean> absorption = new Setting<>("Absorption", false);

    private boolean isActive;
    private final Timer useDelay = new Timer();

    private static AutoGApple instance;

    public AutoGApple() {
        super("AutoGApple", Category.COMBAT);
        instance = this;
    }

    @EventHandler
    public void onUpdate(PostPlayerUpdateEvent e) {
        if (fullNullCheck()) return;
        if (GapInOffHand()) {
            if (mc.player.getHealth() + (absorption.getValue() ? mc.player.getAbsorptionAmount() : 0) <= health.getValue() && useDelay.passedMs(Delay.getValue())) {
                isActive = true;
                mc.options.useKey.setPressed(true);
            } else if (isActive) {
                isActive = false;
                mc.options.useKey.setPressed(false);
            }
        } else if (isActive) {
            isActive = false;
            mc.options.useKey.setPressed(false);
        }
    }

    private boolean GapInOffHand() {
        return !mc.player.getOffHandStack().isEmpty() && (mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE);
    }

    public static AutoGApple getInstance() {
        return instance;
    }
}

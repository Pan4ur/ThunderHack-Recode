package dev.thunderhack.modules.player;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.math.MathUtility;
import net.minecraft.entity.player.PlayerEntity;
import dev.thunderhack.ThunderHack;

import java.util.Arrays;
import java.util.List;

public class AutoSex extends Module {
    private final Setting<Integer> targetRange = new Setting<>("Target Range", 5, 1, 10);
    private final Setting<SexMode> mode = new Setting<>("Sex Mode", SexMode.Active);
    private final Setting<Integer> msgDelay = new Setting<>("Message Delay", 1, 0, 50);

    private enum SexMode {
        Active,
        Passive
    }

    private static final String[] PASSIVE_MESSAGES = {
            "It's so Biiiiiiig",
            "Be careful daddy <3",
            "Oh, I feel it inside me!"
    };
    private static final String[] ACTIVE_MESSAGES = {
            "Oh, I'm cumming!",
            "Oh, ur pussy is so nice!",
            "Yeah, yeah",
            "I feel u!",
            "Oh, im inside u"
    };

    private PlayerEntity target;
    private final Timer messageTimer = new Timer();
    private final Timer sneakTimer = new Timer();

    public AutoSex() {
        super("AutoSex", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;
        if (target == null) {
            target = ThunderHack.combatManager.getNearestTarget(targetRange.getValue());
            return;
        }
        if (target.getPos().squaredDistanceTo(mc.player.getPos()) >= targetRange.getPow2Value()) {
            target = null;
            return;
        }

        switch (mode.getValue()) {
            case Active -> {
                if (sneakTimer.passedMs((long) MathUtility.random(80, 120))) {
                    mc.options.sneakKey.setPressed(!mc.options.sneakKey.isPressed());
                    sneakTimer.reset();
                }
            }
            case Passive -> {
                if (!mc.options.sneakKey.isPressed())
                    mc.options.sneakKey.setPressed(true);
            }
        }

        if (messageTimer.passedMs(msgDelay.getValue() * 1000)) {
            List<String> messages = Arrays.stream(mode.getValue() == SexMode.Active ? ACTIVE_MESSAGES : PASSIVE_MESSAGES).toList();
            mc.getNetworkHandler().sendChatCommand("msg " + target.getName().getString() + " " + messages.get((int) (Math.random() * messages.size())));
            messageTimer.reset();
        }
    }
}

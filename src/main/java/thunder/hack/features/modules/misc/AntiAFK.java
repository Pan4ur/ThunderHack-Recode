package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventKeyboardInput;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.MovementUtility;

import java.util.concurrent.ThreadLocalRandom;

public class AntiAFK extends Module {
    public AntiAFK() {
        super("AntiAFK", Category.MISC);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Simple);
    private final Setting<Boolean> onlyWhenAfk = new Setting<>("OnlyWhenAFK", false);
    private final Setting<Boolean> command = new Setting<>("Command", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> move = new Setting<>("Move", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> spin = new Setting<>("Spin", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Float> rotateSpeed = new Setting<>("RotateSpeed", 5f, 1f, 7f, v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> jump = new Setting<>("Jump", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> swing = new Setting<>("Swing", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> alwayssneak = new Setting<>("AlwaysSneak", false, v -> mode.getValue() == Mode.Simple);
    private final Setting<Integer> radius = new Setting<>("Radius", 64, 1, 128, v -> mode.getValue() == Mode.Baritone);

    private int step;
    private Timer inactiveTime = new Timer();

    private enum Mode {
        Simple, Baritone
    }

    @Override
    public void onEnable() {
        if (alwayssneak.getValue())
            mc.options.sneakKey.setPressed(true);

        step = 0;
    }

    @EventHandler
    public void onSettingChange(EventSetting e) {
        if (e.getSetting() == mode)
            step = 0;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onKeyboardInput(EventKeyboardInput e) {
        if (mc.player != null && mode.is(Mode.Simple) && !MovementUtility.isMoving() && move.getValue() && isAfk()) {
            float angleToRad = (float) Math.toRadians(9 * (mc.player.age % 40));

            float sin = (float) Math.clamp(Math.sin(angleToRad), -1, 1);
            float cos = (float) Math.clamp(Math.cos(angleToRad), -1, 1);

            mc.player.input.movementForward = Math.round(sin);
            mc.player.input.movementSideways = Math.round(cos);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUpdate(PlayerUpdateEvent e) {
        if (mode.is(Mode.Simple) ? isActive() : Managers.PLAYER.currentPlayerSpeed > 0.07)
            inactiveTime.reset();

        if (mode.getValue() == Mode.Simple) {
            if(!isAfk()) return;

            if(move.getValue())
                mc.player.setSprinting(false);

            if (spin.getValue()) {
                double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
                float newYaw = mc.player.getYaw() + rotateSpeed.getValue();
                mc.player.setYaw((float) (newYaw - (newYaw - mc.player.getYaw()) % gcdFix));
            }

            if (jump.getValue() && mc.player.isOnGround())
                mc.player.jump();

            if (swing.getValue() && ThreadLocalRandom.current().nextInt(99) == 0)
                mc.player.swingHand(mc.player.getActiveHand());

            if (command.getValue() && ThreadLocalRandom.current().nextInt(99) == 0)
                mc.player.networkHandler.sendChatCommand("qwerty");

        } else {
            if (inactiveTime.every(5000)) {
                if (step > 3)
                    step = 0;

                switch (step) {
                    case 0: {
                        mc.player.networkHandler.sendChatMessage("#goto ~ ~" + radius.getValue());
                        break;
                    }
                    case 1: {
                        mc.player.networkHandler.sendChatMessage("#goto ~" + radius.getValue() + " ~");
                        break;
                    }
                    case 2: {
                        mc.player.networkHandler.sendChatMessage("#goto ~ ~-" + radius.getValue());
                        break;
                    }
                    case 3: {
                        mc.player.networkHandler.sendChatMessage("#goto ~-" + radius.getValue() + " ~");
                        break;
                    }
                }
                step++;
            }
        }
    }

    @Override
    public void onDisable() {
        if (alwayssneak.getValue())
            mc.options.sneakKey.setPressed(false);

        if (mode.getValue() == Mode.Baritone)
            mc.player.networkHandler.sendChatMessage("#stop");
    }

    private boolean isAfk() {
        return !onlyWhenAfk.getValue() || inactiveTime.passedS(10);
    }

    private boolean isActive() {
        return mc.options.forwardKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed() || mc.options.backKey.isPressed();
    }
}

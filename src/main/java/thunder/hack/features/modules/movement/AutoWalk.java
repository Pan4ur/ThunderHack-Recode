package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.EventKeyboardInput;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class AutoWalk extends Module {
    public AutoWalk() {
        super("AutoWalk", Category.MOVEMENT);
    }

    private final Setting<AutoWalk.Mode> mode = new Setting<>("Mode", AutoWalk.Mode.Simple);

    public enum Mode {
        Simple, Baritone
    }

    @Override
    public void onEnable() {
        if (mode.getValue() == Mode.Baritone)
            mc.player.networkHandler.sendChatMessage("#goto " + 3000000 * Math.cos(Math.toRadians(mc.player.getYaw() + 90f)) + " " + 3000000 * Math.sin(Math.toRadians(mc.player.getYaw() + 90f)));
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == AutoWalk.Mode.Baritone) {
            mc.player.networkHandler.sendChatMessage("#stop");
        }
    }

    @EventHandler
    public void onKey(EventKeyboardInput e) {
        if (mode.getValue() == Mode.Simple)
            mc.player.input.movementForward = 1f;
    }
}

package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import thunder.hack.events.impl.EventKeyboardInput;
import thunder.hack.modules.Module;
import thunder.hack.modules.Module;
import thunder.hack.modules.misc.AntiAFK;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

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
<<<<<<< Updated upstream
        if (mode.getValue() == Mode.Baritone) {
            String direction = mc.player.getHorizontalFacing().toString();
            if ("east" == direction) {
                mc.player.networkHandler.sendChatMessage("#goto 30000000 ~ ~");
            }
            if ("west" == direction) {
                mc.player.networkHandler.sendChatMessage("#goto -30000000 ~ ~");
            }
            if ("north" == direction) {
                mc.player.networkHandler.sendChatMessage("#goto ~ ~ -30000000");
            }
            if ("south" == direction) {
                mc.player.networkHandler.sendChatMessage("#goto ~ ~ 30000000");
            }
        }
=======
        if (mode.getValue() == Mode.Baritone)
            mc.player.networkHandler.sendChatMessage("#goto " + 3000000 * Math.cos(Math.toRadians(mc.player.getYaw() + 90f)) + " " + 3000000 * Math.sin(Math.toRadians(mc.player.getYaw() + 90f)));
>>>>>>> Stashed changes
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == AutoWalk.Mode.Baritone) {
            mc.player.networkHandler.sendChatMessage("#stop");
        }
    }

    @EventHandler
    public void onKey(EventKeyboardInput e) {
<<<<<<< Updated upstream
        if (mode.getValue() == Mode.Simple) {
            mc.player.input.movementForward = 1f;
        }
=======
        if (mode.getValue() == Mode.Simple)
            mc.player.input.movementForward = 1f;
>>>>>>> Stashed changes
    }
}

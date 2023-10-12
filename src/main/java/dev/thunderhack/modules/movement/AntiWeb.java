package dev.thunderhack.modules.movement;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.player.MovementUtility;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.util.math.BlockPos;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.event.events.EventCollision;
import dev.thunderhack.event.events.PlayerUpdateEvent;

public class AntiWeb extends Module {
    public AntiWeb() {
        super("AntiWeb", Category.MOVEMENT);
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.Solid);
    public static final Setting<Float> timer = new Setting<>("Timer", 20f, 1f, 50f, v -> mode.getValue() == Mode.Timer);
    public Setting<Float> speed = new Setting<>("Speed", 0.3f, 0.0f, 10.0f, v -> mode.getValue() == Mode.Fly);

    public enum Mode {
        Timer, Solid, Ignore, Fly
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent e) {
        if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos())).getBlock() == Blocks.COBWEB) {
            if (mode.getValue() == Mode.Timer) {
                if (mc.player.isOnGround()) ThunderHack.TICK_TIMER = 1f;
                else ThunderHack.TICK_TIMER = timer.getValue();
            }
            if (mode.getValue() == Mode.Fly) {
                final double[] dir = MovementUtility.forward(speed.getValue());
                mc.player.setVelocity(dir[0], 0, dir[1]);
                if (mc.options.jumpKey.isPressed())
                    mc.player.setVelocity(mc.player.getVelocity().add(0, speed.getValue(), 0));
                if (mc.options.sneakKey.isPressed())
                    mc.player.setVelocity(mc.player.getVelocity().add(0, -speed.getValue(), 0));
            }
        }
    }

    @EventHandler
    public void onCollide(EventCollision e) {
        if (e.getState().getBlock() instanceof CobwebBlock && mode.getValue() == Mode.Solid)
            e.setState(Blocks.DIRT.getDefaultState());
    }
}

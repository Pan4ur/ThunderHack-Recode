package dev.thunderhack.modules.movement;

import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;

public class ReverseStep extends Module {
    public ReverseStep() {
        super("ReverseStep", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Motion);
    public Setting<Float> timer = new Setting<>("Timer", 3.0F, 1F, 10.0F, v -> mode.getValue() == Mode.Timer);
    public Setting<Float> motion = new Setting<>("Motion", 1.0F, 0.1F, 10.0F, v -> mode.getValue() == Mode.Motion);
    public Setting<Boolean> anyblock = new Setting<>("AnyBlock", false);
    public Setting<Boolean> pauseIfShift = new Setting<>("PauseIfShift", false);

    private boolean disableTimer = true;
    private boolean prevGround = false;

    @EventHandler
    public void onEntitySync(EventSync eventPlayerUpdateWalking) {
        if (ModuleManager.packetFly.isEnabled()) return;

        BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());

        if (pauseIfShift.getValue() && mc.options.sneakKey.isPressed()) {
            disableTimer();
            return;
        }

        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava() || mc.player.isFallFlying() || mc.player.getAbilities().flying || mc.world.getBlockState(playerPos).getBlock() == Blocks.COBWEB) {
            disableTimer();
            return;
        }

        if (checkBlock(mc.world.getBlockState(playerPos.down(2))) || checkBlock(mc.world.getBlockState(playerPos.down(3))) || checkBlock(mc.world.getBlockState(playerPos.down(4))))
            doStep();

        if (disableTimer && (mc.player.isOnGround())) {
            disableTimer = false;
            ThunderHack.TICK_TIMER = 1.0f;
        }

        prevGround = mc.player.isOnGround();
    }

    private void disableTimer() {
        if (disableTimer)
            ThunderHack.TICK_TIMER = 1f;
    }

    private boolean checkBlock(BlockState bs) {
        return bs.getBlock() == Blocks.BEDROCK || bs.getBlock() != Blocks.OBSIDIAN || anyblock.getValue();
    }

    private void doStep() {
        if (!(mode.getValue() != Mode.Timer || !prevGround || mc.player.isOnGround() || !(mc.player.getVelocity().getY() < -0.1) || disableTimer)) {
            ThunderHack.TICK_TIMER = timer.getValue();
            disableTimer = true;
        }

        if (mc.player.isOnGround() && mode.getValue() == Mode.Motion) {
            mc.player.setVelocity(mc.player.getVelocity().add(0, -motion.getValue(), 0));
        }
    }

    public enum Mode {
        Timer, Motion
    }
}
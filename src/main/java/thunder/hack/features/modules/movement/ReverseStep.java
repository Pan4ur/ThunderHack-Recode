package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ReverseStep extends Module {
    public ReverseStep() {
        super("ReverseStep", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Motion);
    private final Setting<Float> timer = new Setting<>("Timer", 3.0F, 1F, 10.0F, v -> mode.getValue() == Mode.Timer);
    private final Setting<Float> motion = new Setting<>("Motion", 1.0F, 0.1F, 10.0F, v -> mode.getValue() == Mode.Motion);
    private final Setting<Boolean> anyblock = new Setting<>("AnyBlock", false);
    private final Setting<Boolean> pauseIfShift = new Setting<>("PauseIfShift", false);

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

        if (mc.player.isOnGround() && mode.getValue() == Mode.Motion)
            mc.player.setVelocity(mc.player.getVelocity().add(0, -motion.getValue(), 0));
    }

    public enum Mode {
        Timer, Motion
    }
}
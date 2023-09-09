package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class ReverseStep extends Module {
    public Setting<Float> timer = new Setting("Timer", 3.0F, 1F, 10.0F);
    public Setting<Boolean> anyblock = new Setting<>("AnyBlock", false);
    public Setting<Boolean> pauseIfShift = new Setting<>("PauseIfShift", false);

    private boolean flag = true;
    private boolean prevGround = false;
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Motion);

    public ReverseStep() {
        super("ReverseStep", "быстро падать", Category.MOVEMENT);
    }

    @EventHandler
    public void onEntitySync(EventSync eventPlayerUpdateWalking) {
        if (ThunderHack.moduleManager.get(PacketFly.class).isEnabled()) return;

        if(pauseIfShift.getValue() && mc.options.sneakKey.isPressed()) return;

        BlockState iBlockState = mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).down(2));
        BlockState iBlockState2 = mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).down(3));
        BlockState iBlockState3 = mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).down(4));
        if (!(iBlockState.getBlock() != Blocks.BEDROCK && iBlockState.getBlock() != Blocks.OBSIDIAN && !(anyblock.getValue()) || mc.player.isInLava() || mc.player.isSubmergedInWater() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB) || mc.player.isFallFlying() || mc.player.getAbilities().flying)) {
            if (mc.player.isOnGround() && mode.getValue() == Mode.Motion) {
                mc.player.setVelocity(mc.player.getVelocity().add(0,-1,0));
            }
            if (!(mode.getValue() != Mode.Timer || !prevGround || mc.player.isOnGround() || !(mc.player.getVelocity().getY() < -0.1) || flag || mc.player.isInLava() || mc.player.isSubmergedInWater() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB) || mc.player.isFallFlying() || mc.player.getAbilities().flying)) {
                ThunderHack.TICK_TIMER = timer.getValue();
                flag = true;
            }
        } else if (!(iBlockState2.getBlock() != Blocks.BEDROCK && iBlockState2.getBlock() != Blocks.OBSIDIAN && !anyblock.getValue() || mc.player.isInLava() || mc.player.isSubmergedInWater() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB) || mc.player.isFallFlying() || mc.player.getAbilities().flying)) {
            if (mc.player.isOnGround() && mode.getValue() == Mode.Motion) {
                mc.player.setVelocity(mc.player.getVelocity().add(0,-1,0));
            }
            if (!(mode.getValue() != Mode.Timer || !prevGround ||  mc.player.isOnGround() || !(mc.player.getVelocity().getY() < -0.1) || flag || mc.player.isInLava() || mc.player.isSubmergedInWater() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB) || mc.player.isFallFlying() || mc.player.getAbilities().flying)) {
                ThunderHack.TICK_TIMER = timer.getValue();
                flag = true;
            }
        } else if (!(iBlockState3.getBlock() != Blocks.BEDROCK && iBlockState3.getBlock() != Blocks.OBSIDIAN && !anyblock.getValue() || mc.player.isInLava() || mc.player.isSubmergedInWater() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB) || mc.player.isFallFlying() || mc.player.getAbilities().flying)) {
            if (mc.player.isOnGround() && mode.getValue() == Mode.Motion) {
                mc.player.setVelocity(mc.player.getVelocity().add(0,-1,0));
            }
            if (!(mode.getValue() != Mode.Timer || !prevGround || mc.player.isOnGround() || !(mc.player.getVelocity().getY() < -0.1) || flag || mc.player.isInLava() || mc.player.isSubmergedInWater() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB) || mc.player.isFallFlying() || mc.player.getAbilities().flying)) {
                ThunderHack.TICK_TIMER = timer.getValue();
                flag = true;
            }
        }
        if (flag && (mc.player.isOnGround() || mc.player.isInLava() || mc.player.isSubmergedInWater() || (mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY())), (int) Math.floor(mc.player.getZ()))).getBlock() == Blocks.COBWEB) || mc.player.isFallFlying() || mc.player.getAbilities().flying)) {
            flag = false;
            ThunderHack.TICK_TIMER = (1.0f);
        }
        prevGround = mc.player.isOnGround();
    }


    public enum Mode {
        Timer, Motion
    }

}
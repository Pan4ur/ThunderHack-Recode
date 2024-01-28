package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import thunder.hack.events.impl.EventBreakBlock;
import thunder.hack.events.impl.EventCollision;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;
import thunder.hack.utility.player.PlayerUtility;

import static thunder.hack.modules.player.AutoTool.getTool;

public class Phase extends Module {

    public Phase() {
        super("Phase", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Vanilla);
    private final Setting<Boolean> silent = new Setting<>("Silent", false, v -> mode.getValue() == Mode.Sunrise);
    private final Setting<Boolean> waitBreak = new Setting<>("WaitBreak", true, v -> mode.getValue() == Mode.Sunrise);
    private final Setting<Integer> afterBreak = new Setting<>("BreakTimeout", 4, 1, 20, v -> mode.getValue() == Mode.Sunrise && waitBreak.getValue());
    private final Setting<Integer> afterPearl = new Setting<>("PearlTimeout", 0, 0, 60, v -> mode.getValue() == Mode.Pearl);

    public int clipTimer;
    public int afterPearlTime;

    private enum Mode {
        Vanilla, Pearl, Sunrise
    }

    @EventHandler
    public void onCollide(EventCollision e) {
        if(fullNullCheck())
            return;
        BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());

        if (mode.getValue() != Mode.Pearl && canNoClip() || afterPearlTime > 0) {
            if (!e.getPos().equals(playerPos.down()) || mc.options.sneakKey.isPressed())
                e.setState(Blocks.AIR.getDefaultState());
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if(fullNullCheck()) return;
        if (clipTimer > 0) clipTimer--;
        if (afterPearlTime > 0) afterPearlTime--;

        if (mode.getValue() == Mode.Sunrise && (mc.player.horizontalCollision || playerInsideBlock()) && !mc.player.isSubmergedInWater() && !mc.player.isInLava()) {
            double[] dir = MovementUtility.forward(0.5);

            BlockPos blockToBreak = null;

            if (mc.options.jumpKey.isPressed()) {
                blockToBreak = BlockPos.ofFloored(mc.player.getX() + dir[0], mc.player.getY() + 2, mc.player.getZ() + dir[1]);
            } else if (mc.options.sneakKey.isPressed()) {
                blockToBreak = BlockPos.ofFloored(mc.player.getX() + dir[0], mc.player.getY() - 1, mc.player.getZ() + dir[1]);
            } else if (MovementUtility.isMoving()) {
                blockToBreak = BlockPos.ofFloored(mc.player.getX() + dir[0], mc.player.getY(), mc.player.getZ() + dir[1]);
            }

            if (blockToBreak == null) return;
            int best_tool = getTool(blockToBreak);
            if (best_tool == -1) return;

            int prevItem = mc.player.getInventory().selectedSlot;


            InventoryUtility.switchTo(best_tool);

            mc.interactionManager.updateBlockBreakingProgress(blockToBreak, mc.player.getHorizontalFacing());
            mc.player.swingHand(Hand.MAIN_HAND);

            if (silent.getValue())
                InventoryUtility.switchTo(prevItem);
        }

        if (mode.getValue() == Mode.Pearl) {
            if(mc.player.horizontalCollision && !playerInsideBlock() && clipTimer <= 0 && mc.player.age > 60){
                double[] dir = MovementUtility.forward(0.5);
                BlockPos block = BlockPos.ofFloored(mc.player.getX() + dir[0], mc.player.getY(), mc.player.getZ() + dir[1]);

                if(mc.options.sneakKey.isPressed())
                    return;

                float[] angle = InteractionUtility.calculateAngle(block.toCenterPos());
                int epSlot = findEPSlot();
                int prevItem = mc.player.getInventory().selectedSlot;

                if (epSlot != -1) {
                    mc.player.setYaw(angle[0]);
                    mc.player.setPitch(80f);
                    InventoryUtility.switchTo(epSlot);
                    sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
                    InventoryUtility.switchTo(prevItem);
                }
                clipTimer = 20;
                afterPearlTime = afterPearl.getValue();
            }
        }
    }

    private int findEPSlot() {
        int epSlot = -1;
        if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
            epSlot = mc.player.getInventory().selectedSlot;
        }
        if (epSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.getInventory().getStack(l).getItem() == Items.ENDER_PEARL) {
                    epSlot = l;
                    break;
                }
            }
        }
        return epSlot;
    }

    public boolean canNoClip() {
        if (mode.getValue() == Mode.Vanilla) return true;
        if (!waitBreak.getValue()) return true;
        return clipTimer != 0;
    }

    public boolean playerInsideBlock() {
        return !mc.world.isAir(BlockPos.ofFloored(mc.player.getPos()));
    }

    @EventHandler
    public void onBreakBlock(EventBreakBlock e) {
        clipTimer = afterBreak.getValue();
    }
}

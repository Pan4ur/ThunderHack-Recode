package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.BlockAnimationUtility;

import static thunder.hack.utility.player.InteractionUtility.BlockPosWithFacing;
import static thunder.hack.utility.player.InteractionUtility.checkNearBlocks;

public class Scaffold extends Module {
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("PlaceMode", InteractionUtility.PlaceMode.Normal);
    public Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public Setting<Boolean> allowShift = new Setting<>("AllowShift", false);
    public Setting<Boolean> autoswap = new Setting<>("AutoSwap", true);
    public Setting<Boolean> tower = new Setting<>("Tower", true);
    public Setting<Boolean> safewalk = new Setting<>("SafeWalk", true);
    public Setting<Boolean> echestholding = new Setting<>("EchestHolding", false);
    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    public Setting<Boolean> render = new Setting<>("Render", true).withParent(renderCategory);
    private final Setting<BlockAnimationUtility.BlockRenderMode> renderMode = new Setting<>("RenderMode", BlockAnimationUtility.BlockRenderMode.All).withParent(renderCategory);
    private final Setting<BlockAnimationUtility.BlockAnimationMode> animationMode = new Setting<>("AnimationMode", BlockAnimationUtility.BlockAnimationMode.Fade).withParent(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("RenderFillColor", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("RenderLineColor", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("RenderLineWidth", 2, 1, 5).withParent(renderCategory);

    public Setting<Boolean> strict = new Setting<>("Strict", false);

    private final Timer timer = new Timer();
    private BlockPosWithFacing currentblock;
    float[] rotation = new float[2];

    public Scaffold() {
        super("Scaffold", Category.MOVEMENT);
    }

    private int findBlockToPlace() {
        if (mc.player.getMainHandStack().getItem() instanceof BlockItem) {
            if (((BlockItem) mc.player.getMainHandStack().getItem()).getBlock().getDefaultState().isSolid())
                return mc.player.getInventory().selectedSlot;
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getCount() != 0) {
                if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) {
                    if (!echestholding.getValue() || (echestholding.getValue() && !mc.player.getInventory().getStack(i).getItem().equals(Item.fromBlock(Blocks.ENDER_CHEST)))) {
                        if (((BlockItem) mc.player.getInventory().getStack(i).getItem()).getBlock().getDefaultState().isSolid())
                            return i;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        rotation = new float[]{mc.player.getYaw(), mc.player.getPitch()};
    }

    private BlockPosWithFacing checkNearBlocksExtended(BlockPos blockPos) {
        BlockPosWithFacing ret = null;
        //:skull:
        ret = checkNearBlocks(blockPos);
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(-1, 0, 0));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(1, 0, 0));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(0, 0, 1));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(0, 0, -1));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(-2, 0, 0));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(2, 0, 0));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(0, 0, 2));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(0, 0, -2));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos.add(0, -1, 0));
        BlockPos blockPos2 = blockPos.add(0, -1, 0);

        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos2.add(1, 0, 0));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos2.add(-1, 0, 0));
        if (ret != null) return ret;

        ret = checkNearBlocks(blockPos2.add(0, 0, 1));
        if (ret != null) return ret;

        return checkNearBlocks(blockPos2.add(0, 0, -1));
    }

    private int countValidBlocks() {
        int n = 36;
        int n2 = 0;

        while (n < 45) {
            if (!mc.player.getInventory().getStack(n >= 36 ? n - 36 : n).isEmpty()) {
                ItemStack itemStack = mc.player.getInventory().getStack(n >= 36 ? n - 36 : n);
                if (itemStack.getItem() instanceof BlockItem) {
                    if (((BlockItem) itemStack.getItem()).getBlock().getDefaultState().isSolid())
                        n2 += itemStack.getCount();
                }
            }
            n++;
        }

        return n2;
    }

    private void doSafeWalk(EventMove event) {
        double x = event.getX();
        double y = event.getY();
        double z = event.getZ();

        if (mc.player.isOnGround() && !mc.player.noClip) {
            double increment;
            for (increment = 0.05D; x != 0.0D && isOffsetBBEmpty(x, 0.0D); ) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
            }
            while (z != 0.0D && isOffsetBBEmpty(0.0D, z)) {
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
            while (x != 0.0D && z != 0.0D && isOffsetBBEmpty(x, z)) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
        }
        event.setX(x);
        event.setY(y);
        event.setZ(z);
        event.cancel();
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (fullNullCheck()) return;
        if (safewalk.getValue())
            doSafeWalk(event);
    }

    private boolean isOffsetBBEmpty(double x, double z) {
        return !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.1, 0, -0.1).offset(x, -2, z)).iterator().hasNext();
    }

    @EventHandler
    public void onPre(EventSync event) {
        if (strict.getValue()) {
            mc.player.setSprinting(false);
            mc.player.setYaw(rotation[0]);
            mc.player.setPitch(rotation[1]);
        }
        if (countValidBlocks() <= 0) {
            currentblock = null;
            return;
        }
        currentblock = null;

        if (mc.player.isSneaking() && !allowShift.getValue()) return;


        int n2 = findBlockToPlace();
        if (n2 == -1) return;

        Item item = mc.player.getInventory().getStack(n2).getItem();
        if (!(item instanceof BlockItem)) return;
        boolean fullBlock = false;
        BlockPos blockPos2 = new BlockPos((int) Math.floor(mc.player.getX()), (int) (Math.floor(mc.player.getY()) - (fullBlock ? 1.0 : 0.01)), (int) Math.floor(mc.player.getZ()));

        if (!mc.world.getBlockState(blockPos2).isReplaceable()) return;

        currentblock = checkNearBlocksExtended(blockPos2);
        if (currentblock != null) {
            if (rotate.getValue()) {

                Vec3d hitVec = new Vec3d(currentblock.position().getX() + 0.5, currentblock.position().getY() + 0.5, currentblock.position().getZ() + 0.5).add(new Vec3d(currentblock.facing().getUnitVector()).multiply(0.5));
                float[] rotations = InteractionUtility.calculateAngle(hitVec);

                if (strict.getValue()) {
                    rotation = rotations;
                } else {
                    mc.player.setYaw(rotations[0]);
                    mc.player.setPitch(rotations[1]);
                }
            }
        }
    }

    @EventHandler
    public void onPost(EventPostSync e) {
        if (mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.2, 0, -0.2).offset(0, -0.5, 0)).iterator().hasNext())
            return;
        if (currentblock == null) return;
        int prev_item = mc.player.getInventory().selectedSlot;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) {
            if (autoswap.getValue()) {
                int blockSlot = findBlockToPlace();
                if (blockSlot != -1) {
                    mc.player.getInventory().selectedSlot = blockSlot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(blockSlot));
                }
            }
        }
        if ((mc.player.getMainHandStack().getItem() instanceof BlockItem) && ((BlockItem) mc.player.getMainHandStack().getItem()).getBlock().getDefaultState().isSolid()) {
            if (!mc.player.input.jumping || mc.player.input.movementForward != 0.0f || mc.player.input.movementSideways != 0.0f || !tower.getValue()) {
                timer.reset();
            } else {
                mc.player.setVelocity(0.0, 0.42, 0.0);
                if (timer.passedMs(1500)) {
                    mc.player.setVelocity(mc.player.getVelocity().x, -0.28, mc.player.getVelocity().z);
                    timer.reset();
                }
            }

            BlockHitResult bhr;

            if (strict.getValue()) {
                bhr = new BlockHitResult(new Vec3d(currentblock.position().getX() + 0.5, currentblock.position().getY() + 0.5, currentblock.position().getZ() + 0.5).add(new Vec3d(currentblock.facing().getUnitVector()).multiply(0.5)), currentblock.facing(), currentblock.position(), false);
            } else {
                bhr = new BlockHitResult(new Vec3d((double) currentblock.position().getX() + Math.random(), currentblock.position().getY() + 0.99f, (double) currentblock.position().getZ() + Math.random()), currentblock.facing(), currentblock.position(), false);
            }

            if(placeMode.getValue() == InteractionUtility.PlaceMode.Packet)
                sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, PlayerUtility.getWorldActionId(mc.world)));
            else
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);

            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            if (render.getValue())
                BlockAnimationUtility.renderBlock(currentblock.position(), renderLineColor.getValue().getColorObject(), renderLineWidth.getValue(), renderFillColor.getValue().getColorObject(), animationMode.getValue(), renderMode.getValue());

            if (!strict.getValue()) {
                mc.player.getInventory().selectedSlot = prev_item;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        }
    }
}

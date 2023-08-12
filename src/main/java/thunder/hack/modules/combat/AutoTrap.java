package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import thunder.hack.utility.player.PlaceUtility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AutoTrap extends Module {
    public AutoTrap() {
        super("AutoTrap", Category.COMBAT);
    }

    private Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private  Setting<Boolean> strictDirection = new Setting<>("StrictDirection", true);
    private  Setting<Integer> actionShift = new Setting<>("ActionShift", 3, 1, 8);
    private  Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 0, 10);
    private  Setting<Float> placeRange = new Setting<>("TargetRange", 3.5F, 1F, 6F);
    private  Setting<Boolean> top = new Setting<>("Top", true);
    private  Setting<Boolean> toggelable = new Setting<>("DisableWhenDone", false);

    public static Timer inactivityTimer = new Timer();

    private int tickCounter = 0;

    private ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    public void onRender3D(MatrixStack stack) {
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                Render3DEngine.drawBoxOutline(new Box(pos), HudEditor.getColor(0), 2);
            }
        });
    }

    @EventHandler
    public void onPlayerUpdateEvent(PlayerUpdateEvent event) {
        if (!mc.player.isOnGround()) return;

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        PlayerEntity nearestTarget = Thunderhack.combatManager.getTargets(placeRange.getValue()).stream()
                .filter(this::isValidBase)
                .min(Comparator.comparing(e -> mc.player.distanceTo(e)))
                .orElse( null);

        if (nearestTarget == null || tickCounter < actionInterval.getValue()) {
            return;
        }

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos nextPos = getNextPos(nearestTarget.getBlockPos());
            if (nextPos != null) {
                if (PlaceUtility.place( nextPos, rotate.getValue(), strictDirection.getValue(), Hand.MAIN_HAND,InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN),false)) {
                    blocksPlaced++;
                    PlaceUtility.ghostBlocks.put(nextPos, System.currentTimeMillis());
                    renderPoses.put(nextPos, System.currentTimeMillis());
                    tickCounter = 0;
                    inactivityTimer.reset();
                } else {
                    break;
                }
            } else {
                if (toggelable.getValue()) {
                    toggle();
                    return;
                }
                break;
            }
        }
    }

    private List<BlockPos> getBlocks(BlockPos center){
        List<BlockPos> tempPos = new ArrayList<>();
        tempPos.add(center);
        tempPos.add(center.north());
        tempPos.add(center.north().east());
        tempPos.add(center.west());
        tempPos.add(center.west().north());
        tempPos.add(center.south());
        tempPos.add(center.south().west());
        tempPos.add(center.east());
        tempPos.add(center.east().south());

        List<BlockPos> tempPos2 = new ArrayList<>();

        for (BlockPos bp : tempPos){
            if(!mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(bp)).isEmpty()){
                tempPos2.add(bp);
            }
        }
        return tempPos2;
    }

    private BlockPos getNextPos(BlockPos playerPos) {
        Direction[] HORIZONTALS = new Direction[]{Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH};
        for(BlockPos bp2 : getBlocks(playerPos)) {
            double furthestDistance = 0D;
            for (Direction enumFacing : HORIZONTALS) {
                BlockPos furthestBlock = null;
                if (PlaceUtility.canPlaceBlock(bp2.offset(enumFacing).down(), strictDirection.getValue())) {
                    BlockPos tempBlock = bp2.offset(enumFacing).down();
                    double tempDistance = PlaceUtility.getEyesPos(mc.player).distanceTo(new Vec3d(tempBlock.getX() + 0.5, tempBlock.getY() + 0.5, tempBlock.getZ() + 0.5));
                    if (tempDistance >= furthestDistance) {
                        furthestBlock = tempBlock;
                        furthestDistance = tempDistance;
                    }
                }
                if (furthestBlock != null) return furthestBlock;
            }

            for (Direction enumFacing : HORIZONTALS) {
                BlockPos furthestBlock = null;
                if (PlaceUtility.canPlaceBlock(bp2.offset(enumFacing), strictDirection.getValue())) {
                    BlockPos tempBlock = bp2.offset(enumFacing);
                    double tempDistance = PlaceUtility.getEyesPos(mc.player).distanceTo(new Vec3d(tempBlock.getX() + 0.5, tempBlock.getY() + 0.5, tempBlock.getZ() + 0.5));
                    if (tempDistance >= furthestDistance) {
                        furthestBlock = tempBlock;
                        furthestDistance = tempDistance;
                    }
                }
                if (furthestBlock != null) return furthestBlock;
            }

            for (Direction enumFacing : HORIZONTALS) {
                BlockPos furthestBlock = null;
                if (PlaceUtility.canPlaceBlock(bp2.up().offset(enumFacing), strictDirection.getValue())) {
                    BlockPos tempBlock = bp2.up().offset(enumFacing);;
                    double tempDistance = PlaceUtility.getEyesPos(mc.player).distanceTo(new Vec3d(tempBlock.getX() + 0.5, tempBlock.getY() + 0.5, tempBlock.getZ() + 0.5));
                    if (tempDistance >= furthestDistance) {
                        furthestBlock = tempBlock;
                        furthestDistance = tempDistance;
                    }
                }
                if (furthestBlock != null) return furthestBlock;
            }

            if (top.getValue()) {
                Block baseBlock = mc.world.getBlockState(bp2.up().up()).getBlock();
                if (baseBlock instanceof AirBlock || baseBlock instanceof FluidBlock) {
                    if (PlaceUtility.canPlaceBlock(bp2.up().up(), true)) {
                        return bp2.up().up();
                    } else {
                        BlockPos offsetPos = bp2.up().up().offset(Direction.fromHorizontal(MathHelper.floor((double) (mc.player.getYaw() * 4.0F / 360.0F) + 0.5D) & 3));
                        if (PlaceUtility.canPlaceBlock(offsetPos, strictDirection.getValue())) {
                            return offsetPos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidBase(PlayerEntity player) {
        Block baseBlock = mc.world.getBlockState(BlockPos.ofFloored(player.getPos()).down()).getBlock();
        return !(baseBlock instanceof AirBlock) && !(baseBlock instanceof FluidBlock);
    }

    @Override
    public void onEnable(){
        inactivityTimer.reset();
    }
}

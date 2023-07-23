package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import thunder.hack.utility.PlaceUtility;

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

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                Render3DEngine.drawBoxOutline(new Box(pos), HudEditor.getColor(0), 2);
            }
        });
    }

    @Subscribe
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
                if (PlaceUtility.place(Blocks.OBSIDIAN, nextPos, rotate.getValue(), strictDirection.getValue(),false) != null) {
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

        for(BlockPos bp2 : getBlocks(playerPos)) {
            Direction[] HORIZONTALS = new Direction[]{Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH};
            for (Direction enumFacing : HORIZONTALS) {
                BlockPos block = null;
                if (PlaceUtility.canPlaceBlock(bp2.offset(enumFacing).down(), true))
                    block = bp2.offset(enumFacing).down();
                if (block != null) return block;
            }

            for (Direction enumFacing : HORIZONTALS) {
                BlockPos block = null;
                if (PlaceUtility.canPlaceBlock(bp2.offset(enumFacing), false))
                    block = bp2.offset(enumFacing);
                if (block != null) return block;
            }

            for (Direction enumFacing : HORIZONTALS) {
                BlockPos block = null;
                if (PlaceUtility.canPlaceBlock(bp2.up().offset(enumFacing), false))
                    block = bp2.up().offset(enumFacing);
                if (block != null) return block;
            }

            if (top.getValue()) {
                if (mc.world.getBlockState(bp2.up().up()).isReplaceable()) {
                    BlockPos offsetPos = bp2.up().up().offset(Direction.fromHorizontal(MathHelper.floor((double) (mc.player.getYaw() * 4.0F / 360.0F) + 0.5D) & 3));
                    if (PlaceUtility.canPlaceBlock(offsetPos, false)) {
                        return offsetPos;
                    }
                    return bp2.up().up();
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

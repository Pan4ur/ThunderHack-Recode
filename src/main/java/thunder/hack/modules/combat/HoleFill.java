package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class HoleFill extends Module {
    public HoleFill() {
        super("HoleFill", Category.COMBAT);
    }

    private Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private  Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    private  Setting<Float> placeRange = new Setting<>("Range", 5f, 1f, 6f);
    private  Setting<Integer> actionShift = new Setting<>("ActionShift", 1, 1, 3);
    private  Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 0, 5);
    private  Setting<Boolean> jumpDisable = new Setting<>("JumpDisable", false);
    private  Setting<Boolean> onlyWebs = new Setting<>("OnlyWebs", false);
    private  Setting<Mode> mode = new Setting<>("Mode", Mode.Always);
    private  Setting<Float> rangeToTarget = new Setting<>("RangeToTarget", 2f, 1f, 5f,v-> mode.getValue() == Mode.Target);
    private  Setting<Boolean> autoDisable = new Setting<>("AutoDisable", false);

    public static Timer inactivityTimer = new Timer();

    private enum Mode {
        Always, Target
    }
    
    private Map<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    private int tickCounter = 0;

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
    public void onSync(EventSync event) {
        if (jumpDisable.getValue() && mc.player.prevY < mc.player.getY()) toggle();
        if (tickCounter < actionInterval.getValue()) tickCounter++;
        if (tickCounter < actionInterval.getValue()) return;
        int slot = getBlockSlot();
        if (slot == -1) return;

        List<BlockPos> holes = findHoles();

        PlayerEntity target = Thunderhack.combatManager.getTargets(placeRange.getValue()).stream().min(Comparator.comparing(e -> mc.player.squaredDistanceTo(e))).orElse(null);;

        if (mode.getValue() == Mode.Target && target == null) return;

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos pos;
            if(mode.getValue() == Mode.Target){
                pos = StreamSupport.stream(holes.spliterator(), false)
                        .filter(this::isHole)
                        .filter(p -> mc.player.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5)) <= placeRange.getValue())
                        .filter(p -> PlaceUtility.canPlaceBlock(p, strictDirection.getValue()))
                        .filter(p -> target.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5)) <= rangeToTarget.getValue())
                        .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5))))
                        .orElse(null);
            } else {
                pos = StreamSupport.stream(holes.spliterator(), false)
                        .filter(this::isHole)
                        .filter(p -> mc.player.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5)) <= placeRange.getValue())
                        .filter(p -> PlaceUtility.canPlaceBlock(p, strictDirection.getValue()))
                        .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5))))
                        .orElse(null);
            }

            if (pos != null) {
                if (PlaceUtility.place(pos, rotate.getValue(), strictDirection.getValue(), Hand.MAIN_HAND, slot,false) != null) {
                    blocksPlaced++;
                    renderPoses.put(pos, System.currentTimeMillis());
                    PlaceUtility.ghostBlocks.put(pos, System.currentTimeMillis());
                    tickCounter = 0;
                    if (!mc.player.isOnGround()) return;
                    inactivityTimer.reset();
                } else {
                    break;
                }
            } else {
                if (autoDisable.getValue()) {
                    toggle();
                }
                break;
            }
        }
    }

    private List<BlockPos> findHoles() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();
        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (isHole(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    private int getBlockSlot() {
        ItemStack stack = mc.player.getMainHandStack();

        if (!stack.isEmpty() && isValidItem(stack.getItem())) {
            return mc.player.getInventory().selectedSlot;
        } else {
            for (int i = 0; i < 9; ++i) {
                stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && isValidItem(stack.getItem())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isValidItem(Item item) {
        if (item instanceof BlockItem) {
            if (onlyWebs.getValue()) {
                return ((BlockItem) item).getBlock() == Blocks.COBWEB;
            }
            return true;
        }
        return false;
    }

    public boolean validObi(BlockPos pos) {
        return !validBedrock(pos)
                && (mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK)
                && mc.world.getBlockState(pos).getBlock() == Blocks.AIR
                && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR
                && mc.world.getBlockState(pos.add(0, 2, 0)).getBlock() == Blocks.AIR;
    }

    public boolean validBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos).getBlock() == Blocks.AIR
                && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR
                && mc.world.getBlockState(pos.add(0, 2, 0)).getBlock() == Blocks.AIR;
    }

    public boolean isHole(BlockPos pos) {
        return validObi(pos) || validBedrock(pos);
    }
}

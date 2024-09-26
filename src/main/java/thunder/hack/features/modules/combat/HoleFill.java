package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventTick;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.math.PredictUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import thunder.hack.utility.render.BlockAnimationUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.*;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class HoleFill extends Module {
    private final Setting<InteractionUtility.Rotate> rotate = new Setting<>("Rotate", InteractionUtility.Rotate.None);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<Float> placeRange = new Setting<>("Range", 5f, 1f, 6f);
    private final Setting<Float> placeWallRange = new Setting<>("WallRange", 5f, 1f, 6f);
    private final Setting<Integer> actionShift = new Setting<>("BLock Per Tick", 1, 1, 4);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<Boolean> jumpDisable = new Setting<>("Jump Disable", false);
    private final Setting<FillBlocks> blocks = new Setting<>("Blocks", FillBlocks.All);

    private final Setting<SettingGroup> fill = new Setting<>("Fill Holes", new SettingGroup(true, 0));
    private final Setting<Boolean> selfFill = new Setting<>("Self Fill", false).addToGroup(fill);
    private final Setting<SelfFillMode> selfFillMode = new Setting<>("Self Fill Mode", SelfFillMode.Burrow).addToGroup(fill);
    private final Setting<Boolean> fillSingle = new Setting<>("Single", true).addToGroup(fill);
    private final Setting<Boolean> fillDouble = new Setting<>("Double", false).addToGroup(fill);
    private final Setting<Boolean> fillQuad = new Setting<>("Quad", false).addToGroup(fill);

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Always);
    private final Setting<Float> rangeToTarget = new Setting<>("Range To Target", 2f, 1f, 5f, v -> mode.getValue() == Mode.Target);
    private final Setting<Boolean> autoDisable = new Setting<>("Auto Disable", false);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Packet);

    private final Setting<SettingGroup> renderCategory = new Setting<>("Render", new SettingGroup(false, 0));
    private final Setting<BlockAnimationUtility.BlockRenderMode> renderMode = new Setting<>("Render Mode", BlockAnimationUtility.BlockRenderMode.All).addToGroup(renderCategory);
    private final Setting<BlockAnimationUtility.BlockAnimationMode> animationMode = new Setting<>("Animation Mode", BlockAnimationUtility.BlockAnimationMode.Fade).addToGroup(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("Render Fill Color", new ColorSetting(HudEditor.getColor(0))).addToGroup(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("Render Line Color", new ColorSetting(HudEditor.getColor(0))).addToGroup(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("Render Line Width", 2, 1, 5).addToGroup(renderCategory);

    private enum Mode {
        Always,
        Target
    }

    private enum FillBlocks {
        All,
        Webs,
        Obsidian,
        Indestructible
    }

    private enum SelfFillMode {
        Burrow,
        Trap
    }

    private static final Vec3i[] HOLE_VECTORS = {
            new Vec3i(-1, 0, -1),
            new Vec3i(1, 0, -1),
            new Vec3i(-1, 0, 1),
            new Vec3i(1, 0, 1),
    };

    private boolean burrowWasEnabled = false;
    public static final Timer inactivityTimer = new Timer();
    private int tickCounter = 0;
    private boolean selfFillNeed = false;

    public HoleFill() {
        super("HoleFill", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        burrowWasEnabled = false;
        selfFillNeed = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (fullNullCheck()) return;
        if (jumpDisable.getValue() && mc.player.prevY < mc.player.getY())
            disable(isRu() ? "Вы прыгнули! Выключаю..." : "You jumped! Disabling...");

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
            return;
        }
        if (HoleUtility.isHole(mc.player.getBlockPos()) && mc.world.getBlockState(mc.player.getBlockPos()).isAir()) {
            burrowWasEnabled = false;
        }
        int slot = getBlockSlot();
        if (slot == -1) return;

        List<BlockPos> holes = findHoles();

        PlayerEntity target = Managers.COMBAT.getTargets(placeRange.getValue()).stream()
                .min(Comparator.comparing(e -> mc.player.squaredDistanceTo(e)))
                .orElse(null);

        if (mode.getValue() == Mode.Target && target == null)
            return;

        final PlayerEntity predicted = PredictUtility.predictPlayer(target, 3);

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos pos;

            if (mode.getValue() == Mode.Target) {
                pos = holes.stream()
                        .filter(this::isHole)
                        .filter(p -> mc.player.getPos().distanceTo(p.toCenterPos()) <= placeRange.getValue())
                        .filter(p -> predicted.getPos().distanceTo(p.toCenterPos()) <= rangeToTarget.getValue())
                        .filter(p -> {
                            if (p.equals(mc.player.getBlockPos()) && selfFill.getValue()) {
                                selfFillNeed = true;
                                return true;
                            }
                            return InteractionUtility.canPlaceBlock(p, interactMode.getValue(), false);
                        })
                        .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(p.toCenterPos())))
                        .orElse(null);
            } else {
                pos = holes.stream()
                        .filter(this::isHole)
                        .filter(p -> mc.player.getPos().distanceTo(p.toCenterPos()) <= placeRange.getValue())
                        .filter(p -> {
                            if (p.equals(mc.player.getBlockPos()) && selfFill.getValue()) {
                                selfFillNeed = true;
                                return true;
                            }
                            return InteractionUtility.canPlaceBlock(p, interactMode.getValue(), false);
                        })
                        .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(p.toCenterPos())))
                        .orElse(null);
            }

            if (pos != null) {
                List<BlockPos> poses = getHolePoses(pos).stream()
                        .filter(blockPos -> mc.player.getPos().distanceTo(blockPos.toCenterPos()) <= placeRange.getValue())
                        .toList();
                boolean broke = false;

                if (selfFillNeed && HoleUtility.isHole(mc.player.getBlockPos())) {
                    switch (selfFillMode.getValue()) {
                        case Burrow -> {
                            if (ModuleManager.burrow.isEnabled() || burrowWasEnabled) {
                                return;
                            }

                            ModuleManager.burrow.enable();
                            selfFillNeed = false;
                            return;
                        }
                        case Trap -> {
                            BlockPos headPos = BlockPos.ofFloored(mc.player.getPos()).up(2);
                            if (mc.world.getBlockState(headPos).isReplaceable() && InteractionUtility.canPlaceBlock(headPos, interactMode.getValue(), false)) {
                                selfFillNeed = false;
                                InteractionUtility.placeBlock(headPos, rotate.getValue(), interactMode.getValue(), placeMode.getValue(), slot, true, false);
                                BlockAnimationUtility.renderBlock(headPos, renderLineColor.getValue().getColorObject(), renderLineWidth.getValue(), renderFillColor.getValue().getColorObject(), animationMode.getValue(), renderMode.getValue());

                                tickCounter = 0;
                                inactivityTimer.reset();
                                return;
                            } else {
                                boolean placed = false;
                                for (int i = 0; i < 3; i++) {
                                    for (Vec3i vecAdd : HoleUtility.VECTOR_PATTERN) {
                                        BlockPos checkPos = headPos.add(vecAdd).down(i);
                                        if (!mc.world.getBlockState(checkPos).isReplaceable()) {
                                            continue;
                                        }
                                        if (InteractionUtility.canPlaceBlock(checkPos, interactMode.getValue(), false)) {
                                            InteractionUtility.placeBlock(checkPos, rotate.getValue(), interactMode.getValue(), placeMode.getValue(), slot, true, false);
                                            blocksPlaced++;
                                            tickCounter = 0;
                                            inactivityTimer.reset();
                                            placed = true;
                                            break;
                                        }
                                    }
                                    if (placed) break;
                                }
                                if (placed) continue;
                            }
                        }
                    }
                }

                for (BlockPos blockPos : poses) {
                    if (InteractionUtility.placeBlock(blockPos, rotate.getValue(), interactMode.getValue(), placeMode.getValue(), slot, true, false)) {
                        blocksPlaced++;
                        tickCounter = 0;
                        BlockAnimationUtility.renderBlock(blockPos, renderLineColor.getValue().getColorObject(), renderLineWidth.getValue(), renderFillColor.getValue().getColorObject(), animationMode.getValue(), renderMode.getValue());
                        if (!mc.player.isOnGround()) return;
                        inactivityTimer.reset();
                    } else {
                        broke = true;
                        break;
                    }
                }
                if (broke)
                    break;
            } else {
                if (autoDisable.getValue()) {
                    disable(isRu() ? "Все холки заполнены!" : "All holes are filled!");
                }
                break;
            }
        }
    }

    private @NotNull @Unmodifiable List<BlockPos> getHolePoses(BlockPos fromPos) {
        if (HoleUtility.validQuadBedrock(fromPos) || HoleUtility.validQuadIndestructible(fromPos)) {
            for (Vec3i vec : HOLE_VECTORS) {
                if (mc.world.getBlockState(fromPos.add(vec)).isReplaceable()
                        && mc.world.getBlockState(fromPos.add(vec.getX(), 0, 0)).isReplaceable()
                        && mc.world.getBlockState(fromPos.add(0, 0, vec.getZ())).isReplaceable()) {
                    return List.of(
                            fromPos,
                            fromPos.add(vec),
                            fromPos.add(vec.getX(), 0, 0),
                            fromPos.add(0, 0, vec.getZ())
                    );
                }
            }
        }

        if (HoleUtility.validTwoBlockBedrock(fromPos) || HoleUtility.validTwoBlockIndestructible(fromPos)) {
            for (Vec3i vec : HoleUtility.VECTOR_PATTERN) {
                if (mc.world.getBlockState(fromPos).isReplaceable()
                        && mc.world.getBlockState(fromPos.add(vec)).isReplaceable()) {
                    return List.of(
                            fromPos,
                            fromPos.add(vec)
                    );
                }
            }
        }

        return List.of(fromPos);
    }

    private @NotNull List<BlockPos> findHoles() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(placeRange.getValue()) + 1;
        int h = placeRange.getValue().intValue();

        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    boolean foundEntity = false;
                    if (isHole(pos) && !isFillingNow(pos)) {

                        for (PlayerEntity pe : Managers.ASYNC.getAsyncPlayers()) {
                            if (new Box(pos).intersects(pe.getBoundingBox())) {
                                foundEntity = true;
                                break;
                            }
                        }

                        if (foundEntity)
                            continue;

                        BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), pos.toCenterPos().offset(Direction.UP, 0.5f), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                        if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != pos)
                            if (InteractionUtility.squaredDistanceFromEyes(pos.toCenterPos()) > placeWallRange.getPow2Value())
                                continue;
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

    private boolean isFillingNow(BlockPos pos) {
        return BlockAnimationUtility.isRendering(pos);
    }

    private boolean isValidItem(Item item) {
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            boolean isCorrectBlock;

            switch (blocks.getValue()) {
                case Webs -> isCorrectBlock = block == Blocks.COBWEB;
                case Obsidian -> isCorrectBlock = block == Blocks.OBSIDIAN;
                case Indestructible -> isCorrectBlock = block == Blocks.OBSIDIAN
                        || block == Blocks.CRYING_OBSIDIAN
                        || block == Blocks.NETHERITE_BLOCK
                        || block == Blocks.RESPAWN_ANCHOR;
                default -> isCorrectBlock = true;
            }

            return isCorrectBlock;
        }

        return false;
    }

    private boolean isHole(BlockPos pos) {
        return ((HoleUtility.validTwoBlockIndestructible(pos) || HoleUtility.validTwoBlockBedrock(pos)) && fillDouble.getValue())
                || ((HoleUtility.validQuadBedrock(pos) || HoleUtility.validQuadIndestructible(pos)) && fillQuad.getValue())
                || ((HoleUtility.validBedrock(pos) || HoleUtility.validIndestructible(pos)) && fillSingle.getValue());
    }
}
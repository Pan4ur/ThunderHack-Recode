package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.utility.world.HoleUtility;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static thunder.hack.modules.client.MainSettings.isRu;

public class HoleFill extends Module {
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<Float> placeRange = new Setting<>("Range", 5f, 1f, 6f);
    private final Setting<Integer> actionShift = new Setting<>("BLock Per Tick", 1, 1, 4);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<Boolean> jumpDisable = new Setting<>("Jump Disable", false);
    private final Setting<FillBlocks> blocks = new Setting<>("Blocks", FillBlocks.All);

    private final Setting<Parent> fill = new Setting<>("Fill Holes", new Parent(true, 0));
    private final Setting<Boolean> fillSingle = new Setting<>("Single", true).withParent(fill);
    private final Setting<Boolean> fillDouble = new Setting<>("Double", false).withParent(fill);
    private final Setting<Boolean> fillQuad = new Setting<>("Quad", false).withParent(fill);

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Always);
    private final Setting<Float> rangeToTarget = new Setting<>("Range To Target", 2f, 1f, 5f, v -> mode.getValue() == Mode.Target);
    private final Setting<Boolean> autoDisable = new Setting<>("Auto Disable", false);
    private final Setting<InventoryUtility.SwitchMode> switchMode = new Setting<>("Switch Mode", InventoryUtility.SwitchMode.Packet);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.All);

    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Fade).withParent(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("Render Fill Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("Render Line Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("Render Line Width", 2, 1, 5).withParent(renderCategory);

    private enum Mode {
        Always,
        Target
    }

    private enum FillBlocks {
        All,
        Webs,
        Obsidian,
        Indestrictible
    }

    private enum RenderMode {
        Fade,
        Decrease
    }

    private final Map<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();
    public static final Timer inactivityTimer = new Timer();
    private int tickCounter = 0;

    public HoleFill() {
        super("HoleFill", Category.COMBAT);
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                switch (renderMode.getValue()) {
                    case Fade -> {
                        Box box = new Box(pos);
                        Render3DEngine.drawFilledBox(stack, box, Render2DEngine.injectAlpha(renderFillColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))));
                        Render3DEngine.drawBoxOutline(box, Render2DEngine.injectAlpha(renderLineColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))), renderLineWidth.getValue());
                    }
                    case Decrease -> {
                        float scale = 1 - (float) (System.currentTimeMillis() - time) / 500;
                        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

                        Render3DEngine.drawFilledBox(stack, box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), Render2DEngine.injectAlpha(renderFillColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))));
                        Render3DEngine.drawBoxOutline(box.shrink(scale, scale, scale).offset(0.5 + scale * 0.5, 0.5 + scale * 0.5, 0.5 + scale * 0.5), renderLineColor.getValue().getColorObject(), renderLineWidth.getValue());
                    }
                }
            }
        });
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (fullNullCheck()) return;
        if (jumpDisable.getValue() && mc.player.prevY < mc.player.getY())
            disable(isRu() ? "Вы прыгнули! Выключаю..." : "You jumped! Disabling...");

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
            return;
        }
        int slot = getBlockSlot();
        if (slot == -1) return;

        List<BlockPos> holes = findHoles();

        PlayerEntity target = Thunderhack.combatManager.getTargets(placeRange.getValue()).stream()
                .min(Comparator.comparing(e -> mc.player.squaredDistanceTo(e)))
                .orElse(null);

        if (mode.getValue() == Mode.Target && target == null) return;

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos pos;
            if (mode.getValue() == Mode.Target) {
                pos = holes.stream()
                        .filter(this::isHole)
                        .filter(p -> mc.player.getPos().distanceTo(p.toCenterPos()) <= placeRange.getValue())
                        .filter(p -> InteractionUtility.canPlaceBlock(p, interactMode.getValue()))
                        .filter(p -> target.getPos().distanceTo(p.toCenterPos()) <= rangeToTarget.getValue())
                        .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(p.toCenterPos())))
                        .orElse(null);
            } else {
                pos = holes.stream()
                        .filter(this::isHole)
                        .filter(p -> mc.player.getPos().distanceTo(p.toCenterPos()) <= placeRange.getValue())
                        .filter(p -> {
                            InteractionUtility.checkEntities = true;
                            boolean canPlace = InteractionUtility.canPlaceBlock(p, interactMode.getValue());
                            InteractionUtility.checkEntities = false;
                            return canPlace;
                        })
                        .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(p.toCenterPos())))
                        .orElse(null);
            }

            if (pos != null) {
                List<BlockPos> poses = HoleUtility.getHolePoses(pos).stream()
                        .filter(blockPos -> mc.player.getPos().distanceTo(blockPos.toCenterPos()) <= placeRange.getValue())
                        .toList();
                boolean broke = false;

                for (BlockPos blockPos : poses) {
                    int preSlot = mc.player.getInventory().selectedSlot;
                    InventoryUtility.switchTo(slot, switchMode.getValue());
                    if (InteractionUtility.placeBlock(blockPos, rotate.getValue(), interactMode.getValue(), placeMode.getValue())) {
                        blocksPlaced++;
                        // InteractionUtility.ghostBlocks.put(blockPos, System.currentTimeMillis());
                        tickCounter = 0;
                        renderPoses.put(blockPos, System.currentTimeMillis());
                        if (!mc.player.isOnGround()) return;
                        inactivityTimer.reset();
                    } else {
                        broke = true;
                        break;
                    }
                    InventoryUtility.switchTo(preSlot, switchMode.getValue());
                }


                if (broke) break;
            } else {
                if (autoDisable.getValue()) {
                    disable(isRu() ? "Все холки заполнены!" : "All holes are filled!");
                }
                break;
            }
        }
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

                    if (isHole(pos) && !isFillingNow(pos)) positions.add(pos);
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
        for (BlockPos checkPos : renderPoses.keySet()) {
            if (checkPos.equals(pos)) return true;
        }

        return false;
    }

    private boolean isValidItem(Item item) {
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            boolean isCorrectBlock;

            switch (blocks.getValue()) {
                case Webs -> isCorrectBlock = block == Blocks.COBWEB;
                case Obsidian -> isCorrectBlock = block == Blocks.OBSIDIAN;
                case Indestrictible -> isCorrectBlock = block == Blocks.OBSIDIAN
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
        return ((HoleUtility.validTwoBlockIndestructibleXZ(pos) || HoleUtility.validTwoBlockIndestructibleXZ(pos)
                || HoleUtility.validTwoBlockBedrockXZ1(pos) || HoleUtility.validTwoBlockBedrockXZ(pos)) && fillDouble.getValue())
                || ((HoleUtility.validQuadBedrock(pos) || HoleUtility.validQuadIndestructible(pos)) && fillQuad.getValue())
                || ((HoleUtility.validBedrock(pos) || HoleUtility.validIndestructible(pos)) && fillSingle.getValue());
    }
}

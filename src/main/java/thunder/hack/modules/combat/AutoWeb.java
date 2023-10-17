package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static thunder.hack.utility.player.InteractionUtility.squaredDistanceFromEyes;

public final class AutoWeb extends Module {
    private final Setting<Integer> range = new Setting<>("Range", 5, 1, 7);
    private final Setting<Integer> placeWallRange = new Setting<>("WallRange", 5, 1, 7);
    private final Setting<PlaceTiming> placeTiming = new Setting<>("PlaceTiming", PlaceTiming.Default);
    private final Setting<Integer> blocksPerTick = new Setting<>("Block/Tick", 8, 1, 12, v -> placeTiming.getValue() == PlaceTiming.Default);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10, v -> placeTiming.getValue() != PlaceTiming.Sequential);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("PlaceMode", InteractionUtility.PlaceMode.Normal);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Fade).withParent(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("Render Fill Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("Render Line Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("Render Line Width", 2, 1, 5).withParent(renderCategory);

    private final ArrayList<BlockPos> sequentialBlocks = new ArrayList<>();
    public static Timer inactivityTimer = new Timer();

    private final Map<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();
    private static AutoWeb instance;

    private int delay = 0;

    public AutoWeb() {
        super("AutoWeb", Category.COMBAT);
        instance = this;
    }

    public void onRender3D(MatrixStack stack) {
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                switch (renderMode.getValue()) {
                    case Fade -> {
                        Render3DEngine.drawFilledBox(stack, new Box(pos), Render2DEngine.injectAlpha(renderFillColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))));
                        Render3DEngine.drawBoxOutline(new Box(pos), Render2DEngine.injectAlpha(renderLineColor.getValue().getColorObject(), (int) (100f * (1f - ((System.currentTimeMillis() - time) / 500f)))), renderLineWidth.getValue());
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


    @Override
    public void onEnable() {
        sequentialBlocks.clear();
        renderPoses.clear();
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        BlockPos targetBlock1 = getSequentialPos();
        if (targetBlock1 == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        InventoryUtility.saveSlot();
        if (placeTiming.getValue() == PlaceTiming.Default) {
            int placed = 0;
            while (placed < blocksPerTick.getValue()) {
                BlockPos targetBlock = getSequentialPos();
                if (targetBlock == null) break;
                if (InteractionUtility.placeBlock(targetBlock, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false, false)) {
                    placed++;
                    renderPoses.put(targetBlock, System.currentTimeMillis());
                    delay = placeDelay.getValue();
                    inactivityTimer.reset();
                }
            }
        } else if (placeTiming.getValue() == PlaceTiming.Vanilla || placeTiming.getValue() == PlaceTiming.Sequential) {
            BlockPos targetBlock = getSequentialPos();
            if (targetBlock == null) return;

            if (InteractionUtility.placeBlock(targetBlock, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false, false)) {
                sequentialBlocks.add(targetBlock);
                renderPoses.put(targetBlock, System.currentTimeMillis());
                delay = placeDelay.getValue();
                inactivityTimer.reset();
            }
        }
        InventoryUtility.returnSlot();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof BlockUpdateS2CPacket pac) {
            if (placeTiming.getValue() == PlaceTiming.Sequential && !sequentialBlocks.isEmpty()) {
                if (sequentialBlocks.contains(pac.getPos())) {
                    BlockPos bp = getSequentialPos();
                    if (bp != null) {
                        InventoryUtility.saveSlot();
                        if (InteractionUtility.placeBlock(bp, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false, false)) {
                            sequentialBlocks.add(bp);
                            sequentialBlocks.remove(pac.getPos());
                            InventoryUtility.returnSlot();
                            inactivityTimer.reset();
                            return;
                        }
                        InventoryUtility.returnSlot();
                    }
                }
            }
        }
    }

    private BlockPos getSequentialPos() {
        PlayerEntity target = ThunderHack.combatManager.getNearestTarget(range.getValue());
        if (target != null) {

            BlockPos targetBp = BlockPos.ofFloored(target.getPos());

            ArrayList<BlockPos> positions = new ArrayList<>();
            positions.add(targetBp);
            positions.add(targetBp.up());
            positions.add(targetBp.east());
            positions.add(targetBp.west());
            positions.add(targetBp.south());
            positions.add(targetBp.north());

            for (BlockPos bp : positions) {
                BlockHitResult wallCheck = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), bp.toCenterPos().offset(Direction.UP, 0.5f), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                if (wallCheck != null && wallCheck.getType() == HitResult.Type.BLOCK && wallCheck.getBlockPos() != bp)
                    if (squaredDistanceFromEyes(bp.toCenterPos()) > placeWallRange.getPow2Value()) continue;
                if (InteractionUtility.canPlaceBlock(bp, interact.getValue(), false) && mc.world.isAir(bp)) {
                    return bp;
                }
            }
        }

        return null;
    }


    private int getSlot() {
        List<Block> canUseBlocks = new ArrayList<>();
        canUseBlocks.add(Blocks.COBWEB);
        int slot = -1;
        final ItemStack mainhandStack = mc.player.getMainHandStack();
        if (mainhandStack != ItemStack.EMPTY && mainhandStack.getItem() instanceof BlockItem) {
            final Block blockFromMainhandItem = ((BlockItem) mainhandStack.getItem()).getBlock();
            if (canUseBlocks.contains(blockFromMainhandItem)) {
                slot = mc.player.getInventory().selectedSlot;
            }
        }
        if (slot == -1) {
            for (int i = 0; i < 9; i++) {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                    final Block blockFromItem = ((BlockItem) stack.getItem()).getBlock();
                    if (canUseBlocks.contains(blockFromItem)) {
                        slot = i;
                        break;
                    }
                }
            }
        }
        return slot;
    }

    public static AutoWeb getInstance() {
        return instance;
    }

    private enum PlaceTiming {
        Default, Vanilla, Sequential
    }

    private enum RenderMode {
        Fade, Decrease
    }
}
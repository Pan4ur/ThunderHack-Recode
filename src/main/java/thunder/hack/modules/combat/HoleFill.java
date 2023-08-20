package thunder.hack.modules.combat;


import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.render.HoleESP;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.modules.render.HoleESP.*;

public class HoleFill extends Module {
    public HoleFill() {
        super("HoleFill", Category.COMBAT);
    }
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Area);
    private final Setting<PlaceTiming> placeTiming = new Setting<>("PlaceTiming", PlaceTiming.Default);
    private final Setting<Integer> blocksPerTick = new Setting<>("Block/Tick", 8, 1, 12, v -> placeTiming.getValue() == PlaceTiming.Default);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10, v -> placeTiming.getValue() != PlaceTiming.Sequential);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("PlaceMode", InteractionUtility.PlaceMode.Normal);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<Parent> blocks = new Setting<>("Blocks", new Parent(false, 0));
    private final Setting<Boolean> obsidian = new Setting<>("Obsidian", true).withParent(blocks);
    private final Setting<Boolean> anchor = new Setting<>("Anchor", false).withParent(blocks);
    private final Setting<Boolean> enderChest = new Setting<>("EnderChest", true).withParent(blocks);
    private final Setting<Boolean> netherite = new Setting<>("Netherite", false).withParent(blocks);
    private final Setting<Boolean> cryingObsidian = new Setting<>("CryingObsidian", true).withParent(blocks);
    private final Setting<Boolean> dirt = new Setting<>("Dirt", false).withParent(blocks);
    private final Setting<Boolean> web = new Setting<>("Web", false).withParent(blocks);
    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Fade).withParent(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("Render Fill Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("Render Line Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("Render Line Width", 2, 1, 5).withParent(renderCategory);
    private final Setting<Float> rangeXZ = new Setting<>("Range", 5f, 2f, 7f);
    private final Setting<Float> rangeToTarget = new Setting<>("RangeToTarget", 3f, 1f, 5f, v-> mode.getValue() == Mode.UnderTarget);

    private final ArrayList<BlockPos> sequentialBlocks = new ArrayList<>();
    public static Timer inactivityTimer = new Timer();
    private final List<BlockPos> positions = new CopyOnWriteArrayList<>();

    private enum PlaceTiming {
        Default, Vanilla, Sequential
    }

    private enum Mode {
        Area,
        UnderTarget
    }

    private enum RenderMode {
        Fade,
        Decrease
    }

    private final Map<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    private int delay = 0;


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
                if (targetBlock == null)
                    break;
                if (InteractionUtility.placeBlock(targetBlock, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false)) {
                    placed++;
                    renderPoses.put(targetBlock, System.currentTimeMillis());
                    delay = placeDelay.getValue();
                }
            }
        } else if (placeTiming.getValue() == PlaceTiming.Vanilla || placeTiming.getValue() == PlaceTiming.Sequential) {
            BlockPos targetBlock = getSequentialPos();
            if (targetBlock == null)
                return;

            if (InteractionUtility.placeBlock(targetBlock, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false)) {
                sequentialBlocks.add(targetBlock);
                renderPoses.put(targetBlock, System.currentTimeMillis());
                delay = placeDelay.getValue();
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
                        if (InteractionUtility.placeBlock(bp, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false)) {
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
        if(positions.isEmpty()) return null;
        if(mode.getValue() == Mode.Area) {
            for (BlockPos bp : positions) {
                if (InteractionUtility.canPlaceBlock(bp, interact.getValue()) && mc.world.isAir(bp)) {
                    return bp;
                }
            }
        } else {
            PlayerEntity target = Thunderhack.combatManager.getNearestTarget(7);
            if(target != null){
                for (BlockPos bp : positions) {
                    if(target.getBlockPos().getSquaredDistance(bp.toCenterPos()) > rangeToTarget.getPow2Value()) continue;
                    if (InteractionUtility.canPlaceBlock(bp, interact.getValue()) && mc.world.isAir(bp)) {
                        return bp;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onThread(){
        findHoles();
    }

    private void findHoles() {
        ArrayList<BlockPos> bloks = new ArrayList<>();
        BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());
        for (int i = (int) Math.floor(playerPos.getX() - rangeXZ.getValue()); i <= Math.ceil(playerPos.getX() + rangeXZ.getValue()); i++) {
            for (int j = (int) Math.floor(playerPos.getY() - rangeXZ.getValue()); j <= Math.ceil(playerPos.getY() + rangeXZ.getValue()); j++) {
                for (int k = (int) Math.floor(playerPos.getZ() - rangeXZ.getValue()); k <= Math.ceil(playerPos.getZ() + rangeXZ.getValue()); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (validIndestructible(pos)) {
                        bloks.add(pos);
                    } else if (validBedrock(pos)) {
                        bloks.add(pos);
                    } else if (validTwoBlockBedrockXZ(pos)) {
                        bloks.add(pos);
                    } else if (validTwoBlockIndestructibleXZ(pos)) {
                        bloks.add(pos);
                    } else if (validTwoBlockBedrockXZ1(pos)) {
                        bloks.add(pos);
                    } else if (validTwoBlockIndestructibleXZ1(pos)) {
                        bloks.add(pos);
                    } else if (validQuadBedrock(pos)) {
                        bloks.add(pos);
                    } else if (validQuadIndestructible(pos)) {
                        bloks.add(pos);
                    }
                }
            }
        }
        positions.clear();
        positions.addAll(bloks);
    }


    private int getSlot() {
        List<Block> canUseBlocks = new ArrayList<>();

        if (obsidian.getValue()) {
            canUseBlocks.add(Blocks.OBSIDIAN);
        }
        if (enderChest.getValue()) {
            canUseBlocks.add(Blocks.ENDER_CHEST);
        }
        if (cryingObsidian.getValue()) {
            canUseBlocks.add(Blocks.CRYING_OBSIDIAN);
        }
        if (netherite.getValue()) {
            canUseBlocks.add(Blocks.NETHERITE_BLOCK);
        }
        if (anchor.getValue()) {
            canUseBlocks.add(Blocks.RESPAWN_ANCHOR);
        }
        if (dirt.getValue()) {
            canUseBlocks.add(Blocks.DIRT);
        }
        if (web.getValue()) {
            canUseBlocks.add(Blocks.COBWEB);
        }

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
}

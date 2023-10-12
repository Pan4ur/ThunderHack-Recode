package dev.thunderhack.modules.combat;

import dev.thunderhack.event.events.EventPostSync;
import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.setting.settings.Parent;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.player.InteractionUtility;
import dev.thunderhack.utils.player.InventoryUtility;
import dev.thunderhack.utils.render.Render2DEngine;
import dev.thunderhack.utils.render.Render3DEngine;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import dev.thunderhack.modules.client.HudEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SelfTrap extends Module {

    public SelfTrap() {
        super("SelfTrap", Category.COMBAT);
    }

    private final Setting<PlaceTiming> placeTiming = new Setting<>("PlaceTiming", PlaceTiming.Default);
    private final Setting<Integer> blocksPerTick = new Setting<>("Block/Tick", 8, 1, 12, v -> placeTiming.getValue() == PlaceTiming.Default);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10, v -> placeTiming.getValue() != PlaceTiming.Sequential);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Vanilla);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("PlaceMode", InteractionUtility.PlaceMode.Normal);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<Parent> blocks = new Setting<>("Blocks", new Parent(false, 0));
    private final Setting<Boolean> obsidian = new Setting<>("Obsidian", true).withParent(blocks);
    private final Setting<Boolean> anchor = new Setting<>("Anchor", false).withParent(blocks);
    private final Setting<Boolean> enderChest = new Setting<>("EnderChest", true).withParent(blocks);
    private final Setting<Boolean> netherite = new Setting<>("Netherite", false).withParent(blocks);
    private final Setting<Boolean> cryingObsidian = new Setting<>("CryingObsidian", true).withParent(blocks);
    private final Setting<Boolean> dirt = new Setting<>("Dirt", false).withParent(blocks);
    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    private final Setting<RenderMode> renderMode = new Setting<>("RenderMode", RenderMode.Fade).withParent(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("Fill", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("Line", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("LineWidth", 2, 1, 5).withParent(renderCategory);

    private ArrayList<BlockPos> sequentialBlocks = new ArrayList<>();
    public static Timer inactivityTimer = new Timer();

    private int delay;

    private final Map<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

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

    @EventHandler
    public void onSync(EventSync e){
        if (placeTiming.getValue() == PlaceTiming.Vanilla && rotate.getValue()) {
            BlockPos targetBlock = getSequentialPos();
            if (targetBlock != null) {
                BlockHitResult result = InteractionUtility.getPlaceResult(targetBlock, interact.getValue(), false);
                if (result != null) {
                    float[] angle = InteractionUtility.calculateAngle(result.getPos());
                    mc.player.setYaw(angle[0]);
                    mc.player.setYaw(angle[1]);
                }
            }
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        List<BlockPos> blocks = getBlocks();
        if (blocks.isEmpty()) return;

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
                if (InteractionUtility.placeBlock(targetBlock, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false, false)) {
                    placed++;
                    delay = placeDelay.getValue();
                    inactivityTimer.reset();
                    renderPoses.put(targetBlock, System.currentTimeMillis());
                } else break;
            }
        } else if (placeTiming.getValue() == PlaceTiming.Vanilla || placeTiming.getValue() == PlaceTiming.Sequential) {
            BlockPos targetBlock = getSequentialPos();
            if (targetBlock != null) {
                if (InteractionUtility.placeBlock(targetBlock, rotate.getValue() && placeTiming.getValue() == PlaceTiming.Sequential, interact.getValue(), placeMode.getValue(), getSlot(), false, false)) {
                    sequentialBlocks.add(targetBlock);
                    delay = placeDelay.getValue();
                    inactivityTimer.reset();
                    renderPoses.put(targetBlock, System.currentTimeMillis());
                }
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
                            renderPoses.put(bp, System.currentTimeMillis());
                            return;
                        }
                        InventoryUtility.returnSlot();
                    }
                }
            }
        }
    }

    private BlockPos getSequentialPos() {
        List<BlockPos> list = getBlocks();
        if (list.isEmpty()) return null;
        for (BlockPos bp : getBlocks()) {
            if (InteractionUtility.canPlaceBlock(bp, interact.getValue(), false) && mc.world.isAir(bp)) {
                return bp;
            }
        }
        return null;
    }

    private List<BlockPos> getBlocks() {
        List<BlockPos> blocks = new ArrayList<>();
        for (BlockPos bp : getPlayerBlocks(mc.player)) {
            blocks.add(bp.east().up());
            blocks.add(bp.west().up());
            blocks.add(bp.south().up());
            blocks.add(bp.north().up());
            blocks.add(bp.down());
            blocks.add(bp.east());
            blocks.add(bp.west());
            blocks.add(bp.south());
            blocks.add(bp.north());
            blocks.add(bp.down());
            blocks.add(bp.east().down());
            blocks.add(bp.west().down());
            blocks.add(bp.south().down());
            blocks.add(bp.north().down());
            blocks.add(bp.up().up());

            if (!InteractionUtility.canPlaceBlock(bp.up().up(), interact.getValue(), false)) {
                Direction dir = mc.player.getHorizontalFacing();
                if (dir != null) {
                    blocks.add(bp.up().up().offset(dir, 1));
                }
            }
        }

        return blocks;
    }

    private List<BlockPos> getPlayerBlocks(PlayerEntity pl) {
        List<BlockPos> tempPos = new ArrayList<>();
        BlockPos center = getPlayerPos(pl);
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

        for (BlockPos bp : tempPos) {
            if (!mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(bp)).isEmpty()) {
                tempPos2.add(bp);
            }
        }
        return tempPos2;
    }

    private int getSlot() {
        List<Block> canUseBlocks = new ArrayList<>();

        if (obsidian.getValue()) canUseBlocks.add(Blocks.OBSIDIAN);
        if (enderChest.getValue()) canUseBlocks.add(Blocks.ENDER_CHEST);
        if (cryingObsidian.getValue()) canUseBlocks.add(Blocks.CRYING_OBSIDIAN);
        if (netherite.getValue()) canUseBlocks.add(Blocks.NETHERITE_BLOCK);
        if (anchor.getValue()) canUseBlocks.add(Blocks.RESPAWN_ANCHOR);
        if (dirt.getValue()) canUseBlocks.add(Blocks.DIRT);

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

    private BlockPos getPlayerPos(PlayerEntity pl) {
        return BlockPos.ofFloored(pl.getX(), pl.getY() - Math.floor(pl.getY()) > 0.8 ? Math.floor(pl.getY()) + 1.0 : Math.floor(pl.getY()), pl.getZ());
    }

    private enum PlaceTiming {
        Default, Vanilla, Sequential
    }

    private enum RenderMode {
        Fade, Decrease
    }
}

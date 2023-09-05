package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.events.impl.*;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.concurrent.ConcurrentHashMap;


public class Surround extends Module {

    public Surround() {
        super("Surround", "окружает тебя обсой", Category.COMBAT);
    }

    private final Setting<PlaceTiming> placeTiming = new Setting<>("PlaceTiming", PlaceTiming.Default);
    private final Setting<Integer> blocksPerTick = new Setting<>("Block/Tick", 8, 1, 12, v -> placeTiming.getValue() == PlaceTiming.Default);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10, v -> placeTiming.getValue() != PlaceTiming.Sequential);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("PlaceMode", InteractionUtility.PlaceMode.Normal);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<Boolean> support = new Setting<>("Support", true);
    private final Setting<Boolean> breakCrystal = new Setting<>("BreakCrystal", true);
    private final Setting<Boolean> breakCrystalTick = new Setting<>("BreakCrystalTick", true);
    private final Setting<Boolean> breakCrystalPacket = new Setting<>("BreakCrystalPacket", true);
    private final Setting<Boolean> center = new Setting<>("Center", true);
    private final Setting<Parent> blocks = new Setting<>("Blocks", new Parent(false, 0));
    private final Setting<Boolean> obsidian = new Setting<>("Obsidian", true).withParent(blocks);
    private final Setting<Boolean> anchor = new Setting<>("Anchor", false).withParent(blocks);
    private final Setting<Boolean> enderChest = new Setting<>("EnderChest", true).withParent(blocks);
    private final Setting<Boolean> netherite = new Setting<>("Netherite", false).withParent(blocks);
    private final Setting<Boolean> cryingObsidian = new Setting<>("CryingObsidian", true).withParent(blocks);
    private final Setting<Boolean> dirt = new Setting<>("Dirt", false).withParent(blocks);
    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    private final Setting<RenderMode> renderMode = new Setting<>("Render Mode", RenderMode.Fade).withParent(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("Render Fill Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("Render Line Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("Render Line Width", 2, 1, 5).withParent(renderCategory);

    private enum PlaceTiming {
        Default,
        Vanilla,
        Sequential
    }

    private enum RenderMode {
        Fade,
        Decrease
    }

    private final List<BlockPos> sequentialBlocks = new ArrayList<>();
    private final Map<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();
    private int delay;
    private BlockPos currentPlacePos = null;

    public static final Timer inactivityTimer = new Timer();

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
        currentPlacePos = null;
        if (center.getValue()) {
            mc.player.updatePosition(MathHelper.floor(mc.player.getX()) + 0.5, mc.player.getY(), MathHelper.floor(mc.player.getZ()) + 0.5);
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
        }
    }

    @EventHandler
    public void onPostSync(EventEntitySpawn e) {
        if (breakCrystal.getValue() && !breakCrystalTick.getValue()) {
            Entity entity = getEntity(currentPlacePos);

            if (entity != null) {
                if (breakCrystalPacket.getValue()) mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                else mc.interactionManager.attackEntity(mc.player, entity);
            }
        }
    }

    public static EndCrystalEntity getEntity(BlockPos blockPos) {
        if (blockPos == null) return null;
        return hasEntity(new Box(blockPos), entity -> entity instanceof EndCrystalEntity) ?
                (EndCrystalEntity) mc.world.getOtherEntities(null, new Box(blockPos), entity -> entity instanceof EndCrystalEntity).get(0) : null;
    }

    public static boolean hasEntity(Box box, Predicate<Entity> predicate) {
        try {
            return !mc.world.getOtherEntities(null, box, predicate).isEmpty();
        } catch (java.util.ConcurrentModificationException ex) {}

        return false;
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        List<BlockPos> blocks = getBlocks();
        if (blocks.isEmpty()) return;

        if (delay > 0) {
            delay--;
            return;
        }

        if(getSlot() == -1) disable(MainSettings.isRu() ? "Нет блоков!" : "No blocks!");

        InventoryUtility.saveSlot();
        if (placeTiming.getValue() == PlaceTiming.Default) {
            int placed = 0;
            while (placed < blocksPerTick.getValue()) {
                BlockPos targetBlock = getSequentialPos();
                if (targetBlock == null)
                    break;
                currentPlacePos = targetBlock;

                if (breakCrystal.getValue() && breakCrystalTick.getValue()) {
                    Entity entity = getEntity(targetBlock);

                    if (entity != null) {
                        if (breakCrystalPacket.getValue()) mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                        else mc.interactionManager.attackEntity(mc.player, entity);
                    }
                }

                if (InteractionUtility.placeBlock(targetBlock, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false, true)) {
                    placed++;
                    delay = placeDelay.getValue();
                    inactivityTimer.reset();
                    renderPoses.put(targetBlock, System.currentTimeMillis());
                } else break;
            }
        } else if (placeTiming.getValue() == PlaceTiming.Vanilla || placeTiming.getValue() == PlaceTiming.Sequential) {
            BlockPos targetBlock = getSequentialPos();
            if (targetBlock == null)
                return;
            currentPlacePos = targetBlock;

            if (breakCrystal.getValue() && breakCrystalTick.getValue()) {
                Entity entity = getEntity(targetBlock);

                if (entity != null) {
                    if (breakCrystalPacket.getValue()) mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                    else mc.interactionManager.attackEntity(mc.player, entity);
                }
            }

            if (InteractionUtility.placeBlock(targetBlock, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false, true)) {
                sequentialBlocks.add(targetBlock);
                delay = placeDelay.getValue();
                inactivityTimer.reset();
                renderPoses.put(targetBlock, System.currentTimeMillis());
            }
        }
        InventoryUtility.returnSlot();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if(getSlot() == -1) disable(MainSettings.isRu() ? "Нет блоков!" : "No blocks!");
        if (e.getPacket() instanceof BlockUpdateS2CPacket pac) {
            if (placeTiming.getValue() == PlaceTiming.Sequential && !sequentialBlocks.isEmpty()) {
                if (sequentialBlocks.contains(pac.getPos())) {
                    BlockPos bp = getSequentialPos();
                    if (bp != null) {
                        currentPlacePos = bp;
                        if (breakCrystal.getValue() && breakCrystalTick.getValue()) {
                            Entity entity = getEntity(bp);

                            if (entity != null) {
                                if (breakCrystalPacket.getValue()) mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                                else mc.interactionManager.attackEntity(mc.player, entity);
                            }
                        }

                        InventoryUtility.saveSlot();
                        if (InteractionUtility.placeBlock(bp, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false, true)) {
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
            if (mc.player.squaredDistanceTo(pac.getPos().toCenterPos()) < 9 && pac.getState() == Blocks.AIR.getDefaultState()) {
                BlockPos bp = getSequentialPos();

                if (bp != null) {
                    currentPlacePos = bp;
                    if (breakCrystal.getValue() && breakCrystalTick.getValue()) {
                        Entity entity = getEntity(bp);

                        if (entity != null) {
                            if (breakCrystalPacket.getValue()) mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                            else mc.interactionManager.attackEntity(mc.player, entity);
                        }
                    }

                    InventoryUtility.saveSlot();
                    if (InteractionUtility.placeBlock(bp, rotate.getValue(), interact.getValue(), placeMode.getValue(), getSlot(), false, true)) {
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

    private @Nullable BlockPos getSequentialPos() {
        for (BlockPos bp : getBlocks()) {
            if(new Box(bp).intersects(mc.player.getBoundingBox())) continue;
            if (InteractionUtility.canPlaceBlock(bp, interact.getValue(), true) && mc.world.isAir(bp)) {
                return bp;
            }
        }
        return null;
    }

    List<BlockPos> getBlocks() {
        BlockPos playerPos = this.getPlayerPos();
        ArrayList<BlockPos> offsets = new ArrayList<BlockPos>();
        if (!center.getValue()) {
            int z;
            int x;
            double decimalX = Math.abs(mc.player.getX()) - Math.floor(Math.abs(mc.player.getX()));
            double decimalZ = Math.abs(mc.player.getZ()) - Math.floor(Math.abs(mc.player.getZ()));
            int lengthXPos = this.calcLength(decimalX, false);
            int lengthXNeg = this.calcLength(decimalX, true);
            int lengthZPos = this.calcLength(decimalZ, false);
            int lengthZNeg = this.calcLength(decimalZ, true);
            ArrayList<BlockPos> tempOffsets = new ArrayList<>();
            offsets.addAll(this.getOverlapPos());
            for (x = 1; x < lengthXPos + 1; ++x) {
                tempOffsets.add(this.addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
                tempOffsets.add(this.addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
            }
            for (x = 0; x <= lengthXNeg; ++x) {
                tempOffsets.add(this.addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
                tempOffsets.add(this.addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
            }
            for (z = 1; z < lengthZPos + 1; ++z) {
                tempOffsets.add(this.addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
                tempOffsets.add(this.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
            }
            for (z = 0; z <= lengthZNeg; ++z) {
                tempOffsets.add(this.addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
                tempOffsets.add(this.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
            }
            for (BlockPos pos : tempOffsets) {
                if (getDown(pos)) {
                    offsets.add(pos.add(0, -1, 0));
                }
                offsets.add(pos);
            }
        } else {
            offsets.add(playerPos.add(0, -1, 0));
            for (int[] surround : new int[][]{
                    {1, 0},
                    {0, 1},
                    {-1, 0},
                    {0, -1}
            }) {
                if (getDown(playerPos.add(surround[0], 0, surround[1])))
                    offsets.add(playerPos.add(surround[0], -1, surround[1]));

                offsets.add(playerPos.add(surround[0], 0, surround[1]));
            }
        }
        return offsets;
    }

    public static boolean getDown(BlockPos pos) {

        for (Direction e : Direction.values())
            if (!mc.world.isAir(pos.add(e.getVector())))
                return false;

        return true;

    }

    int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }

    BlockPos addToPlayer(BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) {
            x = -x;
        }
        if (playerPos.getY() < 0) {
            y = -y;
        }
        if (playerPos.getZ() < 0) {
            z = -z;
        }
        return playerPos.add(BlockPos.ofFloored(x,y,z));
    }

    List<BlockPos> getOverlapPos() {
        ArrayList<BlockPos> positions = new ArrayList<>();
        double decimalX = mc.player.getX() - Math.floor(mc.player.getX());
        double decimalZ = mc.player.getZ() - Math.floor(mc.player.getZ());
        int offX = this.calcOffset(decimalX);
        int offZ = this.calcOffset(decimalZ);
        positions.add(this.getPlayerPos());
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(this.getPlayerPos().add(properX, -1, properZ));
            }
        }
        return positions;
    }


    int calcLength(double decimal, boolean negative) {
        if (negative) {
            return decimal <= 0.3 ? 1 : 0;
        }
        return decimal >= 0.7 ? 1 : 0;
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

    private @NotNull BlockPos getPlayerPos() {
        return BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - Math.floor(mc.player.getY()) > 0.8 ? Math.floor(mc.player.getY()) + 1.0 : Math.floor(mc.player.getY()), mc.player.getZ());
    }
}
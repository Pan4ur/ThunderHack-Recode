package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.BlockAnimationUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static thunder.hack.modules.client.MainSettings.isRu;

public class Surround extends Module {
    private final Setting<PlaceTiming> placeTiming = new Setting<>("Place Timing", PlaceTiming.Default);
    private final Setting<Integer> actionDelay = new Setting<>("Delay", 1, 1, 1000, v -> placeTiming.getValue() == PlaceTiming.Default);
    private final Setting<Integer> blocksPerTick = new Setting<>("Block Per Action", 8, 1, 12, v -> placeTiming.getValue() == PlaceTiming.Default);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10, v -> placeTiming.getValue() != PlaceTiming.Sequential);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    private final Setting<InteractMode> placeMode = new Setting<>("Place Mode", InteractMode.Normal);
    private final Setting<InventoryUtility.SwitchMode> switchMode = new Setting<>("Switch Mode", InventoryUtility.SwitchMode.All);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<Boolean> center = new Setting<>("Center", true);

    private final Setting<Parent> crystalBreaker = new Setting<>("Crystal Breaker", new Parent(false, 0));
    private final Setting<Boolean> breakCrystal = new Setting<>("Break Crystal", true).withParent(crystalBreaker);
    private final Setting<Integer> breakDelay = new Setting<>("Break Delay", 100, 1, 1000).withParent(crystalBreaker);
    private final Setting<Boolean> remove = new Setting<>("Remove", false).withParent(crystalBreaker);
    private final Setting<Boolean> alwaysBreak = new Setting<>("Always Break", true).withParent(crystalBreaker);
    private final Setting<InteractMode> breakCrystalMode = new Setting<>("Break Crystal Mode", InteractMode.Normal).withParent(crystalBreaker);
    private final Setting<Boolean> antiSelfPop = new Setting<>("Anti Self Pop", true).withParent(crystalBreaker);
    private final Setting<Boolean> antiWeakness = new Setting<>("Anti Weakness", false).withParent(crystalBreaker);

    private final Setting<Parent> blocks = new Setting<>("Blocks", new Parent(false, 0));
    private final Setting<Boolean> obsidian = new Setting<>("Obsidian", true).withParent(blocks);
    private final Setting<Boolean> anchor = new Setting<>("Anchor", false).withParent(blocks);
    private final Setting<Boolean> enderChest = new Setting<>("Ender Chest", true).withParent(blocks);
    private final Setting<Boolean> netherite = new Setting<>("Netherite", false).withParent(blocks);
    private final Setting<Boolean> cryingObsidian = new Setting<>("Crying Obsidian", true).withParent(blocks);
    private final Setting<Boolean> dirt = new Setting<>("Dirt", false).withParent(blocks);

    private final Setting<Parent> autoDisable = new Setting<>("Auto Disable", new Parent(false, 0));
    private final Setting<Boolean> onYChange = new Setting<>("On Y Change", true).withParent(autoDisable);
    private final Setting<Boolean> onTp = new Setting<>("On Tp", true).withParent(autoDisable);

    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    private final Setting<BlockAnimationUtility.BlockRenderMode> renderMode = new Setting<>("Render Mode", BlockAnimationUtility.BlockRenderMode.All).withParent(renderCategory);
    private final Setting<BlockAnimationUtility.BlockAnimationMode> animationMode = new Setting<>("Animation Mode", BlockAnimationUtility.BlockAnimationMode.Fade).withParent(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("Render Fill Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("Render Line Color", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("Render Line Width", 2, 1, 5).withParent(renderCategory);

    private enum PlaceTiming {
        Default,
        Vanilla,
        Sequential
    }

    private enum InteractMode {
        Packet,
        Normal,
        All
    }

    public static final Timer inactivityTimer = new Timer();
    public static final Timer attackTimer = new Timer();
    private final List<BlockPos> sequentialBlocks = new ArrayList<>();

    private int delay;
    private double prevY;
    private Thread mainThread;
    private BlockPos currentPlacePos = null;

    public Surround() {
        super("Surround", "Окружает тебя блоками", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        currentPlacePos = null;
        delay = 0;
        prevY = mc.player.getY();

        mainThread = new Thread(() -> {
            while (isEnabled()) {
                try {
                    Thread.sleep(actionDelay.getValue());
                } catch (InterruptedException ignored) {
                }

                if (placeTiming.getValue() == PlaceTiming.Default) {
                    int placed = 0;
                    while (placed < blocksPerTick.getValue()) {
                        if (getSlot() == -1) disable(isRu() ? "Нет блоков!" : "No blocks!");

                        BlockPos targetBlock = getSequentialPos();
                        if (targetBlock == null)
                            break;
                        currentPlacePos = targetBlock;

                        if (placeBlock(targetBlock)) {
                            delay = placeDelay.getValue();
                            inactivityTimer.reset();
                        } else break;
                        placed++;
                    }
                }
            }
        });
        mainThread.start();

        if (center.getValue()) {
            mc.player.updatePosition(MathHelper.floor(mc.player.getX()) + 0.5, mc.player.getY(), MathHelper.floor(mc.player.getZ()) + 0.5);
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
        }
    }

    @Override
    public void onDisable() {
        if (mainThread != null && !mainThread.isInterrupted()) {
            mainThread.interrupt();
            mainThread = null;
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onEntitySpawn(EventEntitySpawn e) {
        if (breakCrystal.getValue() && !alwaysBreak.getValue()) {
            Entity entity = getEntity(currentPlacePos);
            if (entity != null)
                removeCrystal(entity);
        }

        handleSurroundBreak();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onSync(EventSync event) {
        if (mc.player == null) return;

        if (onYChange.getValue() && mc.player.getY() != prevY)
            disable(isRu() ? "Выключен из-за изменения Y!" : "Disabled due to Y change!");
    }

    @EventHandler
    public void onPostSync(@SuppressWarnings("unused") EventPostSync e) {
        List<BlockPos> blocks = getBlocks();
        if (blocks.isEmpty()) return;

        if (delay > 0) {
            delay--;
            return;
        }

        if (getSlot() == -1) disable(isRu() ? "Нет блоков!" : "No blocks!");


        if (placeTiming.getValue() == PlaceTiming.Vanilla || placeTiming.getValue() == PlaceTiming.Sequential) {
            BlockPos targetBlock = getSequentialPos();
            if (targetBlock == null)
                return;
            currentPlacePos = targetBlock;
            if (placeBlock(targetBlock)) {
                sequentialBlocks.add(targetBlock);
                delay = placeDelay.getValue();
                inactivityTimer.reset();
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (getSlot() == -1) disable(isRu() ? "Нет блоков!" : "No blocks!");

        if (e.getPacket() instanceof BlockUpdateS2CPacket pac && mc.player != null) {
            if (placeTiming.getValue() == PlaceTiming.Sequential && !sequentialBlocks.isEmpty()) {
                handleSequential(pac.getPos());
            }
            if (mc.player.squaredDistanceTo(pac.getPos().toCenterPos()) < 9 && pac.getState() == Blocks.AIR.getDefaultState()) {
                handleSurroundBreak();
            }
        }
        if (e.getPacket() instanceof BlockBreakingProgressS2CPacket pac && mc.player != null) {
            if (placeTiming.getValue() == PlaceTiming.Sequential && !sequentialBlocks.isEmpty()) {
                handleSequential(pac.getPos());
            }
            if (mc.player.squaredDistanceTo(pac.getPos().toCenterPos()) < 16) {
                handleSurroundBreak();
            }
        }

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket && onTp.getValue())
            disable(isRu() ? "Выключен из-за руббербенда!" : "Disabled due to teleport!");
    }

    private void handleSurroundBreak() {
        BlockPos bp = getSequentialPos();
        if (bp != null) {
            currentPlacePos = bp;
            if (placeBlock(bp))
                inactivityTimer.reset();
        }
    }

    public void handleSequential(BlockPos pos) {
        if (sequentialBlocks.contains(pos)) {
            BlockPos bp = getSequentialPos();
            if (bp != null) {
                currentPlacePos = bp;
                if (placeBlock(bp)) {
                    sequentialBlocks.add(bp);
                    sequentialBlocks.remove(pos);
                    inactivityTimer.reset();
                }
            }
        }
    }

    private boolean placeBlock(BlockPos pos) {
        boolean validInteraction = false;

        int slot = getSlot();
        Entity crystal;
        if (breakCrystal.getValue() && alwaysBreak.getValue() && (crystal = getEntity(pos)) != null) {
            if (!removeCrystal(crystal))
                return false;
        }
        if (slot == -1)
            return false;

        if (placeMode.getValue() == InteractMode.Packet || placeMode.getValue() == InteractMode.All) {
            validInteraction = InteractionUtility.placeBlock(pos, rotate.getValue(), interact.getValue(), InteractionUtility.PlaceMode.Packet, slot, true, switchMode.getValue(), false);
        }
        if (placeMode.getValue() == InteractMode.Normal || placeMode.getValue() == InteractMode.All) {
            validInteraction = InteractionUtility.placeBlock(pos, rotate.getValue(), interact.getValue(), InteractionUtility.PlaceMode.Normal, slot, true, switchMode.getValue(), false);
        }

        if (validInteraction) {
            BlockAnimationUtility.renderBlock(pos,
                    renderLineColor.getValue().getColorObject(),
                    renderLineWidth.getValue(),
                    renderFillColor.getValue().getColorObject(),
                    animationMode.getValue(),
                    renderMode.getValue()
            );
        }

        return validInteraction;
    }

    private @Nullable EndCrystalEntity getEntity(BlockPos blockPos) {
        if (blockPos == null || mc.world == null) return null;

        return hasEntity(new Box(blockPos), entity -> entity instanceof EndCrystalEntity) ?
                (EndCrystalEntity) mc.world.getOtherEntities(null, new Box(blockPos), entity -> entity instanceof EndCrystalEntity).get(0) : null;
    }

    private boolean hasEntity(Box box, Predicate<Entity> predicate) {
        if (mc.world == null) return false;

        try {
            return !mc.world.getOtherEntities(null, box, predicate).isEmpty();
        } catch (java.util.ConcurrentModificationException ignored) {
        }

        return false;
    }

    private boolean removeCrystal(Entity entity) {
        if (mc.player == null
                || mc.world == null
                || mc.interactionManager == null
                || !(entity instanceof EndCrystalEntity)
                || !attackTimer.passedMs(breakDelay.getValue())
                || mc.player.squaredDistanceTo(entity) > 25) return false;
        attackTimer.reset();

        if (antiSelfPop.getValue() && mc.player.getHealth() - ExplosionUtility.getSelfExplosionDamage(entity.getPos()) <= 0) {
            return false;
        }

        int preSlot = mc.player.getInventory().selectedSlot;
        if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            final SearchInvResult result = InventoryUtility.findInHotBar(stack -> stack.getItem() instanceof SwordItem || stack.getItem() instanceof PickaxeItem);
            if (!result.found())
                return false;

            result.switchTo(switchMode.getValue());
        }

        if (breakCrystalMode.getValue() == InteractMode.Packet || breakCrystalMode.getValue() == InteractMode.All) {
            sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
        }
        if (breakCrystalMode.getValue() == InteractMode.Normal || breakCrystalMode.getValue() == InteractMode.All) {
            mc.interactionManager.attackEntity(mc.player, entity);
        }
        sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        if (remove.getValue()) {
            mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
        }

        if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            InventoryUtility.switchTo(preSlot, switchMode.getValue());
        }

        return true;
    }

    private @Nullable BlockPos getSequentialPos() {
        if (mc.player == null || mc.world == null) return null;

        for (BlockPos bp : getBlocks()) {
            if (new Box(bp).intersects(mc.player.getBoundingBox())) continue;
            if (InteractionUtility.canPlaceBlock(bp, interact.getValue(), true) && mc.world.getBlockState(bp).isReplaceable()) {
                return bp;
            }
        }

        return null;
    }

    private @NotNull List<BlockPos> getBlocks() {
        BlockPos playerPos = getPlayerPos();
        ArrayList<BlockPos> offsets = new ArrayList<>();
        if (playerPos == null) return offsets;

        if (!center.getValue() && mc.player != null) {
            int z;
            int x;
            double decimalX = Math.abs(mc.player.getX()) - Math.floor(Math.abs(mc.player.getX()));
            double decimalZ = Math.abs(mc.player.getZ()) - Math.floor(Math.abs(mc.player.getZ()));
            int lengthXPos = calcLength(decimalX, false);
            int lengthXNeg = calcLength(decimalX, true);
            int lengthZPos = calcLength(decimalZ, false);
            int lengthZNeg = calcLength(decimalZ, true);
            ArrayList<BlockPos> tempOffsets = new ArrayList<>();
            offsets.addAll(getOverlapPos());

            for (x = 1; x < lengthXPos + 1; ++x) {
                tempOffsets.add(addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
                tempOffsets.add(addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
            }
            for (x = 0; x <= lengthXNeg; ++x) {
                tempOffsets.add(addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
                tempOffsets.add(addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
            }
            for (z = 1; z < lengthZPos + 1; ++z) {
                tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
                tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
            }
            for (z = 0; z <= lengthZNeg; ++z) {
                tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
                tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
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
        if (mc.world == null) return false;

        for (Direction e : Direction.values())
            if (!mc.world.getBlockState(pos.add(e.getVector())).isReplaceable())
                return false;

        return true;
    }

    private int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }

    private BlockPos addToPlayer(@NotNull BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) {
            x = -x;
        }
        if (playerPos.getY() < 0) {
            y = -y;
        }
        if (playerPos.getZ() < 0) {
            z = -z;
        }
        return playerPos.add(BlockPos.ofFloored(x, y, z));
    }

    private @NotNull List<BlockPos> getOverlapPos() {
        ArrayList<BlockPos> positions = new ArrayList<>();

        if (mc.player != null) {
            double decimalX = mc.player.getX() - Math.floor(mc.player.getX());
            double decimalZ = mc.player.getZ() - Math.floor(mc.player.getZ());
            int offX = calcOffset(decimalX);
            int offZ = calcOffset(decimalZ);
            positions.add(getPlayerPos());
            for (int x = 0; x <= Math.abs(offX); ++x) {
                for (int z = 0; z <= Math.abs(offZ); ++z) {
                    int properX = x * offX;
                    int properZ = z * offZ;
                    positions.add(Objects.requireNonNull(getPlayerPos()).add(properX, -1, properZ));
                }
            }
        }

        return positions;
    }


    private int calcLength(double decimal, boolean negative) {
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

        if (mc.player == null) return slot;
        final ItemStack mainhandStack = mc.player.getMainHandStack();
        if (mainhandStack != ItemStack.EMPTY && mainhandStack.getItem() instanceof BlockItem) {
            final Block blockFromMainhandItem = ((BlockItem) mainhandStack.getItem()).getBlock();
            if (canUseBlocks.contains(blockFromMainhandItem)) {
                slot = mc.player.getInventory().selectedSlot;
            }
        }


        if (slot == -1) {
            return InventoryUtility.findBlockInHotBar(canUseBlocks).slot();
        }

        return slot;
    }

    private @Nullable BlockPos getPlayerPos() {
        if (mc.player == null) return null;

        return BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - Math.floor(mc.player.getY()) > 0.8 ? Math.floor(mc.player.getY()) + 1.0 : Math.floor(mc.player.getY()), mc.player.getZ());
    }
}
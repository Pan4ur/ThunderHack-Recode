package dev.thunderhack.modules.combat;

import com.google.common.collect.Lists;
import dev.thunderhack.event.events.EventEntitySpawn;
import dev.thunderhack.event.events.EventPostSync;
import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.setting.settings.Parent;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.math.ExplosionUtility;
import dev.thunderhack.utils.player.InteractionUtility;
import dev.thunderhack.utils.player.InventoryUtility;
import dev.thunderhack.utils.player.PlayerUtility;
import dev.thunderhack.utils.player.SearchInvResult;
import dev.thunderhack.utils.render.BlockAnimationUtility;
import dev.thunderhack.utils.world.HoleUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.event.events.*;
import dev.thunderhack.modules.client.HudEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.thunderhack.modules.client.MainSettings.isRu;

public class Surround extends Module {
    private final Setting<Sequential> useSequential = new Setting<>("Use Sequence", Sequential.None);
    private final Setting<PlaceTiming> placeTiming = new Setting<>("Place Timing", PlaceTiming.Default);
    private final Setting<Float> range = new Setting<>("Range", 5f, 0f, 7f);
    private final Setting<Integer> blocksPerTick = new Setting<>("Blocks/Place", 8, 1, 12, v -> placeTiming.getValue() == PlaceTiming.Default);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10, v -> placeTiming.getValue() != PlaceTiming.Sequential);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);
    private final Setting<InteractMode> placeMode = new Setting<>("Place Mode", InteractMode.Normal);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private final Setting<CenterMode> center = new Setting<>("Center", CenterMode.Disabled);

    private final Setting<Parent> crystalBreaker = new Setting<>("CrystalBreaker", new Parent(false, 0));
    private final Setting<Boolean> breakCrystal = new Setting<>("BreakCrystal", true).withParent(crystalBreaker);
    private final Setting<Integer> breakDelay = new Setting<>("BreakDelay", 100, 1, 1000).withParent(crystalBreaker);
    private final Setting<Boolean> remove = new Setting<>("Remove", false).withParent(crystalBreaker);
    private final Setting<InteractMode> breakCrystalMode = new Setting<>("BreakMode", InteractMode.Normal).withParent(crystalBreaker);
    private final Setting<Boolean> antiSelfPop = new Setting<>("AntiSelfPop", true).withParent(crystalBreaker);
    private final Setting<Boolean> antiWeakness = new Setting<>("AntiWeakness", false).withParent(crystalBreaker);

    private final Setting<Parent> blocks = new Setting<>("Blocks", new Parent(false, 0));
    private final Setting<Boolean> obsidian = new Setting<>("Obsidian", true).withParent(blocks);
    private final Setting<Boolean> anchor = new Setting<>("Anchor", false).withParent(blocks);
    private final Setting<Boolean> enderChest = new Setting<>("EnderChest", true).withParent(blocks);
    private final Setting<Boolean> netherite = new Setting<>("Netherite", false).withParent(blocks);
    private final Setting<Boolean> cryingObsidian = new Setting<>("Crying Obsidian", true).withParent(blocks);
    private final Setting<Boolean> dirt = new Setting<>("Dirt", false).withParent(blocks);

    private final Setting<Parent> autoDisable = new Setting<>("Auto Disable", new Parent(false, 0));
    private final Setting<Boolean> onYChange = new Setting<>("On Y Change", true).withParent(autoDisable);
    private final Setting<OnTpAction> onTp = new Setting<>("On Tp", OnTpAction.None).withParent(autoDisable);
    private final Setting<Boolean> onDeath = new Setting<>("On Death", false).withParent(autoDisable);

    private final Setting<Parent> renderCategory = new Setting<>("Render", new Parent(false, 0));
    private final Setting<BlockAnimationUtility.BlockRenderMode> renderMode = new Setting<>("RenderMode", BlockAnimationUtility.BlockRenderMode.All).withParent(renderCategory);
    private final Setting<BlockAnimationUtility.BlockAnimationMode> animationMode = new Setting<>("AnimationMode", BlockAnimationUtility.BlockAnimationMode.Fade).withParent(renderCategory);
    private final Setting<ColorSetting> renderFillColor = new Setting<>("RenderFillColor", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<ColorSetting> renderLineColor = new Setting<>("RenderLineColor", new ColorSetting(HudEditor.getColor(0))).withParent(renderCategory);
    private final Setting<Integer> renderLineWidth = new Setting<>("RenderLineWidth", 2, 1, 5).withParent(renderCategory);

    public static final Timer inactivityTimer = new Timer();
    public static final Timer attackTimer = new Timer();
    private final List<BlockPos> sequentialBlocks = new ArrayList<>();

    private boolean wasTp = false;
    private int delay;
    private double prevY;

    public Surround() {
        super("Surround", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        delay = 0;
        wasTp = false;
        prevY = mc.player.getY();

        // Centering
        if (center.getValue() == CenterMode.Teleport) {
            mc.player.updatePosition(MathHelper.floor(mc.player.getX()) + 0.5, mc.player.getY(), MathHelper.floor(mc.player.getZ()) + 0.5);
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
        }
    }

    @Override
    public void onUpdate() {
        if ((mc.player.isDead() || mc.player.getHealth() + mc.player.getAbsorptionAmount() <= 0) && onDeath.getValue())
            disable(isRu() ? "Выключен из-за смерти." : "Disable because you died.");
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPostSync(EventPostSync event) {
        if (prevY != mc.player.getY() && onYChange.getValue() && !wasTp) {
            disable(isRu() ? "Отключён из-за изменения Y!" : "Disabled due to Y change!");
            return;
        }
        if (wasTp && mc.player.isOnGround()) wasTp = false;
        prevY = mc.player.getY();

        Vec3d centerVec = new Vec3d(
                MathHelper.floor(mc.player.getX()) + 0.5,
                mc.player.getY(),
                MathHelper.floor(mc.player.getZ()) + 0.5
        );
        Box centerBox = new Box(
                centerVec.getX() - 0.2, centerVec.getY() - 0.1, centerVec.getZ() - 0.2,
                centerVec.getX() + 0.2, centerVec.getY() + 0.1, centerVec.getZ() + 0.2
        );
        if (center.getValue() == CenterMode.Motion && !centerBox.contains(mc.player.getPos())) {
            mc.player.move(MovementType.SELF, new Vec3d((centerVec.getX() - mc.player.getX()) / 2, 0, (centerVec.getZ() - mc.player.getZ()) / 2));
            return;
        }

        List<BlockPos> blocks = getBlocks();
        if (blocks.isEmpty()) return;

        if (delay > 0) {
            delay--;
            return;
        }

        if (getSlot() == -1)
            disable(isRu() ? "Нет блоков!" : "No blocks!");

        if (placeTiming.getValue() == PlaceTiming.Vanilla || placeTiming.getValue() == PlaceTiming.Sequential) {
            BlockPos targetBlock = getSequentialPos();
            if (targetBlock == null)
                return;
            if (placeBlock(targetBlock)) {
                sequentialBlocks.add(targetBlock);
                delay = placeDelay.getValue();
                inactivityTimer.reset();
            }
        } else {
            int placed = 0;
            if (delay > 0) return;
            while (placed < blocksPerTick.getValue()) {
                if (getSlot() == -1) disable(isRu() ? "Нет блоков!" : "No blocks!");

                BlockPos targetBlock = getSequentialPos();
                if (targetBlock == null)
                    break;

                if (placeBlock(targetBlock)) {
                    placed++;
                    delay = placeDelay.getValue();
                    inactivityTimer.reset();
                } else break;
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntitySpawn(@NotNull EventEntitySpawn event) {
        if (event.getEntity() instanceof EndCrystalEntity && breakCrystal.getValue()) {
            if (event.getEntity().squaredDistanceTo(mc.player) <= remove.getPow2Value()) {
                removeCrystal(event.getEntity());
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.@NotNull Receive event) {
        if (getSlot() == -1) disable(isRu() ? "Нет блоков!" : "No blocks!");

        if (event.getPacket() instanceof BlockUpdateS2CPacket pac && mc.player != null) {
            if (placeTiming.getValue() == PlaceTiming.Sequential && !sequentialBlocks.isEmpty()) {
                handleSequential(pac.getPos());
            }
            if (mc.player.squaredDistanceTo(pac.getPos().toCenterPos()) < range.getPow2Value() && pac.getState().isReplaceable()) {
                handleSurroundBreak();
            }
        }
        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket pac && mc.player != null) {
            if (placeTiming.getValue() == PlaceTiming.Sequential && !sequentialBlocks.isEmpty())
                handleSequential(pac.getPos());
            if (mc.player.squaredDistanceTo(pac.getPos().toCenterPos()) < range.getPow2Value())
                handleSurroundBreak();
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket)
            switch (onTp.getValue()) {
                case Disable -> disable(isRu() ? "Выключен из-за руббербенда!" : "Disabled due to a rubberband!");
                case Stay -> wasTp = true;
            }
    }

    private void handleSurroundBreak() {
        BlockPos bp = getSequentialPos();
        if (bp != null) {
            if (placeBlock(bp))
                inactivityTimer.reset();
        }
    }

    public void handleSequential(BlockPos pos) {
        if (sequentialBlocks.contains(pos)) {
            BlockPos bp = getSequentialPos();
            if (bp != null) {
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

        if (slot == -1)
            return false;

        if (useSequential.getValue() != Sequential.None) {
            if (useSequential.getValue() == Sequential.PlaceEat && PlayerUtility.isEating())
                mc.options.useKey.setPressed(false);
            else if (useSequential.getValue() == Sequential.EatPlace && PlayerUtility.isEating())
                return false;
        }


        if (placeMode.getValue() == InteractMode.Packet || placeMode.getValue() == InteractMode.All) {
            validInteraction = InteractionUtility.placeBlock(pos, rotate.getValue(), interact.getValue(), InteractionUtility.PlaceMode.Packet, slot, true, false);
        }
        if (placeMode.getValue() == InteractMode.Normal || placeMode.getValue() == InteractMode.All) {
            validInteraction = InteractionUtility.placeBlock(pos, rotate.getValue(), interact.getValue(), InteractionUtility.PlaceMode.Normal, slot, true, false);
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

    private void removeCrystal(Entity entity) {
        if (fullNullCheck()
                || !(entity instanceof EndCrystalEntity)
                || !attackTimer.passedMs(breakDelay.getValue())
                || mc.player.squaredDistanceTo(entity) > range.getPow2Value()
                || !breakCrystal.getValue())
            return;
        if (antiSelfPop.getValue() && mc.player.getHealth() + mc.player.getAbsorptionAmount() - ExplosionUtility.getSelfExplosionDamage(entity.getPos()) <= 2)
            return;

        int preSlot = mc.player.getInventory().selectedSlot;
        if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
            final SearchInvResult result = InventoryUtility.getAntiWeaknessItem();
            if (!result.found())
                return;

            result.switchTo();
        }

        if (breakCrystalMode.getValue() == InteractMode.Packet || breakCrystalMode.getValue() == InteractMode.All)
            sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));

        if (breakCrystalMode.getValue() == InteractMode.Normal || breakCrystalMode.getValue() == InteractMode.All)
            mc.interactionManager.attackEntity(mc.player, entity);

        sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        attackTimer.reset();

        if (remove.getValue()) mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);

        if (antiWeakness.getValue() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS))
            InventoryUtility.switchTo(preSlot);
    }

    private @Nullable BlockPos getSequentialPos() {
        if (mc.player == null || mc.world == null) return null;

        for (BlockPos bp : getBlocks()) {
            if (breakCrystal.getValue()) {
                Lists.newArrayList(mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(bp))).stream()
                        .filter(entity -> mc.player.squaredDistanceTo(entity) <= range.getPow2Value())
                        .forEach(this::removeCrystal);
            }

            if (new Box(bp).intersects(mc.player.getBoundingBox()))
                continue;
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

        if (center.getValue() == CenterMode.Disabled && mc.player != null) {
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
                if (getDown(pos))
                    offsets.add(pos.add(0, -1, 0));
                offsets.add(pos);
            }
        } else {
            offsets.add(playerPos.add(0, -1, 0));

            for (Vec3i surround : HoleUtils.VECTOR_PATTERN) {
                if (getDown(playerPos.add(surround)))
                    offsets.add(playerPos.add(surround.getX(), -1, surround.getZ()));

                offsets.add(playerPos.add(surround));
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
        if (playerPos.getX() < 0) x = -x;
        if (playerPos.getY() < 0) y = -y;
        if (playerPos.getZ() < 0) z = -z;
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
        if (negative) return decimal <= 0.3 ? 1 : 0;
        return decimal >= 0.7 ? 1 : 0;
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

        if (mc.player == null) return slot;
        final ItemStack mainHandStack = mc.player.getMainHandStack();
        if (mainHandStack != ItemStack.EMPTY && mainHandStack.getItem() instanceof BlockItem) {
            final Block blockFromMainHandItem = ((BlockItem) mainHandStack.getItem()).getBlock();
            if (canUseBlocks.contains(blockFromMainHandItem)) {
                slot = mc.player.getInventory().selectedSlot;
            }
        }

        return slot == -1 ? InventoryUtility.findBlockInHotBar(canUseBlocks).slot() : slot;
    }

    private @Nullable BlockPos getPlayerPos() {
        if (mc.player == null) return null;

        return BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - Math.floor(mc.player.getY()) > 0.8 ? Math.floor(mc.player.getY()) + 1.0 : Math.floor(mc.player.getY()), mc.player.getZ());
    }

    private enum PlaceTiming {
        Default,
        Vanilla,
        Sequential
    }

    private enum CenterMode {
        Teleport,
        Motion,
        Disabled
    }

    private enum OnTpAction {
        Disable,
        Stay,
        None
    }

    private enum InteractMode {
        Packet,
        Normal,
        All
    }

    private enum Sequential {
        PlaceEat,
        EatPlace,
        None
    }
}

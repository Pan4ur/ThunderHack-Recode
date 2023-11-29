package thunder.hack.modules.combat;

import com.google.common.collect.Lists;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.events.impl.*;
import thunder.hack.modules.base.IndestructibleModule;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static thunder.hack.modules.client.MainSettings.isRu;

public final class Surround extends IndestructibleModule {
    private final Setting<PlaceTiming> placeTiming = new Setting<>("Place Timing", PlaceTiming.Default);
    private final Setting<Integer> blocksPerTick = new Setting<>("Blocks/Place", 8, 1, 12, v -> placeTiming.getValue() == PlaceTiming.Default);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10, v -> placeTiming.getValue() != PlaceTiming.Sequential);
    private final Setting<CenterMode> center = new Setting<>("Center", CenterMode.Disabled);

    private final Setting<Parent> autoDisable = new Setting<>("Auto Disable", new Parent(false, 0));
    private final Setting<Boolean> onYChange = new Setting<>("On Y Change", true).withParent(autoDisable);
    private final Setting<OnTpAction> onTp = new Setting<>("On Tp", OnTpAction.None).withParent(autoDisable);
    private final Setting<Boolean> onDeath = new Setting<>("On Death", false).withParent(autoDisable);

    private final List<BlockPos> sequentialBlocks = new ArrayList<>();

    private boolean wasTp = false;
    private int delay;
    private double prevY;
    private static Surround instance;

    public Surround() {
        super("Surround", Category.COMBAT);
        instance = this;
    }

    public static Surround getInstance() {
        return instance;
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
        if ((mc.player.isDead() || !mc.player.isAlive() || mc.player.getHealth() + mc.player.getAbsorptionAmount() <= 0) && onDeath.getValue())
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

        if (!getBlockResult().found())
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
                if (!getBlockResult().found()) disable(isRu() ? "Нет блоков!" : "No blocks!");

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
        if (event.getEntity() instanceof EndCrystalEntity && crystalBreaker.getValue().isEnabled()) {
            if (event.getEntity().squaredDistanceTo(mc.player) <= remove.getPow2Value()) {
                breakCrystal((EndCrystalEntity) event.getEntity());
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.@NotNull Receive event) {
        if (!getBlockResult().found()) disable(isRu() ? "Нет блоков!" : "No blocks!");

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

    private @Nullable BlockPos getSequentialPos() {
        if (mc.player == null || mc.world == null) return null;

        for (BlockPos bp : getBlocks()) {
            if (new Box(bp).intersects(mc.player.getBoundingBox()))
                continue;
            if (InteractionUtility.canPlaceBlock(bp, interact.getValue(), true) && mc.world.getBlockState(bp).isReplaceable()) {
                return bp;
            }
        }

        return null;
    }

    private @NotNull List<BlockPos> getBlocks() {
        final BlockPos playerPos = getPlayerPos();
        final List<BlockPos> offsets = new ArrayList<>();

        if (center.getValue() == CenterMode.Disabled && mc.player != null) {
            int z;
            int x;
            final double decimalX = Math.abs(mc.player.getX()) - Math.floor(Math.abs(mc.player.getX()));
            final double decimalZ = Math.abs(mc.player.getZ()) - Math.floor(Math.abs(mc.player.getZ()));
            final int lengthXPos = HoleUtility.calcLength(decimalX, false);
            final int lengthXNeg = HoleUtility.calcLength(decimalX, true);
            final int lengthZPos = HoleUtility.calcLength(decimalZ, false);
            final int lengthZNeg = HoleUtility.calcLength(decimalZ, true);
            final ArrayList<BlockPos> tempOffsets = new ArrayList<>();
            offsets.addAll(getOverlapPos());

            for (x = 1; x < lengthXPos + 1; ++x) {
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
            }
            for (x = 0; x <= lengthXNeg; ++x) {
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
            }
            for (z = 1; z < lengthZPos + 1; ++z) {
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
            }
            for (z = 0; z <= lengthZNeg; ++z) {
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
                tempOffsets.add(HoleUtility.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
            }

            for (BlockPos pos : tempOffsets) {
                if (getDown(pos))
                    offsets.add(pos.add(0, -1, 0));
                offsets.add(pos);
            }
        } else {
            offsets.add(playerPos.add(0, -1, 0));

            for (Vec3i surround : HoleUtility.VECTOR_PATTERN) {
                if (getDown(playerPos.add(surround)))
                    offsets.add(playerPos.add(surround.getX(), -1, surround.getZ()));

                offsets.add(playerPos.add(surround));
            }
        }

        return offsets;
    }

    private boolean getDown(BlockPos pos) {
        if (mc.world == null) return false;

        for (Direction dir : Direction.values())
            if (!mc.world.getBlockState(pos.add(dir.getVector())).isReplaceable())
                return false;

        return mc.world.getBlockState(pos).isReplaceable()
                && interact.getValue() != InteractionUtility.Interact.AirPlace;
    }

    private @NotNull List<BlockPos> getOverlapPos() {
        List<BlockPos> positions = new ArrayList<>();

        if (mc.player != null) {
            double decimalX = mc.player.getX() - Math.floor(mc.player.getX());
            double decimalZ = mc.player.getZ() - Math.floor(mc.player.getZ());
            int offX = HoleUtility.calcOffset(decimalX);
            int offZ = HoleUtility.calcOffset(decimalZ);
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

    private @NotNull BlockPos getPlayerPos() {
        return BlockPos.ofFloored(mc.player.getX(),
                mc.player.getY() - Math.floor(mc.player.getY()) > 0.8 ? Math.floor(mc.player.getY()) + 1.0 : Math.floor(mc.player.getY()),
                mc.player.getZ()
        );
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
}

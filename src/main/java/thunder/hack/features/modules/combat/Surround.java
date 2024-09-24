package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventTick;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.base.PlaceModule;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.*;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class Surround extends PlaceModule {
    private final Setting<Integer> blocksPerTick = new Setting<>("Blocks/Place", 8, 1, 12);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10);
    private final Setting<CenterMode> center = new Setting<>("Center", CenterMode.Disabled);
    private final Setting<SettingGroup> autoDisable = new Setting<>("Auto Disable", new SettingGroup(false, 0));
    private final Setting<Boolean> onYChange = new Setting<>("On Y Change", true).addToGroup(autoDisable);
    private final Setting<OnTpAction> onTp = new Setting<>("On Tp", OnTpAction.None).addToGroup(autoDisable);
    private final Setting<Boolean> onDeath = new Setting<>("On Death", false).addToGroup(autoDisable);

    private int delay;
    private double prevY;

    public Surround() {
        super("Surround", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        delay = 0;
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
    private void onTick(EventTick event) {
        if (prevY != mc.player.getY() && onYChange.getValue() && ThunderHack.core.getSetBackTime() > 500) {
            disable(isRu() ? "Отключён из-за изменения Y!" : "Disabled due to Y change!");
            return;
        }

        prevY = mc.player.getY();

        Vec3d centerVec = new Vec3d(MathHelper.floor(mc.player.getX()) + 0.5, mc.player.getY(), MathHelper.floor(mc.player.getZ()) + 0.5);

        Box centerBox = new Box(centerVec.getX() - 0.2, centerVec.getY() - 0.1, centerVec.getZ() - 0.2, centerVec.getX() + 0.2, centerVec.getY() + 0.1, centerVec.getZ() + 0.2);

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

        if (!getBlockResult().found()) disable(isRu() ? "Нет блоков!" : "No blocks!");


        int placed = 0;
        if (delay > 0) return;
        while (placed < blocksPerTick.getValue()) {
            if (!getBlockResult().found()) disable(isRu() ? "Нет блоков!" : "No blocks!");

            BlockPos targetBlock = getSequentialPos();
            if (targetBlock == null) break;

            if (placeBlock(targetBlock, true)) {
                placed++;
                delay = placeDelay.getValue();
                inactivityTimer.reset();
            } else break;
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.@NotNull Receive event) {
        if (!getBlockResult().found()) disable(isRu() ? "Нет блоков!" : "No blocks!");

        if (event.getPacket() instanceof EntitySpawnS2CPacket spawn && spawn.getEntityType() == EntityType.END_CRYSTAL) {

            EndCrystalEntity cr = new EndCrystalEntity(mc.world, spawn.getX(), spawn.getY(), spawn.getZ());
            cr.setId(spawn.getId());

            if (crystalBreaker.getValue().isEnabled() && cr.squaredDistanceTo(mc.player) <= remove.getPow2Value())
                handlePacket();
        }

        if (event.getPacket() instanceof BlockUpdateS2CPacket pac && mc.player.squaredDistanceTo(pac.getPos().toCenterPos()) < range.getPow2Value() && pac.getState().isReplaceable())
            handlePacket();

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && onTp.getValue() == OnTpAction.Disable)
            disable(isRu() ? "Выключен из-за руббербенда!" : "Disabled due to a rubberband!");
    }

    private void handlePacket() {
        BlockPos bp = getSequentialPos();
        if (bp != null) {
            if (placeBlock(bp, InteractMode.Packet)) inactivityTimer.reset();
        }
    }

    private @Nullable BlockPos getSequentialPos() {
        for (BlockPos bp : getBlocks()) {
            if (new Box(bp).intersects(mc.player.getBoundingBox())) continue;
            if (InteractionUtility.canPlaceBlock(bp, interact.getValue(), true) && mc.world.getBlockState(bp).isReplaceable())
                return bp;
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
        return BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - Math.floor(mc.player.getY()) > 0.8 ? Math.floor(mc.player.getY()) + 1.0 : Math.floor(mc.player.getY()), mc.player.getZ());
    }

    private enum CenterMode {
        Teleport, Motion, Disabled
    }

    private enum OnTpAction {
        Disable, Stay, None
    }
}

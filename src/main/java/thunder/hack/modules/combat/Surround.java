package thunder.hack.modules.combat;

import com.google.common.collect.Lists;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventTick;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.base.PlaceModule;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.player.InteractionUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static thunder.hack.modules.client.ClientSettings.isRu;

public final class Surround extends PlaceModule {
    private final Setting<Integer> blocksPerTick = new Setting<>("Blocks/Place", 8, 1, 12);
    private final Setting<Integer> placeDelay = new Setting<>("Delay/Place", 3, 0, 10);
    private final Setting<CenterMode> center = new Setting<>("Center", CenterMode.Disabled);
    private final Setting<SettingGroup> autoDisable = new Setting<>("Auto Disable", new SettingGroup(false, 0));
    private final Setting<Boolean> onYChange = new Setting<>("On Y Change", true).addToGroup(autoDisable);
    private final Setting<OnTpAction> onTp = new Setting<>("On Tp", OnTpAction.None).addToGroup(autoDisable);
    private final Setting<Boolean> onDeath = new Setting<>("On Death", false).addToGroup(autoDisable);

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

        if (event.getPacket() instanceof ExplosionS2CPacket p && mc.player.squaredDistanceTo(p.getX(), p.getY(), p.getZ()) < range.getPow2Value())
            handlePacket();

        if (event.getPacket() instanceof PlaySoundS2CPacket p && p.getCategory().equals(SoundCategory.BLOCKS) && p.getSound().value().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && mc.player.squaredDistanceTo(p.getX(), p.getY(), p.getZ()) < range.getPow2Value())
            handlePacket();

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) if (onTp.getValue() == OnTpAction.Disable) {
            disable(isRu() ? "Выключен из-за руббербенда!" : "Disabled due to a rubberband!");
        }
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


    public List<BlockPos> getBlocks() {
        List<BlockPos> finalPoses = new ArrayList<>();
        List<BlockPos> playerPos = new ArrayList<>();
        Box box = mc.player.getBoundingBox();
        double y = mc.player.getY() - Math.floor(mc.player.getY()) > 0.8 ? Math.floor(mc.player.getY()) + 1.0 : Math.floor(mc.player.getY());
        playerPos.add(BlockPos.ofFloored(box.maxX, y, box.maxZ));
        playerPos.add(BlockPos.ofFloored(box.minX, y, box.minZ));
        playerPos.add(BlockPos.ofFloored(box.maxX, y, box.minZ));
        playerPos.add(BlockPos.ofFloored(box.minX, y, box.maxZ));

        for (BlockPos pos : playerPos) {
            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN) continue;
                BlockPos offset = pos.offset(direction);
                if (!playerPos.contains(offset)) {
                    finalPoses.add(offset);
                    finalPoses.add(offset.down());
                }
            }
            finalPoses.add(pos.down());
        }

        for (BlockPos pos : Lists.newArrayList(finalPoses))
            for (PlayerEntity player : ThunderHack.asyncManager.getAsyncPlayers())
                if (player.getBoundingBox().intersects(new Box(pos))) {
                    finalPoses.removeIf(b -> b.equals(pos));
                    for (Direction direction : Direction.values()) {
                        if (direction == Direction.UP || direction == Direction.DOWN) continue;
                        if (player.getBoundingBox().intersects(new Box(pos.offset(direction)))) continue;
                        finalPoses.add(pos.offset(direction));
                    }
                }
        return finalPoses;
    }

    private enum CenterMode {
        Teleport, Motion, Disabled
    }

    private enum OnTpAction {
        Disable, Stay, None
    }
}

package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.*;
import thunder.hack.features.modules.base.PlaceModule;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class Blocker extends PlaceModule {
    private final Setting<Integer> actionShift = new Setting<>("Place Per Tick", 1, 1, 5);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);

    private final Setting<SettingGroup> logic = new Setting<>("Logic", new SettingGroup(false, 0));
    private final Setting<Boolean> antiCev = new Setting<>("Anti Cev", true).addToGroup(logic);
    private final Setting<Boolean> antiCiv = new Setting<>("Anti Civ", true).addToGroup(logic);
    private final Setting<Boolean> expand = new Setting<>("Expand", true).addToGroup(logic);
    private final Setting<Boolean> antiTntAura = new Setting<>("Anti TNT", false).addToGroup(logic);
    private final Setting<Boolean> antiAutoAnchor = new Setting<>("Anti Anchor", false).addToGroup(logic);

    private final Setting<SettingGroup> detect = new Setting<>("Detect", new SettingGroup(false, 1)).addToGroup(logic);
    private final Setting<Boolean> onPacket = new Setting<>("On Break Packet", true).addToGroup(detect);
    private final Setting<Boolean> onAttackBlock = new Setting<>("On Attack Block", false).addToGroup(detect);
    private final Setting<Boolean> onBreak = new Setting<>("On Break", true).addToGroup(detect);

    private final List<BlockPos> placePositions = new CopyOnWriteArrayList<>();
    private int tickCounter = 0;

    public Blocker() {
        super("Blocker", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        sendMessage(Formatting.RED + (isRu() ?
                "ВНИМАНИЕ!!! " + Formatting.RESET + "Использование блокера на серверах осуждается игроками, а в некоторых странах карается набутыливанием!" :
                "WARNING!!! " + Formatting.RESET + "The use of blocker on servers is condemned by players, and in some countries is punishable by jail!"
        ));
    }

    @EventHandler
    public void onPostSync(EventPostSync event) {
        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
            return;
        }
        if (tickCounter >= actionInterval.getValue()) {
            tickCounter = 0;
        }

        if (!getBlockResult().found() || placePositions.isEmpty())
            return;

        placePositions.removeIf(b -> PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) > range.getPow2Value());

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos pos = placePositions.stream()
                    .filter(p -> InteractionUtility.canPlaceBlock(p, interact.getValue(), true))
                    .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(p.toCenterPos())))
                    .orElse(null);

            if (pos != null && mc.player.isOnGround() && placeBlock(pos)) {
                blocksPlaced++;
                tickCounter = 0;
                placePositions.remove(pos);
                inactivityTimer.reset();
            } else break;
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPacketReceive(PacketEvent.@NotNull Receive event) {
        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket && onPacket.getValue()) {
            BlockBreakingProgressS2CPacket packet = event.getPacket();
            doLogic(packet.getPos());
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onAttackBlock(EventAttackBlock event) {
        if (!onAttackBlock.getValue()) return;
        doLogic(event.getBlockPos());
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onBreak(EventBreakBlock event) {
        if (!onBreak.getValue()) return;
        doLogic(event.getPos());
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPlaceBlock(@NotNull EventPlaceBlock event) {
        if (event.getBlockPos().equals(mc.player.getBlockPos().up(2))
                && event.getBlock().equals(Blocks.TNT)
                && antiTntAura.getValue()) {
            placePositions.add(event.getBlockPos());
        }
        if (event.getBlockPos().equals(mc.player.getBlockPos().up(2))
                && event.getBlock().equals(Blocks.RESPAWN_ANCHOR)
                && antiAutoAnchor.getValue()) {
            placePositions.add(event.getBlockPos());
        }
    }

    private void doLogic(BlockPos pos) {
        if (mc.world == null || mc.player == null || !HoleUtility.isHole(mc.player.getBlockPos()))
            return;

        if (antiCev.getValue()) {
            for (BlockPos checkPos : HoleUtility.getHolePoses(mc.player.getPos())) {
                if (pos.equals(checkPos.up(2))) {
                    placePositions.add(checkPos.up(3));
                    return;
                }
            }
        }

        if (HoleUtility.getSurroundPoses(mc.player.getPos()).contains(pos)) {
            if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(pos).isReplaceable())
                return;

            placePositions.add(pos.up());

            if (expand.getValue()) {
                for (Vec3i vec : HoleUtility.VECTOR_PATTERN) {
                    BlockPos checkPos = pos.add(vec);
                    if (canPlaceBlock(checkPos, true)) {
                        if (mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(checkPos)).isEmpty())
                            placePositions.add(checkPos);
                    }
                }
            }

            return;
        }

        if (antiCiv.getValue()) {
            for (BlockPos checkPos : HoleUtility.getSurroundPoses(mc.player.getPos())) {
                if (pos.equals(checkPos.up())) {
                    placePositions.add(checkPos.up(2));
                    return;
                }
            }
        }
    }
}

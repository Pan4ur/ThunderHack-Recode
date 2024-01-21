package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.CombatManager;
import thunder.hack.events.impl.EventBreakBlock;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.base.IndestructibleModule;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.client.MainSettings.isRu;

public final class Breaker extends IndestructibleModule {
    private final Setting<Boolean> autoDisable = new Setting<>("Auto Disable", false);
    private final Setting<Boolean> oldVers = new Setting<>("1.12 Mode", false);

    private final Setting<Parent> logic = new Setting<>("Logic", new Parent(false, 0));
    private final Setting<CombatManager.TargetBy> targetLogic = new Setting<>("Target By", CombatManager.TargetBy.Distance).withParent(logic);
    private final Setting<Boolean> antiBurrow = new Setting<>("Burrow", false).withParent(logic);
    private final Setting<Boolean> antiSurround = new Setting<>("Surround", true).withParent(logic);
    private final Setting<Boolean> antiCev = new Setting<>("Cev", true).withParent(logic);
    private final Setting<Boolean> antiCiv = new Setting<>("Civ", true).withParent(logic);

    private final Setting<BooleanParent> autoPlace = new Setting<>("Auto Place", new BooleanParent(true));
    private final Setting<Boolean> placeCiv = new Setting<>("Place Civ", false).withParent(autoPlace);
    private final Setting<Boolean> placeCev = new Setting<>("Place Cev", true).withParent(autoPlace);

    @SuppressWarnings("unused")
    private final Setting<Parent> blocks = new Setting<>("Blocks", new Parent(false, 0), v -> false);

    private static Breaker instance;
    private BlockPos blockPos;
    private PlayerEntity target;

    public Breaker() {
        super("Breaker", Category.COMBAT);
        instance = this;
    }

    @Override
    public void onEnable() {
        blockPos = null;
        target = null;
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onBreakBlock(@NotNull EventBreakBlock event) {
        if (!event.getPos().equals(blockPos)) return;
        if (autoDisable.getValue())
            disable(isRu() ? "Сарраунд пробит! Отключаю..." : "Surround has been broken! Turning off...");
        blockPos = null;
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onSync(EventSync event) {
        if (mc.world == null || mc.player == null) return;

        if (target == null || target.isDead() || !target.isInRange(mc.player, range.getValue() + 1)) {
            target = ThunderHack.combatManager.getTarget(range.getValue() + 1, targetLogic.getValue());
            return;
        }
        BlockPos targetPos = BlockPos.ofFloored(target.getPos());
        if (!HoleUtility.isHole(targetPos)) {
            target = null;
            return;
        }

        boolean found = false;
        BlockPos minePos = null;

        // 1 прогон - на буров
        if (antiBurrow.getValue() && !mc.world.getBlockState(targetPos).isReplaceable()) {
            minePos = targetPos;
            found = true;
        }

        // 2 прогон - ищем на которые можно поставить
        if (antiSurround.getValue() && !found) {
            for (BlockPos offset : HoleUtility.getSurroundPoses(target.getPos())) {
                if (mc.world.getBlockState(offset).getBlock() != Blocks.OBSIDIAN)
                    continue;
                if (!mc.world.isAir(offset.add(0, 1, 0)))
                    continue;
                if (mc.player.squaredDistanceTo(offset.toCenterPos()) < range.getPow2Value()) {
                    minePos = offset;
                    found = true;
                    break;
                }
            }

            if (!found
                    && HoleUtility.getHolePoses(target.getPos()).size() == 1
                    && !mc.world.getBlockState(targetPos.down()).isReplaceable()
                    && mc.world.getBlockState(targetPos.down(2)).isAir()) {
                minePos = targetPos.down(2);
                found = true;
            }
        }

        // 3 прогон кив брейкаем
        if (!found && antiCiv.getValue())
            for (BlockPos pos : HoleUtility.getSurroundPoses(target.getPos()).stream()
                    .map(BlockPos::up)
                    .toList()) {
                if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) || !mc.world.isAir(pos)) continue;
                if (mc.player.squaredDistanceTo(pos.toCenterPos()) < range.getPow2Value()) {
                    minePos = pos;
                    found = true;
                    break;
                }
            }

        // 4 прогон кев брейкаем
        if (!found && antiCev.getValue()) {
            for (BlockPos pos : HoleUtility.getHolePoses(target.getPos()).stream()
                    .map(pos -> pos.up(2))
                    .toList()) {
                if (mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN)
                        && mc.world.getBlockState(pos.up()).isAir()) {
                    minePos = pos;
                    found = true;
                    break;
                }
            }
        }

        // 5 прогон - ищем любой
        if (!found && !oldVers.getValue() && antiSurround.getValue())
            for (BlockPos pos : HoleUtility.getSurroundPoses(target.getPos())) {
                if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN)
                    continue;
                if (mc.player.squaredDistanceTo(pos.toCenterPos()) < range.getPow2Value()) {
                    minePos = pos;
                    break;
                }
            }

        if (minePos == null) return;

        if (rotate.getValue()) {
            float[] rotation = getRotations(minePos);
            if (rotation != null) {
                mc.player.setYaw(rotation[0]);
                mc.player.setPitch(rotation[1]);
            }
        }

        if (SpeedMine.getInstance().isEnabled())
            SpeedMine.getInstance().addBlockToMine(minePos, null, false);

        if (swing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
        blockPos = minePos;
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPostSync(EventPostSync event) {
        if (mc.world == null
                || blockPos == null
                || target == null
                || autoPlace.getValue().isEnabled())
            return;
        final List<BlockPos> poses = new ArrayList<>();
        final List<BlockPos> prePoses = new ArrayList<>();

        if (placeCiv.getValue()) {
            for (BlockPos posToPlace : HoleUtility.getSurroundPoses(target.getPos()).stream()
                    .map(BlockPos::up)
                    .toList()) {
                if (mc.world.getBlockState(posToPlace).isReplaceable() &&
                        mc.world.getBlockState(posToPlace.up()).isReplaceable()) {
                    poses.add(posToPlace);
                    break;
                }
            }
        }
        if (placeCev.getValue() && poses.isEmpty()) {
            for (BlockPos posToPlace : HoleUtility.getHolePoses(target.getPos()).stream()
                    .map(pos -> pos.up(2))
                    .toList()) {
                if (mc.world.getBlockState(posToPlace).isReplaceable()
                        && mc.world.getBlockState(posToPlace.up()).isReplaceable()
                        && InteractionUtility.canPlaceBlock(posToPlace, interact.getValue(), true)) {
                    prePoses.add(posToPlace);
                    break;
                }
            }
            if (prePoses.isEmpty()) {
                for (BlockPos posToPlace : HoleUtility.getSurroundPoses(target.getPos()).stream()
                        .map(pos -> pos.up(2))
                        .toList()) {
                    if (mc.world.getBlockState(posToPlace).isReplaceable()
                            && InteractionUtility.canPlaceBlock(posToPlace, interact.getValue(), true)) {
                        prePoses.add(posToPlace);
                        break;
                    }
                }
            }
            if (prePoses.isEmpty()) {
                for (BlockPos posToPlace : HoleUtility.getSurroundPoses(target.getPos()).stream()
                        .map(BlockPos::up)
                        .toList()) {
                    if (mc.world.getBlockState(posToPlace).isReplaceable()
                            && InteractionUtility.canPlaceBlock(posToPlace, interact.getValue(), true)) {
                        prePoses.add(posToPlace);
                        break;
                    }
                }
            }
            poses.addAll(prePoses);
        }

        poses.forEach(pos -> {
            if (!placeBlock(pos)) return;
            inactivityTimer.reset();
        });
    }

    public static float @Nullable [] getRotations(@NotNull BlockPos blockPos) {
        InteractionUtility.BreakData data = InteractionUtility.getBreakData(blockPos, InteractionUtility.Interact.Strict);
        return data == null ? null : InteractionUtility.calculateAngle(data.vector());
    }

    public static Breaker getInstance() {
        return instance;
    }
}

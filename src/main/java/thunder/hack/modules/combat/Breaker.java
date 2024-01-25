package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.CombatManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.ExplosionUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;
import java.util.List;

public final class Breaker extends Module {
    private final Setting<Integer> range = new Setting<>("Range", 5, 1, 7);
    private final Setting<ReMine> reMine = new Setting<>("Allow Re Mine", ReMine.Breaker);
    private final Setting<Float> targetDamage = new Setting<>("Min Target Damage", 7f, 0f, 36f);
    private final Setting<Float> selfDamage = new Setting<>("Max Self Damage", 4f, 0f, 36f);

    private BlockPos blockPos;

    public Breaker() {
        super("Breaker", Category.COMBAT);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPostSync(EventSync event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        if (SpeedMine.minePosition != blockPos && reMine.getValue() != ReMine.Self) {
            return;
        }

        ThunderHack.asyncManager.run(() -> {
            if (blockPos != null
                    && !mc.world.getBlockState(blockPos).isAir()
                    && isDamagesCorrect(blockPos)) {
                return;
            }

            PlayerEntity target = ThunderHack.combatManager.getTarget(range.getValue() + 1, CombatManager.TargetBy.FOV);
            if (target == null) {
                return;
            }

            for (BlockPos pos : getOffsets(target)) {
                if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.getPow2Value()
                        || !isDamagesCorrect(pos)) {
                    continue;
                }

                boolean canReMine;
                switch (reMine.getValue()) {
                    case Self -> canReMine = true;
                    case Breaker, Both -> canReMine = SpeedMine.minePosition == blockPos;
                    default -> canReMine = false;
                }

                SpeedMine.getInstance().addBlockToMine(pos, null, canReMine);
                blockPos = pos;
                break;
            }
        });
    }

    private boolean isDamagesCorrect(final BlockPos check) {
        if (mc.world == null) {
            return false;
        }

        final BlockState currentState = mc.world.getBlockState(check);
        mc.world.removeBlock(check, false);
        final float currentDamage = ExplosionUtility.getExplosionDamage1(check.toCenterPos(), mc.player);
        mc.world.setBlockState(check, currentState);

        return SpeedMine.getInstance().checkWorth(targetDamage.getValue(), check)
                && currentDamage >= selfDamage.getValue();
    }

    private @NotNull List<BlockPos> getOffsets(final @NotNull PlayerEntity player) {
        final List<BlockPos> blocks = new ArrayList<>();
        final Vec3d pos = player.getPos();

        blocks.addAll(HoleUtility.getHolePoses(pos).stream().map(BlockPos::down).toList());
        blocks.addAll(HoleUtility.getSurroundPoses(pos));
        blocks.addAll(HoleUtility.getSurroundPoses(pos).stream().map(BlockPos::up).toList());
        blocks.addAll(HoleUtility.getHolePoses(pos).stream().map(bp -> bp.up(2)).toList());

        return blocks;
    }

    private enum ReMine {
        Self,
        Breaker,
        Both,
        None
    }
}

package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.player.CombatManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.world.ExplosionUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;
import java.util.Comparator;

public final class Breaker extends Module {
    private final Setting<Target> targetMode = new Setting<>("Target", Target.AutoCrystal);
    private final Setting<Boolean> onlyIfHole = new Setting<>("OnlyIfHole", false);
    private final Setting<CombatManager.TargetBy> targetBy = new Setting<>("TargetBy", CombatManager.TargetBy.Distance, v -> targetMode.is(Target.Breaker));
    private final Setting<Integer> range = new Setting<>("Range", 5, 1, 7, v -> targetMode.is(Target.Breaker));
    private final Setting<Float> minDamage = new Setting<>("MinDamage", 7f, 0f, 36f);
    private final Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 4f, 0f, 36f);
    private final Setting<Boolean> cevPriority = new Setting<>("CevPriority", true);
    private final Setting<Boolean> antiShulker = new Setting<>("AntiShulker", true);

    private enum Target {
        AutoCrystal, Breaker
    }

    private BlockPos blockPos;

    private final Timer pause = new Timer();

    public Breaker() {
        super("Breaker", Category.COMBAT);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onSync(EventSync event) {
        PlayerEntity target;

        if (targetMode.is(Target.Breaker))
            target = Managers.COMBAT.getTarget(range.getValue(), targetBy.getValue());
        else
            target = AutoCrystal.target;

        if (target == null)
            return;

        BlockPos burrow = BlockPos.ofFloored(target.getPos());
        BlockState burrowState = mc.world.getBlockState(burrow);

        if (!pause.passedMs(600))
            return;

        if (blockPos != null) {
            if (mc.world.isAir(blockPos) || mc.player.squaredDistanceTo(blockPos.toCenterPos()) > (ModuleManager.speedMine.isEnabled() ? ModuleManager.speedMine.range.getPow2Value() : ModuleManager.reach.isEnabled() ? ModuleManager.reach.blocksRange.getPow2Value() : 9)) {
                blockPos = null;
                return;
            }

            if (ModuleManager.speedMine.isEnabled()) {
                //   if (SpeedMine.minePosition == blockPos || (SpeedMine.minePosition != null && !mc.world.isAir(SpeedMine.minePosition)))
                //                    return;

                if (ModuleManager.speedMine.alreadyActing(blockPos))
                    return;

                for (SpeedMine.MineAction action : ModuleManager.speedMine.actions)
                    if (action.instantBreaking())
                        return;

                mc.interactionManager.attackBlock(blockPos, Direction.UP);
            } else mc.interactionManager.updateBlockBreakingProgress(blockPos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        ArrayList<BreakData> list = new ArrayList<>();

        if (cevPriority.getValue()) {
            for (int y = 2; y <= 3; y++) {
                BlockPos bp = BlockPos.ofFloored(target.getX(), target.getY() + y, target.getZ());
                if (mc.world.getBlockState(bp).getBlock() == Blocks.OBSIDIAN
                        && !bp.equals(BlockPos.ofFloored(target.getPos()).down())) {
                    if (ModuleManager.autoCrystal.getInteractResult(bp, new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ())) == null)
                        continue;
                    BlockState currentState = mc.world.getBlockState(bp);
                    mc.world.setBlockState(bp, Blocks.AIR.getDefaultState());
                    float damage = ExplosionUtility.getExplosionDamage(bp.toCenterPos().add(0, -0.5, 0), target, false);
                    float selfDamage = ExplosionUtility.getExplosionDamage(bp.toCenterPos().add(0, -0.5, 0), mc.player, false);
                    mc.world.setBlockState(bp, currentState);
                    if ((Float.isNaN(ModuleManager.autoCrystal.renderDamage) || ModuleManager.autoCrystal.renderDamage < damage) && selfDamage < maxSelfDamage.getValue() && damage >= minDamage.getValue())
                        list.add(new BreakData(bp, damage));
                }
            }

            BreakData best = list.stream().max(Comparator.comparing(BreakData::damage)).orElse(null);

            if (best != null && cevPriority.getValue())
                list.add(new BreakData(best.blockPos, 999));
        }

        boolean inBurrow = burrowState.getBlock() == Blocks.OBSIDIAN || burrowState.getBlock() == Blocks.ENDER_CHEST;

        if (inBurrow) {
            list.add(new BreakData(burrow, 995));
            mc.world.setBlockState(burrow, Blocks.AIR.getDefaultState());
        } else if (onlyIfHole.getValue() && !HoleUtility.isHole(BlockPos.ofFloored(target.getPos())))
            return;

        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (y > 1 && (x == -2 || z == -2 || x == 2 || z == 2))
                        continue;
                    BlockPos bp = BlockPos.ofFloored(target.getX() + x, target.getY() + y, target.getZ() + z);

                    if (mc.world.getBlockState(bp).getBlock() instanceof ShulkerBoxBlock && antiShulker.getValue())
                        list.add(new BreakData(burrow, 990));

                    if ((mc.world.getBlockState(bp).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(bp).getBlock() == Blocks.ENDER_CHEST)
                            && (mc.world.getBlockState(bp.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(bp.down()).getBlock() == Blocks.BEDROCK)
                            && !bp.equals(BlockPos.ofFloored(target.getPos()).down())
                    ) {
                        if (ModuleManager.autoCrystal.getInteractResult(bp, new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ())) == null)
                            continue;

                        BlockState currentState = mc.world.getBlockState(bp);
                        mc.world.setBlockState(bp, Blocks.AIR.getDefaultState());
                        float damage = ExplosionUtility.getExplosionDamage(bp.toCenterPos().add(0, -0.5, 0), target, false);
                        float selfDamage = ExplosionUtility.getExplosionDamage(bp.toCenterPos().add(0, -0.5, 0), mc.player, false);
                        mc.world.setBlockState(bp, currentState);

                        if (ModuleManager.autoCrystal.renderDamage < damage && selfDamage <= maxSelfDamage.getValue() && damage >= minDamage.getValue() && bp != blockPos)
                            list.add(new BreakData(bp, damage));
                    }
                }
            }
        }

        if (inBurrow)
            mc.world.setBlockState(burrow, burrowState);

        BreakData best = list.stream().max(Comparator.comparing(BreakData::damage)).orElse(null);
        BreakData secondBest = ModuleManager.speedMine.doubleMine.getValue() ? list.stream().sorted(Comparator.comparing(BreakData::damage).reversed()).skip(1).findFirst().orElse(null) : null;

        blockPos = best == null ? null : (!ModuleManager.speedMine.alreadyActing(best.blockPos()) || secondBest == null ? best.blockPos() : secondBest.blockPos());
    }

    public void pause() {
        pause.reset();
    }

    private record BreakData(BlockPos blockPos, float damage) {
    }
}

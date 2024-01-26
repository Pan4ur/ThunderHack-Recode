package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.CombatManager;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.ExplosionUtility;

import java.util.ArrayList;
import java.util.Comparator;

public final class Breaker extends Module {
    private final Setting<CombatManager.TargetBy> targetBy = new Setting<>("TargetBy", CombatManager.TargetBy.Distance);
    private final Setting<Integer> range = new Setting<>("Range", 5, 1, 7);
    private final Setting<Float> minDamage = new Setting<>("MinDamage", 7f, 0f, 36f);
    private final Setting<Float> maxSelfDamage = new Setting<>("MaxSelfDamage", 4f, 0f, 36f);
    private final Setting<Boolean> cevPriority = new Setting<>("CevPriority", true);

    private BlockPos blockPos;

    public Breaker() {
        super("Breaker", Category.COMBAT);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPostSync(EventSync event) {
        if (fullNullCheck())
            return;

        PlayerEntity target = ThunderHack.combatManager.getTarget(range.getValue(), targetBy.getValue());

        if (target == null)
            return;


        if (blockPos != null) {
            if (ModuleManager.speedMine.isEnabled()) {
                if (SpeedMine.minePosition == blockPos || SpeedMine.progress != 0)
                    return;
                mc.interactionManager.attackBlock(blockPos, Direction.UP);
            } else mc.interactionManager.updateBlockBreakingProgress(blockPos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        ArrayList<BreakData> list = new ArrayList<>();

        if (cevPriority.getValue()) {
            for (int y = 0; y <= 4; y++) {
                BlockPos bp = BlockPos.ofFloored(target.getX(), target.getY() + y, target.getZ());
                if (mc.world.getBlockState(bp).getBlock() == Blocks.OBSIDIAN && mc.world.isAir(bp.up()) && !bp.equals(BlockPos.ofFloored(target.getPos()).down())) {
                    boolean canPlaceOn = ModuleManager.autoCrystal.getInteractResult(bp, new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ())) != null;
                    BlockState currentState = mc.world.getBlockState(bp);
                    mc.world.removeBlock(bp, false);
                    float damage = ExplosionUtility.getExplosionDamage1(bp.toCenterPos(), target);
                    float selfDamage = ExplosionUtility.getExplosionDamage1(bp.toCenterPos(), mc.player);
                    mc.world.setBlockState(bp, currentState);

                    if (ModuleManager.autoCrystal.renderDamage < damage && selfDamage < maxSelfDamage.getValue() && damage >= minDamage.getValue() && canPlaceOn)
                        list.add(new BreakData(bp, damage));
                }
            }
            BreakData best = list.stream().max(Comparator.comparing(BreakData::damage)).orElse(null);
            if(best != null) {
                blockPos = best.blockPos();
                return;
            }
        }

        ThunderHack.asyncManager.run(() -> {
            for (int x = -3; x <= 3; x++) {
                for (int y = 0; y <= 4; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos bp = BlockPos.ofFloored(target.getX() + x, target.getY() + y, target.getZ() + z);
                        if (mc.world.getBlockState(bp).getBlock() == Blocks.OBSIDIAN && mc.world.isAir(bp.up()) && !bp.equals(BlockPos.ofFloored(target.getPos()).down())) {
                            boolean canPlaceOn = ModuleManager.autoCrystal.getInteractResult(bp, new Vec3d(0.5f + bp.getX(), 1f + bp.getY(), 0.5f + bp.getZ())) != null;
                            BlockState currentState = mc.world.getBlockState(bp);
                            mc.world.removeBlock(bp, false);
                            float damage = ExplosionUtility.getExplosionDamage1(bp.toCenterPos(), target);
                            float selfDamage = ExplosionUtility.getExplosionDamage1(bp.toCenterPos(), mc.player);
                            mc.world.setBlockState(bp, currentState);

                            if (ModuleManager.autoCrystal.renderDamage < damage && selfDamage < maxSelfDamage.getValue() && damage >= minDamage.getValue() && canPlaceOn)
                                list.add(new BreakData(bp, damage));
                        }
                    }
                }
            }

            BreakData best = list.stream().max(Comparator.comparing(BreakData::damage)).orElse(null);
            blockPos = best == null ? null : best.blockPos();
        });
    }



    private record BreakData(BlockPos blockPos, float damage) {
    }
}

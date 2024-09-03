package thunder.hack.utility.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.mutable.MutableInt;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.injection.accesors.IExplosion;
import thunder.hack.utility.math.PredictUtility;

import java.util.Objects;

import static thunder.hack.features.modules.Module.mc;

public final class ExplosionUtility {

    public static boolean terrainIgnore = false;
    public static Explosion explosion;

    /**
     * Calculate target damage based on crystal position and target. Uses AutoCrystal settings so use only in AutoCrystal
     *
     * @param crystalPos the position of the crystal whose damage is to be calculated
     * @param target     the damage will be calculated on this entity
     * @param optimized  use light calculate
     * @return damage value in Float format
     */
    public static float getAutoCrystalDamage(Vec3d crystalPos, PlayerEntity target, int predictTicks, boolean optimized) {
        if (predictTicks == 0) return getExplosionDamage(crystalPos, target, optimized);
        else
            return getExplosionDamageWPredict(crystalPos, target, PredictUtility.predictBox(target, predictTicks), optimized);
    }

    /**
     * Calculate self damage based on crystal position and self extrapolation (predict).
     *
     * @param explosionPos the position of the explosion whose damage is to be calculated
     * @param predictTicks the number of game ticks for which the player's position should be predicted
     * @param optimized    use light calculate
     * @return damage value in Float format
     */
    public static float getSelfExplosionDamage(Vec3d explosionPos, int predictTicks, boolean optimized) {
        return getAutoCrystalDamage(explosionPos, mc.player, predictTicks, optimized);
    }

    /**
     * Calculate target damage based on crystal position and target.
     *
     * @param explosionPos the position of the explosion whose damage is to be calculated
     * @param target       the damage will be calculated on this entity
     * @param optimized    use light calculate
     * @return damage value in Float format
     */
    public static float getExplosionDamage(Vec3d explosionPos, PlayerEntity target, boolean optimized) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL || target == null) return 0f;

        if (explosion == null)
            explosion = new Explosion(mc.world, mc.player, 1f, 33f, 7f, 6f, false, Explosion.DestructionType.DESTROY);

        ((IExplosion) explosion).setX(explosionPos.x);
        ((IExplosion) explosion).setY(explosionPos.y);
        ((IExplosion) explosion).setZ(explosionPos.z);

        if (((IExplosion) explosion).getWorld() != mc.world) ((IExplosion) explosion).setWorld(mc.world);

        if (!new Box(MathHelper.floor(explosionPos.x - 11), MathHelper.floor(explosionPos.y - 11), MathHelper.floor(explosionPos.z - 11), MathHelper.floor(explosionPos.x + 13), MathHelper.floor(explosionPos.y + 13), MathHelper.floor(explosionPos.z + 13)).intersects(target.getBoundingBox()))
            return 0f;

        if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
            double distExposure = (float) target.squaredDistanceTo(explosionPos) / 144.;
            if (distExposure <= 1.0) {
                terrainIgnore = ModuleManager.autoCrystal.ignoreTerrain.getValue();
                double exposure = getExposure(explosionPos, target.getBoundingBox(), optimized);
                terrainIgnore = false;
                double finalExposure = (1.0 - distExposure) * exposure;

                float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2. * 7. * 12. + 1.);

                if (mc.world.getDifficulty() == Difficulty.EASY) toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                else if (mc.world.getDifficulty() == Difficulty.HARD) toDamage = toDamage * 3f / 2f;

                toDamage = DamageUtil.getDamageLeft(target, toDamage, ((IExplosion) explosion).getDamageSource(), target.getArmor(), (float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

                if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                    int resistance = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                    float resistance_1 = toDamage * resistance;
                    toDamage = Math.max(resistance_1 / 25f, 0f);
                }

                if (toDamage <= 0f) toDamage = 0f;
                else {
                    float protAmount = ModuleManager.autoCrystal.assumeBestArmor.getValue() ? 32f : getProtectionAmount(target.getArmorItems());

                    if (protAmount > 0)
                        toDamage = DamageUtil.getInflictedDamage(toDamage, protAmount);
                }
                return toDamage;
            }
        }
        return 0f;
    }

    /**
     * Calculate target damage based on crystal position, target and predicted copy of target.
     *
     * @param explosionPos the position of the explosion whose damage is to be calculated
     * @param target       the damage will be calculated on this entity
     * @param predict      predicted copy of target
     * @return damage value in Float format
     */
    public static float getExplosionDamageWPredict(Vec3d explosionPos, PlayerEntity target, Box predict, boolean optimized) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL) return 0f;

        if (target == null || predict == null) return 0f;

        if (explosion == null)
            explosion = new Explosion(mc.world, mc.player, 1f, 33f, 7f, 6f, false, Explosion.DestructionType.DESTROY);

        ((IExplosion) explosion).setX(explosionPos.x);
        ((IExplosion) explosion).setY(explosionPos.y);
        ((IExplosion) explosion).setZ(explosionPos.z);

        if (((IExplosion) explosion).getWorld() != mc.world) ((IExplosion) explosion).setWorld(mc.world);

        if (!new Box(MathHelper.floor(explosionPos.x - 11d), MathHelper.floor(explosionPos.y - 11d), MathHelper.floor(explosionPos.z - 11d), MathHelper.floor(explosionPos.x + 13d), MathHelper.floor(explosionPos.y + 13d), MathHelper.floor(explosionPos.z + 13d)).intersects(predict))
            return 0f;

        if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
            double distExposure = predict.getCenter().add(0, -0.9, 0).squaredDistanceTo(explosionPos) / 144.;
            if (distExposure <= 1.0) {
                terrainIgnore = ModuleManager.autoCrystal.ignoreTerrain.getValue();
                double exposure = getExposure(explosionPos, predict, optimized);
                terrainIgnore = false;
                double finalExposure = (1.0 - distExposure) * exposure;

                float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12d + 1.0);

                if (mc.world.getDifficulty() == Difficulty.EASY) toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                else if (mc.world.getDifficulty() == Difficulty.HARD) toDamage = toDamage * 3f / 2f;

                toDamage = DamageUtil.getDamageLeft(target, toDamage, ((IExplosion) explosion).getDamageSource(), target.getArmor(), (float) Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue());

                if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                    int resistance = 25 - (Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                    float resistance_1 = toDamage * resistance;
                    toDamage = Math.max(resistance_1 / 25f, 0f);
                }

                if (toDamage <= 0f) toDamage = 0f;
                else {
                    float protAmount = ModuleManager.autoCrystal.assumeBestArmor.getValue() ? 32f : getProtectionAmount(target.getArmorItems());

                    if (protAmount > 0) toDamage = DamageUtil.getInflictedDamage(toDamage, protAmount);
                }
                return toDamage;
            }
        }
        return 0f;
    }

    /**
     * Returns the BlockHitResult of the block without considering other blocks
     *
     * @param context context with point of player's eyes and final aiming point
     * @param block   position of block
     * @return BlockHitResult
     */
    public static BlockHitResult rayCastBlock(RaycastContext context, BlockPos block) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
            BlockState blockState;

            if (!blockPos.equals(block)) blockState = Blocks.AIR.getDefaultState();
            else blockState = Blocks.OBSIDIAN.getDefaultState();

            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();
            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, mc.world, blockPos);
            BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);

            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());

            return d <= e ? blockHitResult : blockHitResult2;
        }, (raycastContext) -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed(raycastContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(raycastContext.getEnd()));
        });
    }

    /**
     * Calculate target damage based on explosion position, target and blockpos which, regardless of state, will be counted as obsidian
     *
     * @param explosionPos the position of the explosion whose damage is to be calculated
     * @param target       the damage will be calculated on this entity
     * @param bp           blockpos which, regardless of state, will be counted as obsidian
     * @return damage value in Float format
     */
    public static float getDamageOfGhostBlock(Vec3d explosionPos, PlayerEntity target, BlockPos bp) {

        if (mc.world.getDifficulty() == Difficulty.PEACEFUL) return 0f;

        if (explosion == null)
            explosion = new Explosion(mc.world, mc.player, 1f, 33f, 7f, 6f, false, Explosion.DestructionType.DESTROY);

        ((IExplosion) explosion).setX(explosionPos.x);
        ((IExplosion) explosion).setY(explosionPos.y);
        ((IExplosion) explosion).setZ(explosionPos.z);

        if (((IExplosion) explosion).getWorld() != mc.world) ((IExplosion) explosion).setWorld(mc.world);

        double maxDist = 12;
        if (!new Box(MathHelper.floor(explosionPos.x - maxDist - 1.0), MathHelper.floor(explosionPos.y - maxDist - 1.0), MathHelper.floor(explosionPos.z - maxDist - 1.0), MathHelper.floor(explosionPos.x + maxDist + 1.0), MathHelper.floor(explosionPos.y + maxDist + 1.0), MathHelper.floor(explosionPos.z + maxDist + 1.0)).intersects(target.getBoundingBox())) {
            return 0f;
        }

        if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
            double distExposure = target.squaredDistanceTo(explosionPos) / 144.;
            if (distExposure <= 1.0) {
                terrainIgnore = ModuleManager.autoCrystal.ignoreTerrain.getValue();
                double exposure = getExposureGhost(explosionPos, target, bp);
                terrainIgnore = false;
                double finalExposure = (1.0 - distExposure) * exposure;

                float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * maxDist + 1.0);

                if (mc.world.getDifficulty() == Difficulty.EASY) {
                    toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                    toDamage = toDamage * 3f / 2f;
                }

                toDamage = DamageUtil.getDamageLeft(target, toDamage, ((IExplosion) explosion).getDamageSource(), target.getArmor(), (float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

                if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                    int resistance = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                    float resistance_1 = toDamage * resistance;
                    toDamage = Math.max(resistance_1 / 25f, 0f);
                }

                if (toDamage <= 0f) toDamage = 0f;
                else {
                    float protAmount = ModuleManager.autoCrystal.assumeBestArmor.getValue() ? 32f : getProtectionAmount(target.getArmorItems());

                    if (protAmount > 0) toDamage = DamageUtil.getInflictedDamage(toDamage, protAmount);
                }
                return toDamage;
            }
        }
        return 0f;
    }

    private static float getExposureGhost(Vec3d source, Entity entity, BlockPos pos) {
        Box box = entity.getBoundingBox();
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;

        if (d < 0.0 || e < 0.0 || f < 0.0) {
            return 0.0f;
        }

        int i = 0;
        int j = 0;

        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = MathHelper.lerp(k, box.minX, box.maxX);
                    double o = MathHelper.lerp(l, box.minY, box.maxY);
                    double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                    Vec3d vec3d = new Vec3d(n + g, o, p + h);
                    if (raycastGhost(new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity), pos).getType() == HitResult.Type.MISS)
                        ++i;
                    ++j;
                }
            }
        }

        return (float) i / (float) j;
    }

    public static float getExposure(Vec3d source, Box box, boolean optimized) {
        if (!optimized) return getExposure(source, box);

        int miss = 0;
        int hit = 0;

        for (int k = 0; k <= 1; k += 1) {
            for (int l = 0; l <= 1; l += 1) {
                for (int m = 0; m <= 1; m += 1) {
                    double n = MathHelper.lerp(k, box.minX, box.maxX);
                    double o = MathHelper.lerp(l, box.minY, box.maxY);
                    double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                    Vec3d vec3d = new Vec3d(n, o, p);
                    if (raycast(vec3d, source, ModuleManager.autoCrystal.ignoreTerrain.getValue()) == HitResult.Type.MISS)
                        ++miss;
                    ++hit;
                }
            }
        }
        return (float) miss / (float) hit;
    }

    public static float getExposure(Vec3d source, Box box) {
        double d = 0.4545454446934474;
        double e = 0.21739130885479366;
        double f = 0.4545454446934474;

        int i = 0;
        int j = 0;

        for (double k = 0.0; k <= 1.0; k += d)
            for (double l = 0.0; l <= 1.0; l += e)
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = MathHelper.lerp(k, box.minX, box.maxX);
                    double o = MathHelper.lerp(l, box.minY, box.maxY);
                    double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                    Vec3d vec3d = new Vec3d(n + 0.045454555306552624, o, p + 0.045454555306552624);
                    if (raycast(vec3d, source, ModuleManager.autoCrystal.ignoreTerrain.getValue()) == HitResult.Type.MISS)
                        ++i;
                    ++j;
                }

        return (float) i / (float) j;
    }

    private static BlockHitResult raycastGhost(RaycastContext context, BlockPos bPos) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            Vec3d vec3d = innerContext.getStart();
            Vec3d vec3d2 = innerContext.getEnd();

            BlockState blockState;

            if (!pos.equals(bPos)) blockState = mc.world.getBlockState(bPos);
            else blockState = Blocks.OBSIDIAN.getDefaultState();

            VoxelShape voxelShape = innerContext.getBlockShape(blockState, mc.world, pos);
            BlockHitResult blockHitResult = mc.world.raycastBlock(vec3d, vec3d2, pos, voxelShape, blockState);
            BlockHitResult blockHitResult2 = VoxelShapes.empty().raycast(vec3d, vec3d2, pos);
            double d = blockHitResult == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
            return d <= e ? blockHitResult : blockHitResult2;
        }, innerContext -> {
            Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    public static HitResult.Type raycast(Vec3d start, Vec3d end, boolean ignoreTerrain) {
        return BlockView.raycast(start, end, null, (innerContext, blockPos) -> {
            BlockState blockState = mc.world.getBlockState(blockPos);
            if (blockState.getBlock().getBlastResistance() < 600 && ignoreTerrain) return null;
            BlockHitResult hitResult = blockState.getCollisionShape(mc.world, blockPos).raycast(start, end, blockPos);
            return hitResult == null ? null : hitResult.getType();
        }, (innerContext) -> HitResult.Type.MISS);
    }


    public static int getProtectionAmount(Iterable<ItemStack> equipment) {
        MutableInt mutableInt = new MutableInt();
        equipment.forEach(i -> mutableInt.add(getProtectionAmount(i)));
        return mutableInt.intValue();
    }

    public static int getProtectionAmount(ItemStack stack) {
        int modifierBlast = EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get(), stack);
        int modifier = EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get(), stack);
        return modifierBlast * 2 + modifier;
    }
}
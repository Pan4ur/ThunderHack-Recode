package thunder.hack.utility.math;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;
import thunder.hack.injection.accesors.IExplosion;
import thunder.hack.modules.combat.AutoAnchor;
import thunder.hack.modules.combat.AutoCrystal;

import java.util.Objects;

import static thunder.hack.modules.Module.mc;

public final class ExplosionUtility {
    /*
     *░█▀▀░█░░░█▀▀░█▀█░█▀█░░░█░█░█▀█░░░█▄█░█▀▀░█░█░█
     *░█░░░█░░░█▀▀░█▀█░█░█░░░█░█░█▀▀░░░█░█░█▀▀░▀░▀░▀
     *░▀▀▀░▀▀▀░▀▀▀░▀░▀░▀░▀░░░▀▀▀░▀░░░░░▀░▀░▀▀▀░▀░▀░▀
     */

    public static boolean terrainIgnore = false;
    public static BlockPos anchorIgnore = null;

    public static Explosion explosion;

    public static float getExplosionDamage2(Vec3d crystalPos, PlayerEntity target) {
        try {
            if (AutoCrystal.predictTicks.getValue() == 0) return getExplosionDamage1(crystalPos, target);
            return getExplosionDamageWPredict(crystalPos, target, PredictUtility.predictPlayer(target, AutoCrystal.predictTicks.getValue()));
        } catch (Exception ignored) {
        }
        return 0f;
    }

    public synchronized static float getAnchorExplosionDamage(BlockPos anchorPos, PlayerEntity target) {
        float final_result;
        anchorIgnore = anchorPos;
        terrainIgnore = true;

        if (AutoAnchor.predictTicks.getValue() == 0)
            final_result = getExplosionDamage1(anchorPos.up().toCenterPos(), target);
        else
            final_result = getExplosionDamageWPredict(anchorPos.toCenterPos(), target, PredictUtility.predictPlayer(target, AutoAnchor.predictTicks.getValue()));

        anchorIgnore = null;
        terrainIgnore = false;
        return final_result;
    }

    public static float getSelfExplosionDamage(Vec3d explosionPos, int predictTicks) {
        if (predictTicks == 0)
            return getExplosionDamage1(explosionPos, mc.player);
        else
            return getExplosionDamageWPredict(explosionPos, mc.player, PredictUtility.predictPlayer(mc.player, predictTicks));
    }

    public static float getExplosionDamage1(Vec3d explosionPos, PlayerEntity target) {
        try {
            if (mc.world.getDifficulty() == Difficulty.PEACEFUL)
                return 0f;

            if (explosion == null) {
                explosion = new Explosion(mc.world, mc.player, 1f, 33f, 7f, 6f, false, Explosion.DestructionType.DESTROY);
            }

            ((IExplosion) explosion).setX(explosionPos.x);
            ((IExplosion) explosion).setY(explosionPos.y);
            ((IExplosion) explosion).setZ(explosionPos.z);

            if (((IExplosion) explosion).getWorld() != mc.world)
                ((IExplosion) explosion).setWorld(mc.world);

            double maxDist = 12;
            if (!new Box(MathHelper.floor(explosionPos.x - maxDist - 1.0), MathHelper.floor(explosionPos.y - maxDist - 1.0), MathHelper.floor(explosionPos.z - maxDist - 1.0), MathHelper.floor(explosionPos.x + maxDist + 1.0), MathHelper.floor(explosionPos.y + maxDist + 1.0), MathHelper.floor(explosionPos.z + maxDist + 1.0)).intersects(target.getBoundingBox())) {
                return 0f;
            }

            if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
                double distExposure = (float) target.squaredDistanceTo(explosionPos) / 144;
                if (distExposure <= 1.0) {
                    double xDiff = target.getX() - explosionPos.x;
                    double yDiff = target.getY() - explosionPos.y;
                    double zDiff = target.getZ() - explosionPos.z;
                    double diff = MathHelper.sqrt((float) (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                    if (diff != 0.0) {
                        terrainIgnore = true;
                        double exposure = Explosion.getExposure(explosionPos, target);
                        terrainIgnore = false;
                        double finalExposure = (1.0 - distExposure) * exposure;

                        float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * maxDist + 1.0);

                        if (mc.world.getDifficulty() == Difficulty.EASY) {
                            toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                        } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                            toDamage = toDamage * 3f / 2f;
                        }

                        toDamage = DamageUtil.getDamageLeft(toDamage, target.getArmor(), (float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

                        if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                            int resistance = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                            float resistance_1 = toDamage * resistance;
                            toDamage = Math.max(resistance_1 / 25f, 0f);
                        }

                        if (toDamage <= 0f) toDamage = 0f;
                        else {
                            int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                            if (protAmount > 0) {
                                toDamage = DamageUtil.getInflictedDamage(toDamage, protAmount);
                            }
                        }
                        return toDamage;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return 0f;
    }

    public static float getExplosionDamageWPredict(Vec3d explosionPos, PlayerEntity target, PlayerEntity predict) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL)
            return 0f;

        if (explosion == null) {
            explosion = new Explosion(mc.world, mc.player, 1f, 33f, 7f, 6f, false, Explosion.DestructionType.DESTROY);
        }

        ((IExplosion) explosion).setX(explosionPos.x);
        ((IExplosion) explosion).setY(explosionPos.y);
        ((IExplosion) explosion).setZ(explosionPos.z);

        if (((IExplosion) explosion).getWorld() != mc.world)
            ((IExplosion) explosion).setWorld(mc.world);

        if (!new Box(
                MathHelper.floor(explosionPos.x - 11d),
                MathHelper.floor(explosionPos.y - 11d),
                MathHelper.floor(explosionPos.z - 11d),
                MathHelper.floor(explosionPos.x + 13d),
                MathHelper.floor(explosionPos.y + 13d),
                MathHelper.floor(explosionPos.z + 13d)).intersects(predict.getBoundingBox())
        ) {
            return 0f;
        }

        if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
            double distExposure = MathHelper.sqrt((float) predict.squaredDistanceTo(explosionPos)) / 12d;
            if (distExposure <= 1.0) {
                double xDiff = predict.getX() - explosionPos.x;
                double yDiff = predict.getY() - explosionPos.y;
                double zDiff = predict.getZ() - explosionPos.z;
                double diff = MathHelper.sqrt((float) (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                if (diff != 0.0) {
                    terrainIgnore = true;
                    double exposure = Explosion.getExposure(explosionPos, predict);
                    terrainIgnore = false;
                    double finalExposure = (1.0 - distExposure) * exposure;

                    float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12d + 1.0);

                    if (mc.world.getDifficulty() == Difficulty.EASY) {
                        toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                    } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                        toDamage = toDamage * 3f / 2f;
                    }

                    toDamage = DamageUtil.getDamageLeft(toDamage, target.getArmor(), (float) Objects.requireNonNull(target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)).getValue());

                    if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        int resistance = 25 - (Objects.requireNonNull(target.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier() + 1) * 5;
                        float resistance_1 = toDamage * resistance;
                        toDamage = Math.max(resistance_1 / 25f, 0f);
                    }

                    if (toDamage <= 0f) {
                        toDamage = 0f;
                    } else {
                        int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                        if (protAmount > 0) {
                            toDamage = DamageUtil.getInflictedDamage(toDamage, protAmount);
                        }
                    }
                    return toDamage;
                }
            }
        }
        return 0f;
    }

    public static BlockHitResult rayCastBlock(RaycastContext context, BlockPos block) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (raycastContext, blockPos) -> {
            BlockState blockState;
            if (!blockPos.equals(block)) {
                blockState = Blocks.AIR.getDefaultState();
            } else {
                blockState = Blocks.OBSIDIAN.getDefaultState();
            }
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

    public static float getDamageOfGhostBlock(Vec3d explosionPos, PlayerEntity target, BlockPos bp) {
        try {
            if (mc.world.getDifficulty() == Difficulty.PEACEFUL)
                return 0f;

            if (explosion == null) {
                explosion = new Explosion(mc.world, mc.player, 1f, 33f, 7f, 6f, false, Explosion.DestructionType.DESTROY);
            }

            ((IExplosion) explosion).setX(explosionPos.x);
            ((IExplosion) explosion).setY(explosionPos.y);
            ((IExplosion) explosion).setZ(explosionPos.z);

            if (((IExplosion) explosion).getWorld() != mc.world)
                ((IExplosion) explosion).setWorld(mc.world);

            double maxDist = 12;
            if (!new Box(MathHelper.floor(explosionPos.x - maxDist - 1.0), MathHelper.floor(explosionPos.y - maxDist - 1.0), MathHelper.floor(explosionPos.z - maxDist - 1.0), MathHelper.floor(explosionPos.x + maxDist + 1.0), MathHelper.floor(explosionPos.y + maxDist + 1.0), MathHelper.floor(explosionPos.z + maxDist + 1.0)).intersects(target.getBoundingBox())) {
                return 0f;
            }

            if (!target.isImmuneToExplosion(explosion) && !target.isInvulnerable()) {
                double distExposure = MathHelper.sqrt((float) target.squaredDistanceTo(explosionPos)) / maxDist;
                if (distExposure <= 1.0) {
                    double xDiff = target.getX() - explosionPos.x;
                    double yDiff = target.getY() - explosionPos.y;
                    double zDiff = target.getZ() - explosionPos.z;
                    double diff = MathHelper.sqrt((float) (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                    if (diff != 0.0) {
                        terrainIgnore = true;
                        double exposure = getExposureGhost(explosionPos, target, bp);
                        terrainIgnore = false;
                        double finalExposure = (1.0 - distExposure) * exposure;

                        float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * maxDist + 1.0);

                        if (mc.world.getDifficulty() == Difficulty.EASY) {
                            toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                        } else if (mc.world.getDifficulty() == Difficulty.HARD) {
                            toDamage = toDamage * 3f / 2f;
                        }

                        toDamage = DamageUtil.getDamageLeft(toDamage, target.getArmor(), (float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

                        if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
                            int resistance = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                            float resistance_1 = toDamage * resistance;
                            toDamage = Math.max(resistance_1 / 25f, 0f);
                        }

                        if (toDamage <= 0f) toDamage = 0f;
                        else {
                            int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(explosion));
                            if (protAmount > 0) {
                                toDamage = DamageUtil.getInflictedDamage(toDamage, protAmount);
                            }
                        }
                        return toDamage;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return 0f;
    }

    public static float getExposureGhost(Vec3d source, Entity entity, BlockPos pos) {
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
                    if (raycastGhost(new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity), pos).getType() == HitResult.Type.MISS) {
                        ++i;
                    }
                    ++j;
                }
            }
        }
        return (float) i / (float) j;
    }

    public static BlockHitResult raycastGhost(RaycastContext context, BlockPos bPos) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            Vec3d vec3d = innerContext.getStart();
            Vec3d vec3d2 = innerContext.getEnd();

            BlockState blockState;
            if (!pos.equals(bPos)) {
                blockState = mc.world.getBlockState(bPos);
            } else {
                blockState = Blocks.OBSIDIAN.getDefaultState();
            }

            VoxelShape voxelShape = innerContext.getBlockShape(blockState, mc.world, (BlockPos) pos);
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

}
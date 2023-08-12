package thunder.hack.utility.math;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.AirBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.explosion.Explosion;
import thunder.hack.modules.combat.AutoAnchor;
import thunder.hack.modules.combat.AutoCrystal;


import java.util.Objects;
import java.util.UUID;

import static thunder.hack.modules.Module.mc;


public final class ExplosionUtility {
    public static boolean terrainIgnore = false;
    public static BlockPos anchorIgnore = null;

    public static float getExplosionDamage2(Vec3d crysPos, PlayerEntity target) {
        if(AutoCrystal.predictTicks.getValue() == 0){
            return getExplosionDamage1(crysPos, target);
        }

        PlayerEntity copyEntity = new PlayerEntity(mc.world, target.getBlockPos(), target.getYaw(), new GameProfile(UUID.fromString("66123666-6666-6666-6666-667563866600"), "PredictEntity228")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        copyEntity.setPosition(getEntityPosVec(target,AutoCrystal.predictTicks.getValue()));
        return getExplosionDamageWPredict(crysPos, target,copyEntity);
    }

    public static float getAnchorExplosionDamage(BlockPos anchorPos, PlayerEntity target) {
        float final_result;
        anchorIgnore = anchorPos;
        terrainIgnore = true;
        if(AutoAnchor.predictTicks.getValue() == 0) {
            final_result = getExplosionDamage1(anchorPos.up().toCenterPos(), target);
        } else {
            PlayerEntity copyEntity = new PlayerEntity(mc.world, target.getBlockPos(), target.getYaw(), new GameProfile(UUID.fromString("66123666-6666-6666-6666-667563866600"), "PredictEntity228")) {
                @Override public boolean isSpectator() {
                    return false;
                }
                @Override public boolean isCreative() {
                    return false;
                }
            };
            copyEntity.setPosition(getEntityPosVec(target, AutoAnchor.predictTicks.getValue()));
            final_result = getExplosionDamageWPredict(anchorPos.toCenterPos(), target, copyEntity);
        }
        anchorIgnore = null;
        terrainIgnore = false;
        return final_result;
    }


    public static float getSelfExplosionDamage(Vec3d explosionPos) {
        return getExplosionDamage1(explosionPos,mc.player);
    }


    public static Vec3d getEntityPosVec(PlayerEntity entity, int ticks) {
        return entity.getPos().add(getMotionVec(entity, ticks));
    }

    public static Vec3d getMotionVec(PlayerEntity entity, int ticks) {
        double dX = entity.getX() - entity.prevX;
        double dZ = entity.getZ() - entity.prevZ;
        double entityMotionPosX = 0;
        double entityMotionPosZ = 0;
        for (int i = 1; i <= ticks; i++) {
            if (mc.world.getBlockState( BlockPos.ofFloored(entity.getX() + dX * i, entity.getY(), entity.getZ() + dZ * i)).getBlock() instanceof AirBlock) {
                entityMotionPosX = dX * i;
                entityMotionPosZ = dZ * i;
            } else {
                break;
            }
        }
        return new Vec3d(entityMotionPosX, 0, entityMotionPosZ);
    }

    public static float getExplosionDamage1(Vec3d explosionPos, PlayerEntity target) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL) return 0f;

        Explosion explosion = new Explosion(mc.world, null, explosionPos.x, explosionPos.y, explosionPos.z, 6f, false, Explosion.DestructionType.DESTROY);

        double maxDist = 12;
        if (!new Box(MathHelper.floor(explosionPos.x - maxDist - 1.0), MathHelper.floor(explosionPos.y - maxDist - 1.0), MathHelper.floor(explosionPos.z - maxDist - 1.0), MathHelper.floor(explosionPos.x + maxDist + 1.0), MathHelper.floor(explosionPos.y + maxDist + 1.0), MathHelper.floor(explosionPos.z + maxDist + 1.0)).intersects(target.getBoundingBox())) {
            return 0f;
        }

        if (!target.isImmuneToExplosion() && !target.isInvulnerable()) {
            double distExposure = MathHelper.sqrt((float) target.squaredDistanceTo(explosionPos)) / maxDist;
            if (distExposure <= 1.0) {
                double xDiff = target.getX() - explosionPos.x;
                double yDiff = target.getY() - explosionPos.y;
                double zDiff = target.getX() - explosionPos.z;
                double diff = MathHelper.sqrt((float) (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                if (diff != 0.0) {
                    if (AutoCrystal.terrainIgnore.getValue()) terrainIgnore = true;
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

                    if (toDamage <= 0f) {
                        toDamage = 0f;
                    } else {
                        int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), explosion.getDamageSource());
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

    public static float getExplosionDamageWPredict(Vec3d explosionPos, PlayerEntity target,PlayerEntity predict) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL) return 0f;

        Explosion explosion = new Explosion(mc.world, null, explosionPos.x, explosionPos.y, explosionPos.z, 6f, false, Explosion.DestructionType.DESTROY);
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

        if (!target.isImmuneToExplosion() && !target.isInvulnerable()) {
            double distExposure = MathHelper.sqrt((float) predict.squaredDistanceTo(explosionPos)) / 12d;
            if (distExposure <= 1.0) {
                double xDiff = predict.getX() - explosionPos.x;
                double yDiff = predict.getY() - explosionPos.y;
                double zDiff = predict.getX() - explosionPos.z;
                double diff = MathHelper.sqrt((float) (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
                if (diff != 0.0) {
                    if (AutoCrystal.terrainIgnore.getValue()) terrainIgnore = true;
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
                        int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), explosion.getDamageSource());
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
}

package thunder.hack.utility.math;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

import static thunder.hack.features.modules.Module.mc;

public class PredictUtility {
    public static PlayerEntity movePlayer(PlayerEntity entity, Vec3d newPos) {
        if (entity == null || newPos == null)
            return null;
        return equipAndReturn(entity, newPos);
    }

    public static PlayerEntity predictPlayer(PlayerEntity entity, int ticks) {
        Vec3d posVec = predictPosition(entity, ticks);
        if (posVec == null)
            return null;
        return equipAndReturn(entity, posVec);
    }

    public static Vec3d predictPosition(PlayerEntity entity, int ticks) {
        if (entity == null)
            return null;

        Vec3d posVec = new Vec3d(entity.getX(), entity.getY(), entity.getZ());

        double motionX = entity.getVelocity().getX();
        double motionZ = entity.getVelocity().getZ();

        for (int i = 0; i < ticks; i++) {
            float hbDeltaX = motionX > 0 ? 0.3f : -0.3f;
            float hbDeltaZ = motionZ > 0 ? 0.3f : -0.3f;

            if (!mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX + hbDeltaX, 0.1, motionZ + hbDeltaZ))) || !mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX + hbDeltaX, 1, motionZ + hbDeltaZ)))) {
                motionX = 0;
                motionZ = 0;
            }
            posVec = posVec.add(motionX, 0, motionZ);
        }

        return posVec;
    }

    public static Box predictBox(PlayerEntity entity, int ticks) {
        Vec3d posVec = predictPosition(entity, ticks);
        if (posVec == null)
            return null;
        return createBox(posVec, entity);
    }

    public static PlayerEntity equipAndReturn(PlayerEntity original, Vec3d posVec) {
        PlayerEntity copyEntity = new PlayerEntity(mc.world, original.getBlockPos(), original.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };

        copyEntity.setPosition(posVec);
        copyEntity.setHealth(original.getHealth());
        copyEntity.prevX = original.prevX;
        copyEntity.prevZ = original.prevZ;
        copyEntity.prevY = original.prevY;
        copyEntity.getInventory().clone(original.getInventory());
        for (StatusEffectInstance se : original.getStatusEffects()) {
            copyEntity.addStatusEffect(se);
        }

        return copyEntity;
    }

    public static Box createBox(Vec3d vec, Entity entity) {
        return entity.getBoundingBox().offset(entity.getPos().relativize(vec));
    }
}

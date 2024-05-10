package thunder.hack.utility.math;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

import static thunder.hack.modules.Module.mc;

public class PredictUtility {
    public static PlayerEntity movePlayer(PlayerEntity entity, Vec3d newPos) {
        if(entity == null || newPos == null)
            return null;
        return equipAndReturn(entity, newPos);
    }

    public static PlayerEntity predictPlayer(PlayerEntity entity, int ticks) {
        if(entity == null)
            return null;

        Vec3d posVec = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        double motionX = entity.getX() - entity.prevX;
        double motionY = entity.getY() - entity.prevY;
        double motionZ = entity.getZ() - entity.prevZ;

        // Можно въебать себя при спрыгивании с блока
        if(entity == mc.player)
            motionY = 0;

        for (int i = 0; i < ticks; i++) {
            if (!mc.world.isAir(BlockPos.ofFloored(posVec.add(0, motionY, 0)))) {
                motionY = 0;
            }
            if (!mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX, 0, 0))) || !mc.world.isAir(BlockPos.ofFloored(posVec.add(motionX, 1, 0)))) {
                motionX = 0;
            }
            if (!mc.world.isAir(BlockPos.ofFloored(posVec.add(0, 0, motionZ))) || !mc.world.isAir(BlockPos.ofFloored(posVec.add(0, 1, motionZ)))) {
                motionZ = 0;
            }
            posVec = posVec.add(motionX, motionY, motionZ);

        }

        return equipAndReturn(entity, posVec);
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
}

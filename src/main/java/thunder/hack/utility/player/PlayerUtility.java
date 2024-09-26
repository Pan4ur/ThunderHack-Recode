package thunder.hack.utility.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.utility.world.ExplosionUtility;

import java.util.Objects;

import static thunder.hack.features.modules.Module.mc;

public final class PlayerUtility {
    public static boolean isInHell() {
        if (mc.world == null) return false;
        return Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "the_nether");
    }

    public static boolean isInEnd() {
        if (mc.world == null) return false;
        return Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "the_end");
    }

    public static boolean isInOver() {
        if (mc.world == null) return false;
        return Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "overworld");
    }

    public static boolean isEating() {
        if (mc.player == null) return false;

        return (mc.player.getMainHandStack().getComponents().contains(DataComponentTypes.FOOD)
                || mc.player.getOffHandStack().getComponents().contains(DataComponentTypes.FOOD))
                && mc.player.isUsingItem();
    }

    public static boolean isMining() {
        if (mc.interactionManager == null) return false;

        return mc.interactionManager.isBreakingBlock();
    }

    public static float squaredDistanceFromEyes(@NotNull Vec3d targetPos) {
        if (mc.player == null) return 0.0f;

        double dx = targetPos.x - mc.player.getX();
        double dy = targetPos.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = targetPos.z - mc.player.getZ();

        return (float) (dx * dx + dy * dy + dz * dz);
    }


    public static float squaredDistance2d(@NotNull Vec2f point) {
        if (mc.player == null) return 0f;

        double d = mc.player.getX() - point.x;
        double f = mc.player.getZ() - point.y;
        return (float) (d * d + f * f);
    }

    public static ClientPlayerEntity getPlayer() {
        return mc.player;
    }

    public static float calculatePercentage(@NotNull ItemStack stack) {
        float durability = stack.getMaxDamage() - stack.getDamage();
        return (durability / (float) stack.getMaxDamage()) * 100F;
    }

    public static float fixAngle(float angle) {
        return Math.round(angle / ((float) (getGCD() * 0.15D))) * (float) (getGCD() * 0.15D);
    }

    public static float getGCD() {
        double sensitivity = mc.options.getMouseSensitivity().getValue();
        double value = sensitivity * 0.6 + 0.2;
        double result = Math.pow(value, 3) * 8.0;

        return (float) result;
    }


    public static float squaredDistance2d(double x, double z) {
        if (mc.player == null) return 0f;

        double d = mc.player.getX() - x;
        double f = mc.player.getZ() - z;
        return (float) (d * d + f * f);
    }

    public static float getSquaredDistance2D(Vec3d vec) {
        double d0 = mc.player.getX() - vec.getX();
        double d2 = mc.player.getZ() - vec.getZ();
        return (float) (d0 * d0 + d2 * d2);
    }

    public static boolean canSee(Vec3d pos) {
        Vec3d vec3d = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
        if (pos.distanceTo(vec3d) > 128.0)
            return false;
        else
            return ExplosionUtility.raycast(vec3d, pos, false) == HitResult.Type.MISS;
    }

    public static boolean isFalling() {
        if (mc.player == null) {
            return false;
        }

        return !mc.player.isOnGround() && !mc.player.isCreative() && mc.player.getVelocity().y < 0;
    }
}

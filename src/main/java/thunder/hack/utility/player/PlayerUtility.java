package thunder.hack.utility.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import thunder.hack.injection.accesors.IClientWorldMixin;
import thunder.hack.utility.math.MathUtility;

import static thunder.hack.modules.Module.mc;

public final class PlayerUtility {
    public static boolean isEating() {
        if (mc.player == null) return false;

        return (mc.player.getMainHandStack().isFood() || mc.player.getOffHandStack().isFood())
                && mc.player.isUsingItem();
    }

    public static boolean isMining() {
        if (mc.interactionManager == null) return false;

        return mc.interactionManager.isBreakingBlock();
    }

    public static float squaredDistanceFromEyes(@NotNull Vec3d vec) {
        if (mc.player == null) return 0;

        double d0 = vec.x - mc.player.getX();
        double d1 = vec.z - mc.player.getZ();
        double d2 = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        return (float) (d0 * d0 + d1 * d1 + d2 * d2);
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

    public static int getWorldActionId(ClientWorld world) {
        PendingUpdateManager pum = getUpdateManager(world);
        int p = pum.getSequence();
        pum.close();
        return p;
    }

    public static float calculatePercentage(@NotNull ItemStack stack) {
        float durability = stack.getMaxDamage() - stack.getDamage();
        return (durability / (float) stack.getMaxDamage()) * 100F;
    }

    private static PendingUpdateManager getUpdateManager(ClientWorld world) {
        return ((IClientWorldMixin) world).acquirePendingUpdateManager();
    }

    public static float fixAngle(float angle) {
        return Math.round(angle / ((float) (getGCD() * 0.15D))) * (float) (getGCD() * 0.15D);
    }

    public static float getGCD() {
        return (float) (Math.pow((float) (mc.options.getMouseSensitivity().getValue() * 0.6D + 0.2D), 3) * 8.0F);
    }

    public static float squaredDistance2d(double x, double z) {
        if (mc.player == null) return 0f;

        double d = mc.player.getX() - x;
        double f = mc.player.getZ() - z;
        return (float) (d * d + f * f);
    }
}

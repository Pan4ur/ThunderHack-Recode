package dev.thunderhack.utils.player;

import dev.thunderhack.mixins.accesors.IClientWorldMixin;
import dev.thunderhack.modules.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public final class PlayerUtility {
    public static boolean isEating() {
        if (Module.mc.player == null) return false;

        return (Module.mc.player.getMainHandStack().isFood() || Module.mc.player.getOffHandStack().isFood())
                && Module.mc.player.isUsingItem();
    }

    public static boolean isMining() {
        if (Module.mc.interactionManager == null) return false;

        return Module.mc.interactionManager.isBreakingBlock();
    }

    public static float squaredDistanceFromEyes(@NotNull Vec3d vec) {
        if (Module.mc.player == null) return 0;

        double d0 = vec.x - Module.mc.player.getX();
        double d1 = vec.z - Module.mc.player.getZ();
        double d2 = vec.y - (Module.mc.player.getY() + Module.mc.player.getEyeHeight(Module.mc.player.getPose()));
        return (float) (d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static float squaredDistance2d(@NotNull Vec2f point) {
        if (Module.mc.player == null) return 0f;

        double d = Module.mc.player.getX() - point.x;
        double f = Module.mc.player.getZ() - point.y;
        return (float) (d * d + f * f);
    }

    public static ClientPlayerEntity getPlayer() {
        return Module.mc.player;
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
}

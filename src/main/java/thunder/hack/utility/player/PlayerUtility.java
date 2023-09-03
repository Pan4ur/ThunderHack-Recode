package thunder.hack.utility.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.NotNull;
import thunder.hack.injection.accesors.IClientWorldMixin;

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

    public static float squaredDistance2d(@NotNull Vec2f point) {
        if (mc.player == null) return 0f;

        double d = mc.player.getX() - point.x;
        double f = mc.player.getZ() - point.y;
        return (float) (d * d + f * f);
    }

    public ClientPlayerEntity getPlayer() {
        return mc.player;
    }

    public static int getWorldActionId(ClientWorld world) {
        PendingUpdateManager pum = getUpdateManager(world);
        int p = pum.getSequence();
        pum.close();
        return p;
    }

    static PendingUpdateManager getUpdateManager(ClientWorld world) {
        return ((IClientWorldMixin) world).acquirePendingUpdateManager();
    }
}

package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class EntitySpeed extends Module {
    private final Setting<Float> speed = new Setting<>("Speed", 10f, 0f, 50f);
    private final Setting<Boolean> onGround = new Setting<>("Only On Ground", false);

    private static final double diagonal = 1 / Math.sqrt(2);
    private static final Vec3d horizontalVelocity = new Vec3d(0, 0, 0);

    public EntitySpeed() {
        super("EntitySpeed", Category.MOVEMENT);
    }

    @Contract("_ -> new")
    private @NotNull Vec3d getHorizontalVelocity(double bps) {
        float yaw = mc.player.getYaw();

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double velX = 0;
        double velZ = 0;

        boolean a = false;
        if (mc.player.input.pressingForward) {
            velX += forward.x / 20 * bps;
            velZ += forward.z / 20 * bps;
            a = true;
        }
        if (mc.player.input.pressingBack) {
            velX -= forward.x / 20 * bps;
            velZ -= forward.z / 20 * bps;
            a = true;
        }

        boolean b = false;
        if (mc.player.input.pressingRight) {
            velX += right.x / 20 * bps;
            velZ += right.z / 20 * bps;
            b = true;
        }
        if (mc.player.input.pressingLeft) {
            velX -= right.x / 20 * bps;
            velZ -= right.z / 20 * bps;
            b = true;
        }

        if (a && b) {
            velX *= diagonal;
            velZ *= diagonal;
        }

        return new Vec3d(velX, horizontalVelocity.y, velZ);
    }
}

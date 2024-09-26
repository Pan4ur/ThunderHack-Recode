package thunder.hack.features.modules.client;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventFixVelocity;
import thunder.hack.events.impl.EventKeyboardInput;
import thunder.hack.events.impl.EventPlayerJump;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;

public class Rotations extends Module {
    public Rotations() {
        super("Rotations", Category.CLIENT);
    }

    private final Setting<MoveFix> moveFix = new Setting<>("MoveFix", MoveFix.Off);
    public final Setting<Boolean> clientLook = new Setting<>("ClientLook", false);

    private enum MoveFix {
        Off, Focused, Free
    }

    public float fixRotation;
    private float prevYaw, prevPitch;

    public void onJump(EventPlayerJump e) {
        if (Float.isNaN(fixRotation) || moveFix.getValue() == MoveFix.Off || mc.player.isRiding())
            return;

        if (e.isPre()) {
            prevYaw = mc.player.getYaw();
            mc.player.setYaw(fixRotation);
        } else mc.player.setYaw(prevYaw);
    }

    public void onPlayerMove(EventFixVelocity event) {
        if (moveFix.getValue() == MoveFix.Free) {
            if (Float.isNaN(fixRotation) || mc.player.isRiding())
                return;
            event.setVelocity(fix(fixRotation, event.getMovementInput(), event.getSpeed()));
        }
    }

    public void modifyVelocity(EventPlayerTravel e) {
        if (ModuleManager.aura.isEnabled() && ModuleManager.aura.target != null && ModuleManager.aura.rotationMode.not(Aura.Mode.None)
                && ModuleManager.aura.elytraTarget.getValue() && Managers.PLAYER.ticksElytraFlying > 5) {
            if (e.isPre()) {
                prevYaw = mc.player.getYaw();
                prevPitch = mc.player.getPitch();

                mc.player.setYaw(fixRotation);
                mc.player.setPitch(ModuleManager.aura.rotationPitch);
            } else {
                mc.player.setYaw(prevYaw);
                mc.player.setPitch(prevPitch);
            }
            return;
        }

        if (moveFix.getValue() == MoveFix.Focused && !Float.isNaN(fixRotation) && !mc.player.isRiding()) {
            if (e.isPre()) {
                prevYaw = mc.player.getYaw();
                mc.player.setYaw(fixRotation);
            } else {
                mc.player.setYaw(prevYaw);
            }
        }
    }

    public void onKeyInput(EventKeyboardInput e) {
        if (moveFix.getValue() == MoveFix.Free) {
            if (Float.isNaN(fixRotation) || mc.player.isRiding())
                return;

            float mF = mc.player.input.movementForward;
            float mS = mc.player.input.movementSideways;
            float delta = (mc.player.getYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
            float cos = MathHelper.cos(delta);
            float sin = MathHelper.sin(delta);
            mc.player.input.movementSideways = Math.round(mS * cos - mF * sin);
            mc.player.input.movementForward = Math.round(mF * cos + mS * sin);
        }
    }

    private Vec3d fix(float yaw, Vec3d movementInput, float speed) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7)
            return Vec3d.ZERO;
        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float f = MathHelper.sin(yaw * MathHelper.RADIANS_PER_DEGREE);
        float g = MathHelper.cos(yaw * MathHelper.RADIANS_PER_DEGREE);
        return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }
}

package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;

public class AntiAim extends Module {
    public AntiAim() {
        super("AntiAim", Category.PLAYER);
    }

    private final Setting<Mode> pitchMode = new Setting<>("PitchMode", Mode.None);
    private final Setting<Mode> yawMode = new Setting<>("YawMode", Mode.None);

    public enum Mode {None, RandomAngle, Spin, Sinus, Fixed, Static, Jitter}


    public Setting<Integer> Speed = new Setting<>("Speed", 1, 1, 45);
    public Setting<Integer> yawDelta = new Setting<>("YawDelta", 60, -360, 360);
    public Setting<Integer> pitchDelta = new Setting<>("PitchDelta", 10, -90, 90);
    public Setting<Integer> yawOffset = new Setting<>("YawOffset", 0, -180, 180);
    public final Setting<Boolean> bodySync = new Setting<>("BodySync", true);
    public final Setting<Boolean> allowInteract = new Setting<>("AllowInteract", true);

    private float rotationYaw, rotationPitch, pitch_sinus_step, yaw_sinus_step;

    @EventHandler(priority = 99)
    public void onSync(EventSync e) {
        if(allowInteract.getValue() && (mc.options.attackKey.isPressed() || mc.options.attackKey.isPressed())) return;
        double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
        if (yawMode.getValue() != Mode.None) {
            mc.player.setYaw((float) (rotationYaw - (rotationYaw - mc.player.getYaw()) % gcdFix));
            if (bodySync.getValue())
                mc.player.setBodyYaw(rotationYaw);
        }
        if (pitchMode.getValue() != Mode.None)
            mc.player.setPitch((float) (rotationPitch - (rotationPitch - mc.player.getPitch()) % gcdFix));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCalc(PlayerUpdateEvent e) {
        if (pitchMode.getValue() == Mode.RandomAngle)
            if (mc.player.age % Speed.getValue() == 0)
                rotationPitch = MathUtility.random(90, -90);

        if (yawMode.getValue() == Mode.RandomAngle)
            if (mc.player.age % Speed.getValue() == 0)
                rotationYaw = MathUtility.random(0, 360);

        if (yawMode.getValue() == Mode.Spin)
            if (mc.player.age % Speed.getValue() == 0) {
                rotationYaw += yawDelta.getValue();
                if (rotationYaw > 360) rotationYaw = 0;
                if (rotationYaw < 0) rotationYaw = 360;
            }

        if (pitchMode.getValue() == Mode.Spin)
            if (mc.player.age % Speed.getValue() == 0) {
                rotationPitch += pitchDelta.getValue();
                if (rotationPitch > 90) rotationPitch = -90;
                if (rotationPitch < -90) rotationPitch = 90;
            }

        if (pitchMode.getValue() == Mode.Sinus) {
            pitch_sinus_step += Speed.getValue() / 10f;
            rotationPitch = (float) (mc.player.getPitch() + pitchDelta.getValue() * Math.sin(pitch_sinus_step));
            rotationPitch = MathUtility.clamp(rotationPitch, -90, 90);
        }

        if (yawMode.getValue() == Mode.Sinus) {
            yaw_sinus_step += Speed.getValue() / 10f;
            rotationYaw = (float) ((mc.player.getYaw() + yawDelta.getValue() * Math.sin(yaw_sinus_step)) + yawOffset.getValue());
        }

        if (pitchMode.getValue() == Mode.Fixed)
            rotationPitch = pitchDelta.getValue();

        if (yawMode.getValue() == Mode.Fixed)
            rotationYaw = yawDelta.getValue();

        if (pitchMode.getValue() == Mode.Static) {
            rotationPitch = mc.player.getPitch() + pitchDelta.getValue();
            rotationPitch = MathUtility.clamp(rotationPitch, -90, 90);
        }
        if (yawMode.getValue() == Mode.Static)
            rotationYaw =  mc.player.getYaw() % 360 + yawDelta.getValue();


        // господи какое говно я написал
        if (pitchMode.getValue() == Mode.Jitter) {

            if (mc.player.age % (Speed.getValue() * 2) == 0) {
                rotationPitch = pitchDelta.getValue() / 2f;
            }

            if (mc.player.age % (Speed.getValue() * 2) == Speed.getValue()) {
                rotationPitch = pitchDelta.getValue() / -2f;
            }
        }

        if (yawMode.getValue() == Mode.Jitter) {

            if (mc.player.age % (Speed.getValue() * 2) == 0) {
                rotationYaw = yawDelta.getValue() / 2f + (float) yawOffset.getValue() + mc.player.getYaw();
            }

            if (mc.player.age % (Speed.getValue() * 2) == Speed.getValue()) {
                rotationYaw = yawDelta.getValue() / -2f + (float) yawOffset.getValue() + mc.player.getYaw();
            }

        }

        ModuleManager.rotations.fixRotation = rotationYaw;
    }
}

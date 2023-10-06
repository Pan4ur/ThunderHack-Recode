package thunder.hack.modules.player;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class ViewLock extends Module {
    public ViewLock() {
        super("ViewLock", Category.PLAYER);
    }

    public Setting<Boolean> pitch = new Setting<>("Pitch", true);
    public Setting<Float> pitchValue = new Setting<>("PitchValue", 0f, -90f, 90f, v -> pitch.getValue());

    public Setting<Boolean> yaw = new Setting<>("Yaw", true);
    public Setting<Float> yawValue = new Setting<>("YawValue", 0f, -180f, 180f, v -> pitch.getValue());

    public void onRender3D(MatrixStack m) {
        if (pitch.getValue()) mc.player.setPitch(pitchValue.getValue());
        if (yaw.getValue()) mc.player.setYaw(yawValue.getValue());
    }
}

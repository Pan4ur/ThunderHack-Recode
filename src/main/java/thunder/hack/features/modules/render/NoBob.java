package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoBob extends Module {
    public NoBob() {
        super("NoBob", Category.RENDER);
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.Sexy);

    public void bobView(MatrixStack matrices, float tickDelta) {
        if (!(mc.getCameraEntity() instanceof PlayerEntity))
            return;

        float g = -(mc.player.horizontalSpeed + (mc.player.horizontalSpeed - mc.player.prevHorizontalSpeed) * tickDelta);
        float h = MathHelper.lerp(tickDelta, mc.player.prevStrideDistance, mc.player.strideDistance);
        matrices.translate(0, -Math.abs(g * h * (mode.is(Mode.Sexy) ? 0.00035 : 0.)), 0);
    }

    public enum Mode {
        Sexy,
        Off
    }
}
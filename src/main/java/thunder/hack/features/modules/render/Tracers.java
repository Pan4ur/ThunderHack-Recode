package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class Tracers extends Module {
    public Tracers() {
        super("Tracers", Category.RENDER);
    }

    private final Setting<Float> height = new Setting<>("Height", 0f, 0f, 2f);

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(0x93FF0000, true)));
    private final Setting<ColorSetting> friendColor = new Setting<>("Friends", new ColorSetting(new Color(0x9317DE5D, true)));

    public void onRender3D(MatrixStack stack) {
        for (PlayerEntity player : Managers.ASYNC.getAsyncPlayers()) {
            if (player == mc.player)
                continue;

            Color color1 = color.getValue().getColorObject();

            if (Managers.FRIEND.isFriend(player))
                color1 = friendColor.getValue().getColorObject();

            double x1 = mc.player.prevX + (mc.player.getX() - mc.player.prevX) * Render3DEngine.getTickDelta();
            double y1 = mc.player.getEyeHeight(mc.player.getPose()) + mc.player.prevY + (mc.player.getY() - mc.player.prevY) * Render3DEngine.getTickDelta();
            double z1 = mc.player.prevZ + (mc.player.getZ() - mc.player.prevZ) * Render3DEngine.getTickDelta();

            Vec3d vec2 = new Vec3d(0, 0, 75)
                    .rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
                    .rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
                    .add(x1, y1, z1);

            double x = player.prevX + (player.getX() - player.prevX) * Render3DEngine.getTickDelta();
            double y = player.prevY + (player.getY() - player.prevY) * Render3DEngine.getTickDelta();
            double z = player.prevZ + (player.getZ() - player.prevZ) * Render3DEngine.getTickDelta();

            Render3DEngine.drawLineDebug(vec2, new Vec3d(x, y + height.getValue(), z), color1);
        }
    }
}

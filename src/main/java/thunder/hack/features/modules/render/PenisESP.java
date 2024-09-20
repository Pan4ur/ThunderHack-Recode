package thunder.hack.features.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.core.Managers.FRIEND;

public class PenisESP extends Module {
    public PenisESP() {
        super("PenisESP", Category.RENDER);
    }

    private final Setting<Boolean> onlyOwn = new Setting<>("OnlyOwn", false);
    private final Setting<Float> ballSize = new Setting<>("BallSize", 0.1f, 0.1f, 0.5f);
    private final Setting<Float> penisSize = new Setting<>("PenisSize", 1.5f, 0.1f, 3.0f);
    private final Setting<Float> friendSize = new Setting<>("FriendSize", 1.5f, 0.1f, 3.0f);
    private final Setting<Float> enemySize = new Setting<>("EnemySize", 0.5f, 0.1f, 3.0f);
    private final Setting<Integer> gradation = new Setting<>("Gradation", 30, 20, 100);
    private final Setting<ColorSetting> penisColor = new Setting<>("PenisColor", new ColorSetting(new Color(231, 180, 122, 255)));
    private final Setting<ColorSetting> headColor = new Setting<>("HeadColor", new ColorSetting(new Color(240, 50, 180, 255)));

    @Override
    public void onRender2D(DrawContext event) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (onlyOwn.getValue() && player != mc.player) continue;
            double size = (FRIEND.isFriend(player) ? friendSize.getValue() : (player != mc.player ? enemySize.getValue() : penisSize.getValue()));

            Vec3d base = getBase(player);
            Vec3d forward = base.add(0, player.getHeight() / 2.4, 0).add(Vec3d.fromPolar(0, player.getYaw()).multiply(0.1));

            Vec3d left = forward.add(Vec3d.fromPolar(0, player.getYaw() - 90).multiply(ballSize.getValue()));
            Vec3d right = forward.add(Vec3d.fromPolar(0, player.getYaw() + 90).multiply(ballSize.getValue()));

            drawBall(player, ballSize.getValue(), gradation.getValue(), left, penisColor.getValue().getColorObject(), 0);
            drawBall(player, ballSize.getValue(), gradation.getValue(), right, penisColor.getValue().getColorObject(), 0);
            drawPenis(player, event.getMatrices(), size, forward);
        }
    }

    public Vec3d getBase(Entity entity) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * Render3DEngine.getTickDelta());
        double y = entity.prevY + ((entity.getY() - entity.prevY) * Render3DEngine.getTickDelta());
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * Render3DEngine.getTickDelta());

        return new Vec3d(x, y, z);
    }

    public void drawBall(PlayerEntity player, double radius, int gradation, Vec3d pos, Color color, int stage) {
        float alpha, beta;

        for (alpha = 0.0f; alpha < Math.PI; alpha += Math.PI / gradation) {
            for (beta = 0.0f; beta < 2.0 * Math.PI; beta += Math.PI / gradation) {
                double x1 = (float) (pos.getX() + (radius * Math.cos(beta) * Math.sin(alpha)));
                double y1 = (float) (pos.getY() + (radius * Math.sin(beta) * Math.sin(alpha)));
                double z1 = (float) (pos.getZ() + (radius * Math.cos(alpha)));

                double sin = Math.sin(alpha + Math.PI / gradation);
                double x2 = (float) (pos.getX() + (radius * Math.cos(beta) * sin));
                double y2 = (float) (pos.getY() + (radius * Math.sin(beta) * sin));
                double z2 = (float) (pos.getZ() + (radius * Math.cos(alpha + Math.PI / gradation)));

                Vec3d base = getBase(player);
                Vec3d forward = base.add(0, player.getHeight() / 2.4, 0).add(Vec3d.fromPolar(0, player.getYaw()).multiply(0.1));
                Vec3d vec3d = new Vec3d(x1, y1, z1);

                switch (stage) {
                    case 1 -> {
                        if (!vec3d.isInRange(forward, 0.145)) continue;
                    }
                    case 2 -> {
                        double size = (FRIEND.isFriend(player) ? friendSize.getValue() : (player != mc.player ? enemySize.getValue() : penisSize.getValue()));
                        if (vec3d.isInRange(forward, size + 0.095)) continue;
                    }
                }

                Render3DEngine.drawLine(vec3d, new Vec3d(x2, y2, z2), color);
            }
        }
    }

    public void drawPenis(PlayerEntity player, MatrixStack event, double size, Vec3d start) {
        Vec3d copy = start;
        start = start.add(Vec3d.fromPolar(0, player.getYaw()).multiply(0.1));
        Vec3d end = start.add(Vec3d.fromPolar(0, player.getYaw()).multiply(size));

        List<Vec3d> vecs = getVec3ds(start, 0.1);
        vecs.forEach(vec3d -> {
            if (!vec3d.isInRange(copy, 0.145)) return;
            if (vec3d.isInRange(copy, 0.135)) return;
            Vec3d pos = vec3d.add(Vec3d.fromPolar(0, player.getYaw()).multiply(size));
            Render3DEngine.drawLine(vec3d, pos, penisColor.getValue().getColorObject());
        });

        drawBall(player, 0.1, gradation.getValue(), start, penisColor.getValue().getColorObject(), 1);
        drawBall(player, 0.1, gradation.getValue(), end, headColor.getValue().getColorObject(), 2);
    }

    public List<Vec3d> getVec3ds(Vec3d vec3d, double radius) {
        List<Vec3d> vec3ds = new ArrayList<>();
        float alpha, beta;

        for (alpha = 0.0f; alpha < Math.PI; alpha += Math.PI / gradation.getValue()) {
            for (beta = 0.0f; beta < 2.01f * Math.PI; beta += Math.PI / gradation.getValue()) {
                double x1 = (float) (vec3d.getX() + (radius * Math.cos(beta) * Math.sin(alpha)));
                double y1 = (float) (vec3d.getY() + (radius * Math.sin(beta) * Math.sin(alpha)));
                double z1 = (float) (vec3d.getZ() + (radius * Math.cos(alpha)));

                Vec3d vec = new Vec3d(x1, y1, z1);
                vec3ds.add(vec);
            }
        }

        return vec3ds;
    }

}

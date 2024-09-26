package thunder.hack.features.hud.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.Managers;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.MSAAFramebuffer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.Objects;

public class RadarRewrite extends HudElement {
    private final Setting<Boolean> glow = new Setting<>("Glow", false);
    private final Setting<Float> width = new Setting<>("Height", 2.28f, 0.1f, 5f);
    private final Setting<Float> down = new Setting<>("Down", 3.63f, 0.1F, 20.0F);
    private final Setting<Float> tracerWidth = new Setting<>("Width", 0.44F, 0.0F, 8.0F);
    private final Setting<Integer> xOffset = new Setting<>("TracerRadius", 68, 20, 100);
    private final Setting<Integer> pitchLock = new Setting<>("PitchLock", 42, 0, 90);
    private final Setting<triangleModeEn> triangleMode = new Setting<>("TracerCMode", triangleModeEn.Astolfo);
    private final Setting<Float> CRadius = new Setting<>("CompassRadius", 47F, 0.1F, 70.0F);
    private final Setting<ColorSetting> ciColor = new Setting<>("Circle", new ColorSetting(new Color(0xFFFFFF)));
    private final Setting<ColorSetting> colorf = new Setting<>("Friend", new ColorSetting(new Color(0x00E800)));
    private final Setting<ColorSetting> colors = new Setting<>("Tracer", new ColorSetting(new Color(0xFFFF00)));

    public RadarRewrite() {
        super("AkrienRadar", 50, 50);
    }

    public static float getRotations(Entity entity) {
        if (mc.player == null) return 0;
        double x = interp(entity.getPos().x, entity.prevX) - interp(mc.player.getPos().x, mc.player.prevX);
        double z = interp(entity.getPos().z, entity.prevZ) - interp(mc.player.getPos().z, mc.player.prevZ);
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

    public static double interp(double d, double d2) {
        return d2 + (d - d2) * (double) Render3DEngine.getTickDelta();
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        if (fullNullCheck()) return;

        float middleW = mc.getWindow().getScaledWidth() * getX();
        float middleH = mc.getWindow().getScaledHeight() * getY();

        MSAAFramebuffer.use(false, () -> {
            context.getMatrices().push();
            renderCompass(context.getMatrices(), middleW + CRadius.getValue(), middleH + CRadius.getValue());
            context.getMatrices().pop();

            int color = 0;

            context.getMatrices().push();
            context.getMatrices().translate(middleW + CRadius.getValue(), middleH + CRadius.getValue(), 0);
            context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f / Math.abs(90f / MathUtility.clamp(mc.player.getPitch(), pitchLock.getValue(), 90f)) - 102));
            context.getMatrices().translate(-(middleW + CRadius.getValue()), -(middleH + CRadius.getValue()), 0);

            for (PlayerEntity e : Lists.newArrayList(mc.world.getPlayers())) {
                if (e != mc.player) {
                    context.getMatrices().push();
                    float yaw = getRotations(e) - mc.player.getYaw();
                    context.getMatrices().translate(middleW + CRadius.getValue(), middleH + CRadius.getValue(), 0.0F);
                    context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                    context.getMatrices().translate(-(middleW + CRadius.getValue()), -(middleH + CRadius.getValue()), 0.0F);

                    if (Managers.FRIEND.isFriend(e))
                        color = colorf.getValue().getColor();
                    else color = switch (triangleMode.getValue()) {
                        case Custom -> colors.getValue().getColor();
                        case Astolfo -> Render2DEngine.astolfo(false, 1).getRGB();
                    };

                    Render2DEngine.drawTracerPointer(context.getMatrices(), middleW + CRadius.getValue(), middleH - xOffset.getValue() + CRadius.getValue(), width.getValue() * 5F, tracerWidth.getValue(), down.getValue(), true, glow.getValue(), color);

                    context.getMatrices().translate(middleW + CRadius.getValue(), middleH + CRadius.getValue(), 0.0F);
                    context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                    context.getMatrices().translate(-(middleW + CRadius.getValue()), -(middleH + CRadius.getValue()), 0.0F);
                    context.getMatrices().pop();
                }
            }
            context.getMatrices().pop();
        });
        setBounds(getPosX(), getPosY(),(int) (CRadius.getValue() * 2), (int) (CRadius.getValue() * 2));
    }

    public void renderCompass(MatrixStack matrices, float x, float y) {
        float pitchFactor = Math.abs(90f / MathUtility.clamp(mc.player.getPitch(), pitchLock.getValue(), 90f));
        drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, pitchFactor, 1f, -2f, 1f, ciColor.getValue().getColorObject(), false);
        drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, pitchFactor, 1f, 0f, 3f, Color.WHITE, true);
    }

    public void drawEllipsCompas(MatrixStack matrices, int yaw, float x, float y, float x2, float y2, float margin, float width, Color color, boolean Dir) {
        drawElipse(matrices, x, y, x2, y2, 15 + yaw, 75 + yaw, margin, width, color, Dir ? "W" : "");
        drawElipse(matrices, x, y, x2, y2, 105 + yaw, 165 + yaw, margin, width, color, Dir ? "N" : "");
        drawElipse(matrices, x, y, x2, y2, 195 + yaw, 255 + yaw, margin, width, color, Dir ? "E" : "");
        drawElipse(matrices, x, y, x2, y2, 285 + yaw, 345 + yaw, margin, width, color, Dir ? "S" : "");
    }

    public void drawElipse(MatrixStack matrices, float x, float y, float rx, float ry, float start, float end, float margin, float width, Color color, String direction) {
        float sin;
        float cos;
        float endOffset;

        if (start > end) {
            endOffset = end;
            end = start;
            start = endOffset;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        float radius = CRadius.getValue() - margin;

        for (float i = start; i <= end; i += 6) {
            float stage = (i - start) / 360f;
            if (!Objects.equals(direction, ""))
                color = HudEditor.getColor((int) (stage * 500f));

            cos = (float) Math.cos(i * Math.PI / 180);
            sin = (float) Math.sin(i * Math.PI / 180);

            bufferBuilder.vertex((x + cos * (radius / ry)), (y + sin * (radius / rx)), 0f).color(color.getRGB());
            bufferBuilder.vertex((x + cos * ((radius - width) / ry)), (y + sin * ((radius - width) / rx)), 0f).color(color.getRGB());
        }

        Render2DEngine.endBuilding(bufferBuilder);
        RenderSystem.disableBlend();

        if (!Objects.equals(direction, ""))
            FontRenderers.getModulesRenderer().drawString(matrices, direction, (x - 2 + Math.cos((start - 15) * Math.PI / 180) * (radius / ry)), (y - 1 + Math.sin((start - 15) * Math.PI / 180) * (radius / rx)), -1);
    }

    public enum mode2 {
        Custom, Astolfo, TwoColor
    }

    public enum triangleModeEn {
        Custom, Astolfo
    }
}

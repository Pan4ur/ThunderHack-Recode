package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import thunder.hack.Thunderhack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AstolfoAnimation;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class RadarRewrite extends HudElement {

    public static AstolfoAnimation astolfo = new AstolfoAnimation();
    public static Setting<Boolean> glow = new Setting("TracerGlow", false);

    float xOffset2 = 0;
    float yOffset2 = 0;

    public Setting<Boolean> items = new Setting<>("Items", false);
    private final Setting<Float> width = new Setting<>("TracerHeight", 2.28f, 0.1f, 5f);
    private static final Setting<Float> rad22ius = new Setting<>("TracerDown", 3.63f, 0.1F, 20.0F);
    private static final Setting<Float> tracerA = new Setting<>("TracerWidth", 0.44F, 0.0F, 8.0F);
    private final Setting<Integer> xOffset = new Setting<>("TracerRadius", 68, 20, 100);
    private final Setting<Integer> maxup2 = new Setting<>("PitchLock", 42, -90, 90);
    private static final Setting<Integer> glowe = new Setting<>("GlowRadius", 10, 1, 20);
    private static final Setting<Integer> glowa = new Setting<>("GlowAlpha", 170, 0, 255);
    private final Setting<triangleModeEn> triangleMode = new Setting<>("TracerCMode", triangleModeEn.Astolfo);
    private final Setting<mode2> Mode2 = new Setting<>("CircleCMode", mode2.Astolfo);
    private final Setting<Float> CRadius = new Setting<>("CompasRadius", 47F, 0.1F, 70.0F);
    private final Setting<Integer> fsef = new Setting<>("Correct", 12, -90, 90);

    public static final Setting<Integer> colorOffset1 = new Setting("ColorOffset", 10, 1, 20);
    public static final Setting<ColorSetting> cColor2 = new Setting<>("CompassColor2", new ColorSetting(0x2250b4b4));
    private final Setting<ColorSetting> cColor = new Setting<>("CompassColor", new ColorSetting(0x2250b4b4));

    private final Setting<ColorSetting> ciColor = new Setting<>("CircleColor", new ColorSetting(0x2250b4b4));
    private final Setting<ColorSetting> colorf = new Setting<>("FriendColor", new ColorSetting(0x2250b4b4));
    private final Setting<ColorSetting> icolor = new Setting<>("ItemColor", new ColorSetting(0x2250b4b4));
    private final Setting<ColorSetting> colors = new Setting<>("TracerColor", new ColorSetting(0x2250b4b4));

    public RadarRewrite() {
        super("AkrienRadar", "стрелочки", 50, 50);
    }

    public static float clamp2(float num, float min, float max) {
        if (num < min) {
            return min;
        } else {
            return Math.min(num, max);
        }
    }

    public static float getRotations(Entity entity) {
        if (mc.player == null) return 0;
        double x = interp(entity.getPos().x, entity.prevX) - interp(mc.player.getPos().x, mc.player.prevX);
        double z = interp(entity.getPos().z, entity.prevZ) - interp(mc.player.getPos().z, mc.player.prevZ);
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }


    public static double interp(double d, double d2) {
        return d2 + (d - d2) * (double) mc.getTickDelta();
    }


    public boolean isHovering() {
        return normaliseX() > xOffset2 - 50 && normaliseX() < xOffset2 + 50 && normaliseY() > yOffset2 - 50 && normaliseY() < yOffset2 + 50;
    }

    private CopyOnWriteArrayList<Entity> players = new CopyOnWriteArrayList<>();

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;
        players.clear();
        for (Entity ent : mc.world.getEntities()) {
            if (ent instanceof PlayerEntity) {
                players.add(ent);
            }
            if (ent instanceof ItemEntity && items.getValue()) {
                players.add(ent);
            }
        }
        astolfo.update();
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        if (fullNullCheck()) return;

        context.getMatrices().push();
        rendercompass(context.getMatrices());
        context.getMatrices().pop();

        xOffset2 = (mc.getWindow().getScaledWidth() * getX());
        yOffset2 = (mc.getWindow().getScaledHeight() * getY());

        int color = 0;
        switch (triangleMode.getValue()) {
            case Custom:
                color = colors.getValue().getColor();
                break;
            case Astolfo:
                color = Render2DEngine.astolfo(false, 1).getRGB();
                break;
            case Rainbow:
                color = Render2DEngine.rainbow(300, 1, 1).getRGB();
                break;
        }
        float xOffset = mc.getWindow().getScaledWidth() * getX();
        float yOffset = mc.getWindow().getScaledHeight() * getY();

        context.getMatrices().push();
        context.getMatrices().translate(xOffset2, yOffset2, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f / Math.abs(90f / clamp2(mc.player.getPitch(), maxup2.getValue(), 90f)) - 90 - fsef.getValue()));
        context.getMatrices().translate(-xOffset2, -yOffset2, 0);


        for (Entity e : players) {
            if (e != mc.player) {
                context.getMatrices().push();
                float yaw = getRotations(e) - mc.player.getYaw();
                context.getMatrices().translate(xOffset, yOffset, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                context.getMatrices().translate(-xOffset, -yOffset, 0.0F);

                if (Thunderhack.friendManager.isFriend(e.getName().getString())) {
                    drawTracerPointer(context.getMatrices(), xOffset, yOffset - this.xOffset.getValue(), width.getValue() * 5F, colorf.getValue().getColor());
                } else {
                    if (e instanceof ItemEntity) {
                        drawTracerPointer(context.getMatrices(), xOffset, yOffset - this.xOffset.getValue(), width.getValue() * 5F, icolor.getValue().getColor());
                    } else {
                        drawTracerPointer(context.getMatrices(), xOffset, yOffset - this.xOffset.getValue(), width.getValue() * 5F, color);
                    }
                }
                context.getMatrices().translate(xOffset, yOffset, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                context.getMatrices().translate(-xOffset, -yOffset, 0.0F);
                context.getMatrices().pop();
            }
        }
        context.getMatrices().pop();
    }

    public void rendercompass(MatrixStack matrices) {
        float x = mc.getWindow().getScaledWidth() * getX();
        float y = mc.getWindow().getScaledHeight() * getY();

        float nigga = Math.abs(90f / clamp2(mc.player.getPitch(), maxup2.getValue(), 90f));

        if (Mode2.getValue() == mode2.Custom) {
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2.5f, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 3, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 3.5f, ciColor.getValue().getColorObject(), false, Mode2.getValue());

        }
        if (Mode2.getValue() == mode2.Rainbow) {
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2, Render2DEngine.rainbow(300, 1, 1), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2.5f, Render2DEngine.rainbow(300, 1, 1), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 3.0f, Render2DEngine.rainbow(300, 1, 1), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 3.5f, Render2DEngine.rainbow(300, 1, 1), false, Mode2.getValue());

        }
        if (Mode2.getValue() == mode2.Astolfo) {
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2.5f, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 3.0f, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 3.5f, ciColor.getValue().getColorObject(), false, Mode2.getValue());
        }
        if (Mode2.getValue() == mode2.TwoColor) {
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 2.5f, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 3, ciColor.getValue().getColorObject(), false, Mode2.getValue());
            drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue() - 3.5f, ciColor.getValue().getColorObject(), false, Mode2.getValue());

        }
        drawEllipsCompas(matrices, -(int) mc.player.getYaw(), x, y, nigga, 1f, CRadius.getValue(), cColor.getValue().getColorObject(), true, mode2.Custom);
    }

    public static void drawTracerPointer(MatrixStack matrices, float x, float y, float size, int color) {
        if (glow.getValue())
            Render2DEngine.drawBlurredShadow(matrices, x - size * tracerA.getValue(), y, (x + size * tracerA.getValue()) - (x - size * tracerA.getValue()), size, glowe.getValue(), Render2DEngine.injectAlpha(new Color(color), glowa.getValue()));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        matrices.push();

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        Render2DEngine.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (x - size * tracerA.getValue()), (y + size), 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, (y + size - rad22ius.getValue()), 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        Render2DEngine.endRender();

        color = Render2DEngine.darker(new Color(color), 0.8f).getRGB();

        f = (float) (color >> 24 & 255) / 255.0F;
        g = (float) (color >> 16 & 255) / 255.0F;
        h = (float) (color >> 8 & 255) / 255.0F;
        k = (float) (color & 255) / 255.0F;

        Render2DEngine.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, (y + size - rad22ius.getValue()), 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (x + size * tracerA.getValue()), (y + size), 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        Render2DEngine.endRender();

        color = Render2DEngine.darker(new Color(color), 0.6f).getRGB();

        f = (float) (color >> 24 & 255) / 255.0F;
        g = (float) (color >> 16 & 255) / 255.0F;
        h = (float) (color >> 8 & 255) / 255.0F;
        k = (float) (color & 255) / 255.0F;

        Render2DEngine.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (x - size * tracerA.getValue()), (y + size), 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (x + size * tracerA.getValue()), (y + size), 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, (y + size - rad22ius.getValue()), 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (x - size * tracerA.getValue()), (y + size), 0.0F).color(g, h, k, f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        Render2DEngine.endRender();
        matrices.pop();


        // RenderSystem.setShaderColor(1f,1f,1f,1f);
    }

    public static void drawEllipsCompas(MatrixStack matrices, int yaw, float x, float y, float x2, float y2, float radius, Color color, boolean Dir, RadarRewrite.mode2 mode) {
        if (Dir) {
            drawElipse(matrices, x, y, x2, y2, 15 + yaw, 75 + yaw, radius, color, 0, mode);
            drawElipse(matrices, x, y, x2, y2, 105 + yaw, 165 + yaw, radius, color, 1, mode);
            drawElipse(matrices, x, y, x2, y2, 195 + yaw, 255 + yaw, radius, color, 2, mode);
            drawElipse(matrices, x, y, x2, y2, 285 + yaw, 345 + yaw, radius, color, 3, mode);
        } else {
            drawElipse(matrices, x, y, x2, y2, 15 + yaw, 75 + yaw, radius, color, -1, mode);
            drawElipse(matrices, x, y, x2, y2, 105 + yaw, 165 + yaw, radius, color, -1, mode);
            drawElipse(matrices, x, y, x2, y2, 195 + yaw, 255 + yaw, radius, color, -1, mode);
            drawElipse(matrices, x, y, x2, y2, 285 + yaw, 345 + yaw, radius, color, -1, mode);
        }
    }

    public static void drawElipse(MatrixStack matrices, float x, float y, float rx, float ry, float start, float end, float radius, Color color, int stage1, RadarRewrite.mode2 cmode) {
        float sin;
        float cos;
        float i;


        float endOffset;
        if (start > end) {
            endOffset = end;
            end = start;
            start = endOffset;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.lineWidth(100F);

        Render2DEngine.setupRender();
        for (i = start; i <= end; i += 4) {

            double stage = (i - start) / 360;
            Color clr = null;

            if (cmode == RadarRewrite.mode2.Astolfo) {
                clr = new Color(astolfo.getColor(stage));
            } else if (cmode == RadarRewrite.mode2.Rainbow) {
                clr = color;
            } else if (cmode == RadarRewrite.mode2.Custom) {
                clr = color;
            } else {
                clr = Render2DEngine.TwoColoreffect(color, cColor2.getValue().getColorObject(), Math.abs(System.currentTimeMillis() / 10) / 100.0 + i * ((20f - colorOffset1.getValue()) / 200));
            }

            int clr2 = clr.getRGB();
            int red = ((clr2 >> 16) & 255);
            int green = ((clr2 >> 8) & 255);
            int blue = ((clr2 & 255));
            cos = (float) Math.cos(i * Math.PI / 180) * (radius / ry);
            sin = (float) Math.sin(i * Math.PI / 180) * (radius / rx);
            bufferBuilder.vertex((x + cos), (y + sin), 0f).color(red, green, blue, 255).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.lineWidth(1F);
        RenderSystem.disableBlend();
        Render2DEngine.endRender();
        if (stage1 != -1) {
            cos = (float) Math.cos((start - 15) * Math.PI / 180) * (radius / ry);
            sin = (float) Math.sin((start - 15) * Math.PI / 180) * (radius / rx);

            x = x - 2;
            y = y - 1;

            switch (stage1) {
                case 0 -> {
                    // FontRender.drawCentString3("W", (x + cos), (y + sin), -1);
                    FontRenderers.getRenderer2().drawString(matrices, "W", (x + cos), (y + sin), -1);
                }
                case 1 -> {
                    FontRenderers.getRenderer2().drawString(matrices, "N", (x + cos), (y + sin), -1);

                }
                case 2 -> {
                    FontRenderers.getRenderer2().drawString(matrices, "E", (x + cos), (y + sin), -1);

                }
                case 3 -> {
                    FontRenderers.getRenderer2().drawString(matrices, "S", (x + cos), (y + sin), -1);
                }
            }
        }
    }

    public enum mode2 {
        Custom, Rainbow, Astolfo, TwoColor
    }

    public enum triangleModeEn {
        Custom, Astolfo, Rainbow
    }
}

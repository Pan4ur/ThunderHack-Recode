package thunder.hack.gui.hud.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import thunder.hack.ThunderHack;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AstolfoAnimation;

import java.awt.*;

import static thunder.hack.gui.hud.impl.RadarRewrite.getRotations;

public class CrosshairArrows extends HudElement {

    public CrosshairArrows() {
        super("CrosshairArrows", 0, 0);
    }

    public static AstolfoAnimation astolfo = new AstolfoAnimation();

    public static Setting<Boolean> glow = new Setting<>("Glow", false);
    private final Setting<Float> width = new Setting<>("Height", 2.28f, 0.1f, 5f);
    private final Setting<BooleanParent> down = new Setting<>("Down", new BooleanParent(false));
    private final Setting<Float> downHeight = new Setting<>("DownHeight", 3.63f, 0.1F, 20.0F).withParent(down);
    private final Setting<Float> tracerWidth = new Setting<>("Width", 0.44F, 0.0F, 8.0F);
    private final Setting<Integer> xOffset = new Setting<>("TracerRadius", 68, 20, 100);
    private final Setting<Integer> pitchLock = new Setting<>("PitchLock", 42, 0, 90);
    private final Setting<Integer> glowRadius = new Setting<>("GlowRadius", 10, 1, 20);
    private final Setting<Integer> glowAlpha = new Setting<>("GlowAlpha", 170, 0, 255);
    private final Setting<triangleModeEn> triangleMode = new Setting<>("TracerCMode", triangleModeEn.Astolfo);
    private final Setting<ColorSetting> colorf = new Setting<>("Friend", new ColorSetting(new Color(0x00E800)));
    private final Setting<ColorSetting> colors = new Setting<>("Tracer", new ColorSetting(new Color(0xFFFF00)));

    @Override
    public void onUpdate() {
        astolfo.update();
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        if (fullNullCheck()) return;

        float middleW = mc.getWindow().getScaledWidth() * .5f;
        float middleH = mc.getWindow().getScaledHeight() * .5f;

        int color = switch (triangleMode.getValue()) {
            case Custom -> colors.getValue().getColor();
            case Astolfo -> Render2DEngine.astolfo(false, 1).getRGB();
        };

        context.getMatrices().push();
        context.getMatrices().translate(middleW, middleH, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f / Math.abs(90f / MathUtility.clamp(mc.player.getPitch(), pitchLock.getValue(), 90f)) - 102));
        context.getMatrices().translate(-middleW, -middleH, 0);

        for (PlayerEntity e : Lists.newArrayList(mc.world.getPlayers())) {
            if (e != mc.player){
                context.getMatrices().push();
                float yaw = getRotations(e) - mc.player.getYaw();
                context.getMatrices().translate(middleW, middleH, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                context.getMatrices().translate(-middleW, -middleH, 0.0F);

                if (ThunderHack.friendManager.isFriend(e))
                    color = colorf.getValue().getColor();

                drawTracerPointer(context.getMatrices(), middleW, middleH - xOffset.getValue(), width.getValue() * 5F, color);

                context.getMatrices().translate(middleW, middleH, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                context.getMatrices().translate(-middleW, -middleH, 0.0F);
                context.getMatrices().pop();
            }
        }
        context.getMatrices().pop();
    }

    public void drawTracerPointer(MatrixStack matrices, float x, float y, float size, int color) {
        if (glow.getValue())
            Render2DEngine.drawBlurredShadow(matrices, x - size * tracerWidth.getValue(), y, (x + size * tracerWidth.getValue()) - (x - size * tracerWidth.getValue()), size, glowRadius.getValue(), Render2DEngine.injectAlpha(new Color(color), glowAlpha.getValue()));

        matrices.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color).next();
        bufferBuilder.vertex(matrix, (x - size * tracerWidth.getValue()), (y + size), 0.0F).color(color).next();
        bufferBuilder.vertex(matrix, x, (y + size - downHeight.getValue()), 0.0F).color(color).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color).next();
        color = Render2DEngine.darker(new Color(color), 0.8f).getRGB();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color).next();
        bufferBuilder.vertex(matrix, x, (y + size - downHeight.getValue()), 0.0F).color(color).next();
        bufferBuilder.vertex(matrix, (x + size * tracerWidth.getValue()), (y + size), 0.0F).color(color).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color).next();

        if(down.getValue().isEnabled()) {
            color = Render2DEngine.darker(new Color(color), 0.6f).getRGB();
            bufferBuilder.vertex(matrix, (x - size * tracerWidth.getValue()), (y + size), 0.0F).color(color).next();
            bufferBuilder.vertex(matrix, (x + size * tracerWidth.getValue()), (y + size), 0.0F).color(color).next();
            bufferBuilder.vertex(matrix, x, (y + size - downHeight.getValue()), 0.0F).color(color).next();
            bufferBuilder.vertex(matrix, (x - size * tracerWidth.getValue()), (y + size), 0.0F).color(color).next();
        }

        tessellator.draw();

        RenderSystem.disableBlend();
        matrices.pop();
    }

    public enum triangleModeEn {
        Custom, Astolfo
    }
}

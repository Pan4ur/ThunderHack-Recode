package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.render.Particles;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

import static thunder.hack.utility.render.Render2DEngine.*;
import static thunder.hack.utility.render.Render2DEngine.star;

public class Crosshair extends Module {

    public Crosshair() {
        super("Crosshair", Category.HUD);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Circle);
    private final Setting<ColorMode> colorMode = new Setting<>("ColorMode", ColorMode.Sync);
    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4));
    private final Setting<Boolean> dynamic = new Setting<>("Dynamic", true);
    private final Setting<Float> range = new Setting<>("Range", 30.0f, 0.1f, 120f);
    private final Setting<Float> speed = new Setting<>("Speed", 3.0f, 0.1f, 20f);
    private final Setting<Float> backSpeed = new Setting<>("BackSpeed", 5.0f, 0.1f, 20f);

    private enum ColorMode {
        Custom, Sync
    }

    private enum Mode {
        Circle, WiseTree, Dot
    }

    private float xAnim, yAnim, prevPitch, prevProgress;

    public void onRender2D(DrawContext context) {
        if (!mc.options.getPerspective().isFirstPerson()) return;

        float midX = mc.getWindow().getScaledWidth() / 2f;
        float midY = mc.getWindow().getScaledHeight() / 2f;

        float yawDelta = mc.player.prevHeadYaw - mc.player.getHeadYaw();
        float pitchDelta = prevPitch - mc.player.getPitch();


        if (yawDelta > 0) xAnim = AnimationUtility.fast(xAnim, midX - range.getValue(), speed.getValue());
        else if (yawDelta < 0) xAnim = AnimationUtility.fast(xAnim, midX + range.getValue(), speed.getValue());
        else xAnim = AnimationUtility.fast(xAnim, midX, backSpeed.getValue());


        if (pitchDelta > 0) yAnim = AnimationUtility.fast(yAnim, midY - range.getValue(), speed.getValue());
        else if (pitchDelta < 0) yAnim = AnimationUtility.fast(yAnim, midY + range.getValue(), speed.getValue());
        else yAnim = AnimationUtility.fast(yAnim, midY, backSpeed.getValue());

        prevPitch = mc.player.getPitch();

        if (!dynamic.getValue()) {
            xAnim = midX;
            yAnim = midY;
        }

        float progress =  (360f * mc.player.getAttackCooldownProgress(0.5f));
        progress = progress == 0 ? 360f : progress;

        switch (mode.getValue()) {
            case Circle -> {
                Render2DEngine.drawArc(context.getMatrices(), xAnim - 25, yAnim - 25, 50, 50, 0.05f, 0.12f, 0, Render2DEngine.interpolateFloat(prevProgress, progress, mc.getTickDelta()));
                prevProgress = progress;
            }
            case WiseTree -> {
                Color color = this.color.getValue().getColorObject();
                context.getMatrices().push();
                context.getMatrices().translate(xAnim, yAnim, 0);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation((System.currentTimeMillis() % 70000) / 70000f * 360f));
                context.getMatrices().translate(-xAnim, -yAnim, 0);
                Render2DEngine.drawRect(context.getMatrices(), xAnim - 0.75f, yAnim - 5, 1.5f, 10, color);
                Render2DEngine.drawRect(context.getMatrices(), xAnim - 5, yAnim - 0.75f, 10, 1.5f, color);
                Render2DEngine.drawRect(context.getMatrices(), xAnim, yAnim - 5, 5, 1.5f, color);
                Render2DEngine.drawRect(context.getMatrices(), xAnim - 5, yAnim + 4, 5.25f, 1.5f, color);
                Render2DEngine.drawRect(context.getMatrices(), xAnim - 5f, yAnim - 5, 1.5f, 4.25f, color);
                Render2DEngine.drawRect(context.getMatrices(), xAnim + 3.5f, yAnim, 1.5f, 5.5f, color);
                context.getMatrices().pop();
            }
            case Dot -> {
                context.getMatrices().push();
                context.getMatrices().translate(xAnim + 4, yAnim + 4, 0);
                RenderSystem.setShaderTexture(0, firefly);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                RenderSystem.setShaderTexture(0, firefly);
                Color color1 = colorMode.getValue() == ColorMode.Sync ? HudEditor.getColor(1) : color.getValue().getColorObject();
                Matrix4f posMatrix = context.getMatrices().peek().getPositionMatrix();
                bufferBuilder.vertex(posMatrix, 0, -8f, 0).texture(0f, 1f).color(color1.getRGB()).next();
                bufferBuilder.vertex(posMatrix, -8f, -8f, 0).texture(1f, 1f).color(color1.getRGB()).next();
                bufferBuilder.vertex(posMatrix, -8f, 0, 0).texture(1f, 0).color(color1.getRGB()).next();
                bufferBuilder.vertex(posMatrix, 0, 0, 0).texture(0, 0).color(color1.getRGB()).next();
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                RenderSystem.depthMask(true);
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                context.getMatrices().pop();
            }
        }
    }

    public float getAnimatedPosX() {
        if (xAnim == 0)
            return mc.getWindow().getScaledWidth() / 2f;
        return xAnim;
    }

    public float getAnimatedPosY() {
        if (yAnim == 0)
            return mc.getWindow().getScaledHeight() / 2f;
        return yAnim;
    }
}
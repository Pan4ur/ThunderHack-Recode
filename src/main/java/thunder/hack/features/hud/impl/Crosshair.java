package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

public class Crosshair extends Module {
    public Crosshair() {
        super("Crosshair", Category.HUD);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Circle);
    private final Setting<Boolean> animated = new Setting<>("Animated", true, v -> mode.is(Mode.Default));
    private final Setting<Boolean> dot = new Setting<>("Dot", false, v -> mode.is(Mode.Default));
    private final Setting<Boolean> t = new Setting<>("T", false, v -> mode.is(Mode.Default));
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
        Circle, WiseTree, Dot, Default
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

        float progress = (360f * mc.player.getAttackCooldownProgress(0.5f));
        progress = progress == 0 ? 360f : progress;

        switch (mode.getValue()) {
            case Circle -> {
                Color c1 = colorMode.getValue() == ColorMode.Sync ? HudEditor.hcolor1.getValue().getColorObject() : color.getValue().getColorObject();
                Color c2 = colorMode.getValue() == ColorMode.Sync ? HudEditor.acolor.getValue().getColorObject() : color.getValue().getColorObject();

                Render2DEngine.drawArc(context.getMatrices(), xAnim - 25, yAnim - 25, 50, 50, 0.05f, 0.12f, 0,
                        Render2DEngine.interpolateFloat(prevProgress, progress, Render3DEngine.getTickDelta()), c1, c2);
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
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
                BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                RenderSystem.setShaderTexture(0, TextureStorage.firefly);
                Color color1 = colorMode.getValue() == ColorMode.Sync ? HudEditor.getColor(1) : color.getValue().getColorObject();
                Matrix4f posMatrix = context.getMatrices().peek().getPositionMatrix();
                bufferBuilder.vertex(posMatrix, 0, -8f, 0).texture(0f, 1f).color(color1.getRGB());
                bufferBuilder.vertex(posMatrix, -8f, -8f, 0).texture(1f, 1f).color(color1.getRGB());
                bufferBuilder.vertex(posMatrix, -8f, 0, 0).texture(1f, 0).color(color1.getRGB());
                bufferBuilder.vertex(posMatrix, 0, 0, 0).texture(0, 0).color(color1.getRGB());
                BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                context.getMatrices().pop();
            }
            case Default -> {
                Color color = this.color.getValue().getColorObject();

                float offset = animated.getValue() ? -3f + (Render2DEngine.interpolateFloat(prevProgress, progress, Render3DEngine.getTickDelta()) / 100f) : 0;
                prevProgress = progress;

                if (!t.getValue()) {
                    Render2DEngine.drawRect(context.getMatrices(), xAnim - 1, yAnim - 6 + offset, 2, 4, Color.BLACK);
                    Render2DEngine.drawRect(context.getMatrices(), xAnim - 0.5f, yAnim - 5.5f + offset, 1, 3, color);
                }

                Render2DEngine.drawRect(context.getMatrices(), xAnim - 1, yAnim + 2 - offset, 2, 4, Color.BLACK);
                Render2DEngine.drawRect(context.getMatrices(), xAnim - 0.5f, yAnim + 2.5f - offset, 1, 3, color);

                Render2DEngine.drawRect(context.getMatrices(), xAnim - 6 + offset, yAnim - 1, 4, 2, Color.BLACK);
                Render2DEngine.drawRect(context.getMatrices(), xAnim - 5.5f + offset, yAnim - 0.5f, 3, 1, color);

                Render2DEngine.drawRect(context.getMatrices(), xAnim + 2 - offset, yAnim - 1, 4, 2, Color.BLACK);
                Render2DEngine.drawRect(context.getMatrices(), xAnim + 2.5f - offset, yAnim - 0.5f, 3, 1, color);

                if (dot.getValue()) {
                    Render2DEngine.drawRect(context.getMatrices(), xAnim - 1f, yAnim - 1f, 2, 2, Color.BLACK);
                    Render2DEngine.drawRect(context.getMatrices(), xAnim - .5f, yAnim - .5f, 1, 1, color);
                }
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
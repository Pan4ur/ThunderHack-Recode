package thunder.hack.utility.render.animation;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

import static thunder.hack.features.modules.Module.mc;

public class CaptureMark {
    private static float espValue = 1f, prevEspValue;
    private static float espSpeed = 1f;
    private static boolean flipSpeed;

    public static void render(Entity target) {
        Camera camera = mc.gameRenderer.getCamera();

        double tPosX = Render2DEngine.interpolate(target.prevX, target.getX(), Render3DEngine.getTickDelta()) - camera.getPos().x;
        double tPosY = Render2DEngine.interpolate(target.prevY, target.getY(), Render3DEngine.getTickDelta()) - camera.getPos().y;
        double tPosZ = Render2DEngine.interpolate(target.prevZ, target.getZ(), Render3DEngine.getTickDelta()) - camera.getPos().z;

        MatrixStack matrices = new MatrixStack();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(tPosX, (tPosY + target.getEyeHeight(target.getPose()) / 2f), tPosZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(Render2DEngine.interpolateFloat(prevEspValue, espValue, Render3DEngine.getTickDelta())));
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, TextureStorage.capture);
        matrices.translate(-0.75, -0.75, -0.01);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, 0, 1.5f, 0).texture(0f, 1f).color(HudEditor.getColor(90).getRGB());
        buffer.vertex(matrix, 1.5f, 1.5f, 0).texture(1f, 1f).color(HudEditor.getColor(0).getRGB());
        buffer.vertex(matrix, 1.5f, 0, 0).texture(1f, 0).color(HudEditor.getColor(180).getRGB());
        buffer.vertex(matrix, 0, 0, 0).texture(0, 0).color(HudEditor.getColor(270).getRGB());
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void tick() {
        prevEspValue = espValue;
        espValue += espSpeed;
        if (espSpeed > 25) flipSpeed = true;
        if (espSpeed < -25) flipSpeed = false;
        espSpeed = flipSpeed ? espSpeed - 0.5f : espSpeed + 0.5f;
    }
}

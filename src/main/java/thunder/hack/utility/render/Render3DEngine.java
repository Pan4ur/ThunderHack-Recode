package thunder.hack.utility.render;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.HudEditor;

import java.awt.*;
import java.util.ArrayList;

import static com.mojang.blaze3d.systems.RenderSystem.disableBlend;
import static thunder.hack.modules.Module.mc;


public class Render3DEngine {

    public static void drawFilledBox(MatrixStack stack, Box box, Color c) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();


        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB()).next();
        RenderSystem.disableDepthTest();

        tessellator.draw();
        RenderSystem.disableBlend();
        //   cleanup();
    }

    public static Box interpolateBox(Box from, Box to, float delta) {
        double interpX = Render2DEngine.interpolate(from.maxX, to.maxX, delta);
        double interpY = Render2DEngine.interpolate(from.maxY, to.maxY, delta);
        double interpZ = Render2DEngine.interpolate(from.maxZ, to.maxZ, delta);
        double interpX1 = Render2DEngine.interpolate(from.minX, to.minX, delta);
        double interpY1 = Render2DEngine.interpolate(from.minY, to.minY, delta);
        double interpZ1 = Render2DEngine.interpolate(from.minZ, to.minZ, delta);
        return new Box(interpX1, interpY1, interpZ1, interpX, interpY, interpZ);
    }

    public static void drawFilledSide(MatrixStack stack, Box box, Color c, Direction dir) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();


        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        if (dir == Direction.DOWN) {
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB()).next();
        }

        if (dir == Direction.NORTH) {
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB()).next();
        }

        if (dir == Direction.EAST) {
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB()).next();
        }
        if (dir == Direction.SOUTH) {
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB()).next();
        }

        if (dir == Direction.WEST) {
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB()).next();
        }

        if (dir == Direction.UP) {
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB()).next();
        }
        RenderSystem.disableDepthTest();
        tessellator.draw();
        RenderSystem.disableBlend();
        //   cleanup();
    }

    public static void drawTextIn3D(String text, Vec3d pos, double offX, double offY, double textOffset, Color color) {
        RenderSystem.disableDepthTest();
        MatrixStack matrices = new MatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(pos.getX() - camera.getPos().x, pos.getY() - camera.getPos().y, pos.getZ() - camera.getPos().z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.translate(offX, offY - 0.1, -0.01);
        matrices.scale(-0.025f, -0.025f, 0);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        FontRenderers.modules.drawCenteredString(matrices, text, textOffset, 0f, color.getRGB());
        immediate.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }


    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
    }

    public static void drawFilledFadeBox(MatrixStack stack, Box box, Color c, Color c1) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();


        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c1.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c1.getRGB()).next();

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c1.getRGB()).next();
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c1.getRGB()).next();
        RenderSystem.disableDepthTest();

        tessellator.draw();
        RenderSystem.disableBlend();
        //   cleanup();
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {
        setup();

        MatrixStack matrices = matrixFrom(x1, y1, z1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Line
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), color);
        tessellator.draw();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        cleanup();
    }

    public static void drawLine(Vec3d vec1, Vec3d vec2, Color color, float width) {
        setup();

        MatrixStack matrices = matrixFrom(vec1.getX(), vec1.getY(), vec1.getZ());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Line
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (vec2.getX() - vec1.getX()), (float) (vec2.getY() - vec1.getY()), (float) (vec2.getZ() - vec1.getZ()), color);
        tessellator.draw();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        cleanup();
    }

    public static void drawBoxOutline(Box box, Color color, float lineWidth) {
        setup();
        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
        vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
        vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
        vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
        vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);

        tessellator.draw();
        RenderSystem.enableCull();
        cleanup();
    }

    public static void drawSideOutline(Box box, Color color, float lineWidth, Direction dir) {
        setup();
        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;


        // vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
        // vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
        // vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
        // vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);


        if (dir == Direction.DOWN) {
            vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
            vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
            vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
            vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
        }

        if (dir == Direction.NORTH) {
            vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
            vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
            vertexLine(matrices, buffer, x2, y1, z1, x1, y1, z1, color);
            vertexLine(matrices, buffer, x2, y2, z1, x1, y2, z1, color);
        }

        if (dir == Direction.EAST) {
            vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
            vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
            vertexLine(matrices, buffer, x2, y2, z2, x2, y2, z1, color);
            vertexLine(matrices, buffer, x2, y1, z2, x2, y1, z1, color);
        }

        if (dir == Direction.SOUTH) {
            vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
            vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
            vertexLine(matrices, buffer, x1, y1, z2, x2, y1, z2, color);
            vertexLine(matrices, buffer, x1, y2, z2, x2, y2, z2, color);
        }

        if (dir == Direction.WEST) {
            vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
            vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
            vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
            vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
        }

        if (dir == Direction.UP) {
            vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
            vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
            vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
            vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
        }

        tessellator.draw();
        RenderSystem.enableCull();
        cleanup();
    }



    public static void drawBottomOutline(Box box, Color color, float lineWidth) {
        setup();
        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float z2 = (float) box.maxZ;

        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);

        tessellator.draw();
        RenderSystem.enableCull();
        cleanup();
    }

    public static void drawHoleOutline(Box box, Color color, float lineWidth) {
        setup();
        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float y2 = (float) box.maxY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float z2 = (float) box.maxZ;

        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);

        vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
        vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);

        tessellator.draw();
        RenderSystem.enableCull();
        cleanup();
    }


    public static void vertexLine(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, Color lineColor) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();
        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);
        vertexConsumer.vertex(model, x1, y1, z1).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
        vertexConsumer.vertex(model, x2, y2, z2).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
    }


    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static void setup() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void cleanup() {
        disableBlend();
    }

    public static void drawTargetEsp(MatrixStack stack, Entity target) {
        ArrayList<Vec3d> vecs = new ArrayList<>();
        ArrayList<Vec3d> vecs1 = new ArrayList<>();
        ArrayList<Vec3d> vecs2 = new ArrayList<>();

        double x = target.prevX + (target.getX() - target.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = target.prevZ + (target.getZ() - target.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();


        double height = target.getHeight();

        for (int i = 0; i <= 361; ++i) {
            double v = Math.sin(Math.toRadians(i));
            double u = Math.cos(Math.toRadians(i));
            Vec3d vec = new Vec3d((float) (u * 0.5f), height, (float) (v * 0.5f));
            vecs.add(vec);

            double v1 = Math.sin(Math.toRadians((i + 120) % 360));
            double u1 = Math.cos(Math.toRadians(i + 120) % 360);
            Vec3d vec1 = new Vec3d((float) (u1 * 0.5f), height, (float) (v1 * 0.5f));
            vecs1.add(vec1);

            double v2 = Math.sin(Math.toRadians((i + 240) % 360));
            double u2 = Math.cos(Math.toRadians((i + 240) % 360));
            Vec3d vec2 = new Vec3d((float) (u2 * 0.5f), height, (float) (v2 * 0.5f));
            vecs2.add(vec2);
            height -= 0.004f;
        }


        stack.push();
        stack.translate(x, y, z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();


        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int j = 0; j < vecs.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - ThunderHack.initTime) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) vecs.get(j).x, (float) vecs.get(j).y, (float) vecs.get(j).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255)).getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) vecs.get(j + 1).x, (float) vecs.get(j + 1).y + 0.1f, (float) vecs.get(j + 1).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255f)).getRGB()).next();
        }
        tessellator.draw();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int j = 0; j < vecs1.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - ThunderHack.initTime) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) vecs1.get(j).x, (float) vecs1.get(j).y, (float) vecs1.get(j).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255)).getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) vecs1.get(j + 1).x, (float) vecs1.get(j + 1).y + 0.1f, (float) vecs1.get(j + 1).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255f)).getRGB()).next();
        }
        tessellator.draw();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int j = 0; j < vecs2.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - ThunderHack.initTime) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) vecs2.get(j).x, (float) vecs2.get(j).y, (float) vecs2.get(j).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255)).getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) vecs2.get(j + 1).x, (float) vecs2.get(j + 1).y + 0.1f, (float) vecs2.get(j + 1).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255f)).getRGB()).next();
        }
        tessellator.draw();


        RenderSystem.enableCull();
        stack.translate(-x, -y, -z);
        Render3DEngine.cleanup();
        stack.pop();
    }

    public static void renderCrosses(Box box, Color color, float lineWidth) {
        setup();
        MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        vertexLine(matrices, buffer, (float) box.maxX, (float) box.minY, (float) box.minZ, (float) box.minX, (float) box.minY, (float) box.maxZ, color);
        vertexLine(matrices, buffer, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.maxZ, color);

        tessellator.draw();
        RenderSystem.enableCull();
        cleanup();
    }

    public static void drawSphere(MatrixStack matrix, float radius, int slices, int stacks, int color) {
        float drho = 3.1415927F / ((float) stacks);
        float dtheta = 6.2831855F / ((float) slices - 1f);
        float rho;
        float theta;
        float x;
        float y;
        float z;
        int i;
        int j;
        Render3DEngine.setup();
        for (i = 1; i < stacks; ++i) {
            rho = (float) i * drho;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

            for (j = 0; j < slices; ++j) {
                theta = (float) j * dtheta;
                x = (float) (Math.cos(theta) * Math.sin(rho));
                y = (float) (Math.sin(theta) * Math.sin(rho));
                z = (float) Math.cos(rho);
                bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x * radius, y * radius, z * radius).color(color).next();
            }
            tessellator.draw();
        }

        for (j = 0; j < slices; ++j) {
            theta = (float) j * dtheta;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

            for (i = 0; i <= stacks; ++i) {
                rho = (float) i * drho;
                x = (float) (Math.cos(theta) * Math.sin(rho));
                y = (float) (Math.sin(theta) * Math.sin(rho));
                z = (float) Math.cos(rho);
                bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x * radius, y * radius, z * radius).color(color).next();
            }
            tessellator.draw();
        }
        Render3DEngine.cleanup();
    }
}

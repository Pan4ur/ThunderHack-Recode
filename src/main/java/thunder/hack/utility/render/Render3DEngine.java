package thunder.hack.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.ClientSettings;
import thunder.hack.modules.client.HudEditor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.Module.mc;

public class Render3DEngine {

    public static List<FillAction> FILLED_QUEUE = new ArrayList<>();
    public static List<OutlineAction> OUTLINE_QUEUE = new ArrayList<>();
    public static List<FadeAction> FADE_QUEUE = new ArrayList<>();
    public static List<FillSideAction> FILLED_SIDE_QUEUE = new ArrayList<>();
    public static List<OutlineSideAction> OUTLINE_SIDE_QUEUE = new ArrayList<>();
    public static List<DebugLineAction> DEBUG_LINE_QUEUE = new ArrayList<>();
    public static List<LineAction> LINE_QUEUE = new ArrayList<>();

    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    private static float prevCircleStep;
    private static float circleStep;

    public static void onRender3D(MatrixStack stack) {
        if (!FILLED_QUEUE.isEmpty() || !FADE_QUEUE.isEmpty() || !FILLED_SIDE_QUEUE.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();

            RenderSystem.disableDepthTest();
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            FILLED_QUEUE.forEach(action -> setFilledBoxVertexes(bufferBuilder, stack.peek().getPositionMatrix(), action.box(), action.color()));

            FADE_QUEUE.forEach(action -> setFilledFadePoints(action.box(), bufferBuilder, stack.peek().getPositionMatrix(), action.color(), action.color2()));

            FILLED_SIDE_QUEUE.forEach(action -> setFilledSidePoints(bufferBuilder, stack.peek().getPositionMatrix(), action.box, action.color(), action.side()));

            tessellator.draw();
            endRender();
            RenderSystem.enableDepthTest();

            FADE_QUEUE.clear();
            FILLED_SIDE_QUEUE.clear();
            FILLED_QUEUE.clear();
        }

        if (!OUTLINE_QUEUE.isEmpty() || !OUTLINE_SIDE_QUEUE.isEmpty()) {
            setupRender();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

            RenderSystem.lineWidth(2f);

            OUTLINE_QUEUE.forEach(action -> {
                setOutlinePoints(action.box(), matrixFrom(action.box().minX, action.box().minY, action.box().minZ), buffer, action.color());
            });

            OUTLINE_SIDE_QUEUE.forEach(action -> {
                setSideOutlinePoints(action.box, matrixFrom(action.box().minX, action.box().minY, action.box().minZ), buffer, action.color(), action.side());
            });

            tessellator.draw();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            endRender();
            OUTLINE_QUEUE.clear();
            OUTLINE_SIDE_QUEUE.clear();
        }

        if (!DEBUG_LINE_QUEUE.isEmpty()) {
            setupRender();
            RenderSystem.disableDepthTest();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            RenderSystem.lineWidth(1f);
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.LINES);
            DEBUG_LINE_QUEUE.forEach(action -> {
                MatrixStack matrices = matrixFrom(action.start.getX(), action.start.getY(), action.start.getZ());
                vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (action.end.getX() - action.start.getX()), (float) (action.end.getY() - action.start.getY()), (float) (action.end.getZ() - action.start.getZ()), action.color);
            });
            tessellator.draw();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            endRender();
            DEBUG_LINE_QUEUE.clear();
        }

        if (!LINE_QUEUE.isEmpty()) {
            setupRender();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
            RenderSystem.lineWidth(2f);
            RenderSystem.disableDepthTest();
            buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            LINE_QUEUE.forEach(action -> {
                MatrixStack matrices = matrixFrom(action.start.getX(), action.start.getY(), action.start.getZ());
                vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (action.end.getX() - action.start.getX()), (float) (action.end.getY() - action.start.getY()), (float) (action.end.getZ() - action.start.getZ()), action.color);
            });
            tessellator.draw();
            RenderSystem.enableCull();
            RenderSystem.lineWidth(1f);
            RenderSystem.enableDepthTest();
            endRender();
            LINE_QUEUE.clear();
        }
    }

    @Deprecated
    @SuppressWarnings("unused")
    public static void drawFilledBox(MatrixStack stack, Box box, Color c) {
        FILLED_QUEUE.add(new FillAction(box, c));
    }

    public static void setFilledBoxVertexes(@NotNull BufferBuilder bufferBuilder, Matrix4f m, @NotNull Box box, @NotNull Color c) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB()).next();

        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB()).next();

        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB()).next();

        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB()).next();

        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB()).next();

        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB()).next();
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB()).next();
    }

    public static @NotNull Box interpolateBox(@NotNull Box from, @NotNull Box to, float delta) {
        double X = Render2DEngine.interpolate(from.maxX, to.maxX, delta);
        double Y = Render2DEngine.interpolate(from.maxY, to.maxY, delta);
        double Z = Render2DEngine.interpolate(from.maxZ, to.maxZ, delta);
        double X1 = Render2DEngine.interpolate(from.minX, to.minX, delta);
        double Y1 = Render2DEngine.interpolate(from.minY, to.minY, delta);
        double Z1 = Render2DEngine.interpolate(from.minZ, to.minZ, delta);
        return new Box(X1, Y1, Z1, X, Y, Z);
    }

    @Deprecated
    public static void drawFilledSide(MatrixStack stack, @NotNull Box box, Color c, Direction dir) {
        FILLED_SIDE_QUEUE.add(new FillSideAction(box, c, dir));
    }

    public static void setFilledSidePoints(BufferBuilder buffer, Matrix4f matrix, Box box, Color c, Direction dir) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

        if (dir == Direction.DOWN) {
            buffer.vertex(matrix, minX, minY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, minY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, minY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, minX, minY, maxZ).color(c.getRGB()).next();
        }

        if (dir == Direction.NORTH) {
            buffer.vertex(matrix, minX, minY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, minX, maxY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, maxY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, minY, minZ).color(c.getRGB()).next();
        }

        if (dir == Direction.EAST) {
            buffer.vertex(matrix, maxX, minY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, maxY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, minY, maxZ).color(c.getRGB()).next();
        }
        if (dir == Direction.SOUTH) {
            buffer.vertex(matrix, minX, minY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, minY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, minX, maxY, maxZ).color(c.getRGB()).next();
        }

        if (dir == Direction.WEST) {
            buffer.vertex(matrix, minX, minY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, minX, minY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, minX, maxY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, minX, maxY, minZ).color(c.getRGB()).next();
        }

        if (dir == Direction.UP) {
            buffer.vertex(matrix, minX, maxY, minZ).color(c.getRGB()).next();
            buffer.vertex(matrix, minX, maxY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB()).next();
            buffer.vertex(matrix, maxX, maxY, minZ).color(c.getRGB()).next();
        }
    }

    public static void drawTextIn3D(String text, @NotNull Vec3d pos, double offX, double offY, double textOffset, @NotNull Color color) {
        MatrixStack matrices = new MatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(pos.getX() - camera.getPos().x, pos.getY() - camera.getPos().y, pos.getZ() - camera.getPos().z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        setupRender();
        matrices.translate(offX, offY - 0.1, -0.01);
        matrices.scale(-0.025f, -0.025f, 0);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        FontRenderers.modules.drawCenteredString(matrices, text, textOffset, 0f, color.getRGB());
        immediate.draw();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        endRender();
    }

    public static @NotNull Vec3d worldSpaceToScreenSpace(@NotNull Vec3d pos) {
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

        return new Vec3d(target.x / getScaleFactor(), (displayHeight - target.y) / getScaleFactor(), target.z);
    }

    public static double getScaleFactor() {
        return ClientSettings.scaleFactorFix.getValue() ? ClientSettings.scaleFactorFixValue.getValue() : mc.getWindow().getScaleFactor();
    }


    @Deprecated
    @SuppressWarnings("unused")
    public static void drawFilledFadeBox(@NotNull MatrixStack stack, @NotNull Box box, @NotNull Color c, @NotNull Color c1) {
        FADE_QUEUE.add(new FadeAction(box, c, c1));
    }

    public static void setFilledFadePoints(Box box, BufferBuilder buffer, Matrix4f posMatrix, Color c, Color c1) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

        if (ModuleManager.holeESP.culling.getValue())
            RenderSystem.enableCull();

        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB()).next();

        buffer.vertex(posMatrix, maxX, minY, minZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB()).next();

        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, maxX, minY, maxZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB()).next();

        buffer.vertex(posMatrix, minX, minY, minZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, minX, minY, maxZ).color(c.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB()).next();

        buffer.vertex(posMatrix, minX, maxY, minZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, minX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, maxZ).color(c1.getRGB()).next();
        buffer.vertex(posMatrix, maxX, maxY, minZ).color(c1.getRGB()).next();

        if (ModuleManager.holeESP.culling.getValue())
            RenderSystem.disableCull();
    }

    public static void drawLine(@NotNull Vec3d start, @NotNull Vec3d end, @NotNull Color color) {
        LINE_QUEUE.add(new LineAction(start, end, color));
    }

    @Deprecated
    public static void drawBoxOutline(@NotNull Box box, Color color, float lineWidth) {
        OUTLINE_QUEUE.add(new OutlineAction(box, color, lineWidth));
    }

    public static void setOutlinePoints(Box box, MatrixStack matrices, BufferBuilder buffer, Color color) {
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
    }

    @Deprecated
    public static void drawSideOutline(@NotNull Box box, Color color, float lineWidth, Direction dir) {
        OUTLINE_SIDE_QUEUE.add(new OutlineSideAction(box, color, lineWidth, dir));
    }

    public static void setSideOutlinePoints(Box box, MatrixStack matrices, BufferBuilder buffer, Color color, Direction dir) {
        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        switch (dir) {
            case UP -> {
                vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
                vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
            }
            case DOWN -> {
                vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
                vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
                vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
            }
            case EAST -> {
                vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
                vertexLine(matrices, buffer, x2, y2, z2, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y1, z1, color);
            }
            case WEST -> {
                vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
            }
            case NORTH -> {
                vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z1, x1, y1, z1, color);
                vertexLine(matrices, buffer, x2, y2, z1, x1, y2, z1, color);
            }
            case SOUTH -> {
                vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
                vertexLine(matrices, buffer, x1, y1, z2, x2, y1, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x2, y2, z2, color);
            }
        }
    }

    public static void drawHoleOutline(@NotNull Box box, Color color, float lineWidth) {
        setupRender();
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
        endRender();
    }

    public static void vertexLine(@NotNull MatrixStack matrices, @NotNull VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, @NotNull Color lineColor) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        MatrixStack.Entry entry = matrices.peek();
        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);

        //TODO Test
        buffer.vertex(model, x1, y1, z1).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(entry, normalVec.x(), normalVec.y(), normalVec.z()).next();
        buffer.vertex(model, x2, y2, z2).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(entry,normalVec.x(), normalVec.y(), normalVec.z()).next();
    }

    public static @NotNull Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    public static @NotNull MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void endRender() {
        RenderSystem.disableBlend();
    }

    public static void drawTargetEsp(MatrixStack stack, @NotNull Entity target) {
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
        setupRender();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();


        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        Matrix4f matrix = stack.peek().getPositionMatrix();

        for (int j = 0; j < vecs.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - ThunderHack.initTime) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(matrix, (float) vecs.get(j).x, (float) vecs.get(j).y, (float) vecs.get(j).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255)).getRGB()).next();
            bufferBuilder.vertex(matrix, (float) vecs.get(j + 1).x, (float) vecs.get(j + 1).y + 0.1f, (float) vecs.get(j + 1).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255f)).getRGB()).next();
        }
        tessellator.draw();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int j = 0; j < vecs1.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - ThunderHack.initTime) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(matrix, (float) vecs1.get(j).x, (float) vecs1.get(j).y, (float) vecs1.get(j).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255)).getRGB()).next();
            bufferBuilder.vertex(matrix, (float) vecs1.get(j + 1).x, (float) vecs1.get(j + 1).y + 0.1f, (float) vecs1.get(j + 1).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255f)).getRGB()).next();
        }
        tessellator.draw();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int j = 0; j < vecs2.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - ThunderHack.initTime) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(matrix, (float) vecs2.get(j).x, (float) vecs2.get(j).y, (float) vecs2.get(j).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255)).getRGB()).next();
            bufferBuilder.vertex(matrix, (float) vecs2.get(j + 1).x, (float) vecs2.get(j + 1).y + 0.1f, (float) vecs2.get(j + 1).z).color(Render2DEngine.injectAlpha(HudEditor.getColor((int) (j / 20f)), (int) (alpha * 255f)).getRGB()).next();
        }
        tessellator.draw();

        RenderSystem.enableCull();
        stack.translate(-x, -y, -z);
        endRender();
        RenderSystem.enableDepthTest();
        stack.pop();
    }

    public static void renderCrosses(@NotNull Box box, Color color, float lineWidth) {
        setupRender();
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
        endRender();
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
        setupRender();
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
        endRender();
    }

    public static void drawCircle3D(MatrixStack stack, Entity ent, float radius, int color, int points, boolean hudColor, int colorOffset) {
        setupRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        double x = ent.prevX + (ent.getX() - ent.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = ent.prevY + (ent.getY() - ent.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = ent.prevZ + (ent.getZ() - ent.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        stack.push();
        stack.translate(x, y, z);

        Matrix4f matrix = stack.peek().getPositionMatrix();
        for (int i = 0; i <= points; i++) {
            if (hudColor)
                color = HudEditor.getColor(i * colorOffset).getRGB();

            bufferBuilder.vertex(matrix, (float) (radius * Math.cos(i * 6.28 / points)), 0f, (float) (radius * Math.sin(i * 6.28 / points))).color(color).next();
        }

        tessellator.draw();
        endRender();
        stack.translate(-x, -y, -z);
        stack.pop();
    }

    public static void drawOldTargetEsp(MatrixStack stack, Entity target) {
        double cs = prevCircleStep + (circleStep - prevCircleStep) * mc.getTickDelta();
        double prevSinAnim = absSinAnimation(cs - 0.45f);
        double sinAnim = absSinAnimation(cs);
        double x = target.prevX + (target.getX() - target.prevX) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + prevSinAnim * target.getHeight();
        double z = target.prevZ + (target.getZ() - target.prevZ) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY + (target.getY() - target.prevY) * mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + sinAnim * target.getHeight();
        stack.push();
        setupRender();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        float cos;
        float sin;
        for (int i = 0; i <= 30; i++) {
            cos = (float) (x + Math.cos(i * 6.28 / 30) * target.getWidth() * 0.8);
            sin = (float) (z + Math.sin(i * 6.28 / 30) * target.getWidth() * 0.8);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), cos, (float) nextY, sin).color(Render2DEngine.injectAlpha(HudEditor.getColor(i), 170).getRGB()).next();
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), cos, (float) y, sin).color(Render2DEngine.injectAlpha(HudEditor.getColor(i), 0).getRGB()).next();
        }
        tessellator.draw();
        RenderSystem.enableCull();
        endRender();
        RenderSystem.enableDepthTest();
        stack.pop();
    }

    public static void updateTargetESP() {
        prevCircleStep = circleStep;
        circleStep += 0.15f;
    }

    public static double absSinAnimation(double input) {
        return Math.abs(1 + Math.sin(input)) / 2;
    }

    public static Vec3d interpolatePos(float prevposX, float prevposY, float prevposZ, float posX, float posY, float posZ) {
        double x = prevposX + ((posX - prevposX) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = prevposY + ((posY - prevposY) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = prevposZ + ((posZ - prevposZ) * mc.getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        return new Vec3d(x, y, z);
    }

    public static void drawLineDebug(double x, double y, double z, double x1, double y1, double z1, Color color) {
        drawLineDebug(new Vec3d(x, y, z), new Vec3d(x1, y1, z1), color);
    }

    public static void drawLineDebug(Vec3d start, Vec3d end, Color color) {
        DEBUG_LINE_QUEUE.add(new DebugLineAction(start, end, color));
    }

    public record FillAction(Box box, Color color) {
    }

    public record OutlineAction(Box box, Color color, float lineWidth) {
    }

    public record FadeAction(Box box, Color color, Color color2) {
    }

    public record FillSideAction(Box box, Color color, Direction side) {
    }

    public record OutlineSideAction(Box box, Color color, float lineWidth, Direction side) {
    }

    public record DebugLineAction(Vec3d start, Vec3d end, Color color) {
    }

    public record LineAction(Vec3d start, Vec3d end, Color color) {
    }
}

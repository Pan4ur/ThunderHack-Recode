package thunder.hack.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.RandomStringUtils;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40C;
import thunder.hack.gui.font.Texture;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.shaders.GradientGlowProgram;
import thunder.hack.utility.render.shaders.MainMenuProgram;
import thunder.hack.utility.render.shaders.RoundedGradientProgram;
import thunder.hack.utility.render.shaders.RoundedProgram;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Stack;

import static thunder.hack.modules.Module.mc;

public class Render2DEngine {
    public static RoundedGradientProgram ROUNDED_GRADIENT_PROGRAM;
    public static RoundedProgram ROUNDED_PROGRAM;
    public static GradientGlowProgram GRADIENT_GLOW_PROGRAM;
    public static MainMenuProgram MAIN_MENU_PROGRAM;

    private static final Identifier star = new Identifier("textures/star.png");
    private static final Identifier heart = new Identifier("textures/heart.png");

    public static void addWindow(MatrixStack stack, Rectangle r1) {
        Matrix4f matrix = stack.peek().getPositionMatrix();
        Vector4f coord = new Vector4f((float) r1.getX(), (float) r1.getY(), 0, 1);
        Vector4f end = new Vector4f((float) r1.getX1(), (float) r1.getY1(), 0, 1);
        coord.mulTranspose(matrix);
        end.mulTranspose(matrix);
        double x = coord.x();
        double y = coord.y();
        double endX = end.x();
        double endY = end.y();
        Rectangle r = new Rectangle(x, y, endX, endY);
        if (clipStack.empty()) {
            clipStack.push(r);
            beginScissor(r.getX(), r.getY(), r.getX1(), r.getY1());
        } else {
            Rectangle lastClip = clipStack.peek();
            double lsx = lastClip.getX();
            double lsy = lastClip.getY();
            double lstx = lastClip.getX1();
            double lsty = lastClip.getY1();
            double nsx = MathHelper.clamp(r.getX(), lsx, lstx);
            double nsy = MathHelper.clamp(r.getY(), lsy, lsty);
            double nstx = MathHelper.clamp(r.getX1(), nsx, lstx);
            double nsty = MathHelper.clamp(r.getY1(), nsy, lsty);
            clipStack.push(new Rectangle(nsx, nsy, nstx, nsty));
            beginScissor(nsx, nsy, nstx, nsty);
        }
    }

    final static Stack<Rectangle> clipStack = new Stack<>();

    public static void popWindow() {
        clipStack.pop();
        if (clipStack.empty()) {
            endScissor();
        } else {
            Rectangle r = clipStack.peek();
            beginScissor(r.getX(), r.getY(), r.getX1(), r.getY1());
        }
    }

    public static void beginScissor(double x, double y, double endX, double endY) {
        double width = endX - x;
        double height = endY - y;
        width = Math.max(0, width);
        height = Math.max(0, height);
        float d = (float) mc.getWindow().getScaleFactor();
        int ay = (int) ((mc.getWindow().getScaledHeight() - (y + height)) * d);
        RenderSystem.enableScissor((int) (x * d), ay, (int) (width * d), (int) (height * d));
    }


    public static void endScissor() {
        RenderSystem.disableScissor();
    }

    public static void addWindow(MatrixStack stack, float x, float y, float x1, float y1, double animation_factor) {
        float h = y + y1;
        float h2 = (float) (h * (1d - MathUtility.clamp(animation_factor, 0, 1.0025f)));

        float x3 = x;
        float y3 = y + h2;
        float x4 = x1;
        float y4 = y1 - h2;

        if (x4 < x3) {
            x4 = x3;
        }
        if (y4 < y3) {
            y4 = y3;
        }
        addWindow(stack, new Rectangle(x3, y3, x4, y4));
    }

    public static class Rectangle {
        public Rectangle(double x, double y, double width, double heidht) {
            this.x = x;
            this.y = y;
            this.x1 = width;
            this.y1 = heidht;
        }

        private double x;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getX1() {
            return x1;
        }

        public void setX1(double x1) {
            this.x1 = x1;
        }

        public double getY1() {
            return y1;
        }

        public void setY1(double y1) {
            this.y1 = y1;
        }

        private double y;
        private double x1;
        private double y1;

        public boolean contains(double x, double y) {
            return x >= this.x && x <= this.x1 && y >= this.y && y <= this.y1;
        }
    }

    public static HashMap<Integer, BlurredShadow> shadowCache = new HashMap<>();

    public static void drawTexture(DrawContext context, Identifier icon, int x, int y, int width, int height) {
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.enableBlend();
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        context.drawTexture(icon, x, y, 0, 0, width, height, width, height);
    }

    public static void horizontalGradient(MatrixStack matrices, double x1, double y1, double x2, double y2, int startColor, int endColor) {

        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(f1, f2, f3, f).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(f1, f2, f3, f).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(f5, f6, f7, f4).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(f5, f6, f7, f4).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }


    public static void verticalGradient(MatrixStack matrices, double left, double top, double right, double bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) left, (float) top, 0.0F).color(f1, f2, f3, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, 0.0F).color(f5, f6, f7, f4).next();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, 0.0F).color(f5, f6, f7, f4).next();
        bufferBuilder.vertex(matrix, (float) right, (float) top, 0.0F).color(f1, f2, f3, f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).next();
        Tessellator.getInstance().draw();
        endRender();
    }


    public static void drawRectDumbWay(MatrixStack matrices, double x, double y, double x1, double y1, Color c1) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x, (float) y1, 0.0F).color(c1.getRGB()).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(c1.getRGB()).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y, 0.0F).color(c1.getRGB()).next();
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(c1.getRGB()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void drawRectDumbWay(MatrixStack matrices, float x, float y, float x1, float y1, Color c1, Color c2, Color c3, Color c4) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x, (float) y1, 0.0F).color(c1.getRGB()).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(c2.getRGB()).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y, 0.0F).color(c3.getRGB()).next();
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(c4.getRGB()).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
    }

    public static void drawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
        if (!HudEditor.glow.getValue()) return;
        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x = x - blurRadius;
        y = y - blurRadius;

        int identifier = (int) (width * height + width * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            RenderSystem.defaultBlendFunc();
        } else {
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
            return;
        }
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderSystem.enableBlend();
        renderTexture(matrices, x, y, width, height, 0, 0, width, height, width, height);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }


    public static void drawGradientBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
        drawBlurredShadow(matrices, x, y, width, height, blurRadius, color1);
    }


    public static void registerBufferedImageTexture(Texture i, BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            byte[] bytes = baos.toByteArray();
            registerTexture(i, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerTexture(Texture i, byte[] content) {
        try {
            ByteBuffer data = BufferUtils.createByteBuffer(content.length).put(content);
            data.flip();
            NativeImageBackedTexture tex = new NativeImageBackedTexture(NativeImage.read(data));
            mc.execute(() -> mc.getTextureManager().registerTexture(i, tex));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void renderTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).texture((u + 0.0F) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight).next();
        bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).texture((u + 0.0F) / (float) textureWidth, (v + 0.0F) / (float) textureHeight).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void drawElipse(float x, float y, float rx, float ry, float start, float end, float radius, Color color) {

        if (start > end) {
            float endOffset = end;
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
        for (float i = start; i <= end; i += 4) {
            float cos = (float) Math.cos(i * Math.PI / 180) * (radius / ry);
            float sin = (float) Math.sin(i * Math.PI / 180) * (radius / rx);
            bufferBuilder.vertex((x + cos), (y + sin), 0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.lineWidth(1F);
        RenderSystem.disableBlend();
        Render2DEngine.endRender();
    }

    public static void drawElipseSync(float x, float y, float rx, float ry, float start, float end, float radius, Color color) {

        if (start > end) {
            float endOffset = end;
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
        for (float i = start; i <= end; i += 4) {
            float cos = (float) Math.cos(i * Math.PI / 180) * (radius / ry);
            float sin = (float) Math.sin(i * Math.PI / 180) * (radius / rx);
            bufferBuilder.vertex((x + cos), (y + sin), 0f).color(HudEditor.getColor((int) i).getRed(), HudEditor.getColor((int) i).getGreen(), HudEditor.getColor((int) i).getBlue(), HudEditor.getColor((int) i).getAlpha()).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.lineWidth(1F);
        RenderSystem.disableBlend();
        Render2DEngine.endRender();
    }

    public static void renderRoundedGradientRect(MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float Radius) {

        if (!HudEditor.fpsEater.getValue()) {
            Render2DEngine.drawRound(matrices, x, y, width, height, Radius, color1);
            return;
        }
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);


        Render2DEngine.drawRound(matrices, x, y, width, height, Radius, color1);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        setupRender();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x, (float) y + height, 0.0F).color(color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f).next();
        bufferBuilder.vertex(matrix, (float) x + width, (float) y + height, 0.0F).color(color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f).next();
        bufferBuilder.vertex(matrix, (float) x + width, (float) y, 0.0F).color(color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f).next();
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
        RenderSystem.defaultBlendFunc();


    }

    public static void drawRound(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        renderRoundedQuad(matrices, color, x, y, width + x, height + y, radius, 4);
    }

    public static void drawRoundD(MatrixStack matrices, double x, double y, double width, double height, float radius, Color color) {
        renderRoundedQuad(matrices, color, x, y, width + x, height + y, radius, 4);
    }

    public static void drawRoundDoubleColor(MatrixStack matrices, double x, double y, double width, double height, float radius, Color color, Color color2) {
        renderRoundedQuad(matrices, color, color2, x, y, width + x, height + y, radius, 4);
    }

    public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        renderRoundedQuadInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, radius, samples);
        endRender();
    }

    public static void renderRoundedQuad(MatrixStack matrices, Color c, Color c2, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        int color1 = c2.getRGB();

        float f1 = (float) (color1 >> 24 & 255) / 255.0F;
        float g1 = (float) (color1 >> 16 & 255) / 255.0F;
        float h1 = (float) (color1 >> 8 & 255) / 255.0F;
        float k1 = (float) (color1 & 255) / 255.0F;

        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        renderRoundedQuadInternal(matrix, g, h, k, f, g1, h1, k1, f1, fromX, fromY, toX, toY, radius, samples);
        endRender();
    }

    public static void renderRoundedQuad2(MatrixStack matrices, Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        renderRoundedQuadInternal2(matrices.peek().getPositionMatrix(), c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f, c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, c2.getAlpha() / 255f, c3.getRed() / 255f, c3.getGreen() / 255f, c3.getBlue() / 255f, c3.getAlpha() / 255f, c4.getRed() / 255f, c4.getGreen() / 255f, c4.getBlue() / 255f, c4.getAlpha() / 255f, fromX, fromY, toX, toY, radius);
        endRender();
    }

    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double[][] map = new double[][]{new double[]{toX - radius, toY - radius, radius}, new double[]{toX - radius, fromY + radius, radius}, new double[]{fromX + radius, fromY + radius, radius}, new double[]{fromX + radius, toY - radius, radius}};
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
            }
            float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }


    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca1, float cr1, float cg1, float cb1, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double[][] map = new double[][]{new double[]{toX - radius, toY - radius, radius}, new double[]{toX - radius, fromY + radius, radius}, new double[]{fromX + radius, fromY + radius, radius}, new double[]{fromX + radius, toY - radius, radius}};
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                if (i == 1 || i == 0) {
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
                } else {
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr1, cg1, cb1, ca1).next();
                }
            }
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }


    public static void renderRoundedQuadInternal2(Matrix4f matrix, float cr, float cg, float cb, float ca, float cr1, float cg1, float cb1, float ca1, float cr2, float cg2, float cb2, float ca2, float cr3, float cg3, float cb3, float ca3, double fromX, double fromY, double toX, double toY, double radC1) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double[][] map = new double[][]{
                new double[]{
                        toX - radC1,
                        toY - radC1,
                        radC1
                },

                new double[]{
                        toX - radC1,
                        fromY + radC1,
                        radC1
                },

                new double[]{
                        fromX + radC1,
                        fromY + radC1,
                        radC1
                },

                new double[]{
                        fromX + radC1,
                        toY - radC1,
                        radC1
                }
        };


        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90; r < (90 + i * 90); r += 10) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                if (i == 1) {
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
                } else if (i == 0) {
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr1, cg1, cb1, ca1).next();
                } else if (i == 2) {
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr2, cg2, cb2, ca2).next();
                } else {
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr3, cg3, cb3, ca3).next();
                }
            }
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }


    public static void draw2DGradientRect(MatrixStack matrices, float left, float top, float right, float bottom, int leftBottomColor, int leftTopColor, int rightBottomColor, int rightTopColor) {
        float lba = (float) (leftBottomColor >> 24 & 255) / 255.0F;
        float lbr = (float) (leftBottomColor >> 16 & 255) / 255.0F;
        float lbg = (float) (leftBottomColor >> 8 & 255) / 255.0F;
        float lbb = (float) (leftBottomColor & 255) / 255.0F;
        float rba = (float) (rightBottomColor >> 24 & 255) / 255.0F;
        float rbr = (float) (rightBottomColor >> 16 & 255) / 255.0F;
        float rbg = (float) (rightBottomColor >> 8 & 255) / 255.0F;
        float rbb = (float) (rightBottomColor & 255) / 255.0F;
        float lta = (float) (leftTopColor >> 24 & 255) / 255.0F;
        float ltr = (float) (leftTopColor >> 16 & 255) / 255.0F;
        float ltg = (float) (leftTopColor >> 8 & 255) / 255.0F;
        float ltb = (float) (leftTopColor & 255) / 255.0F;
        float rta = (float) (rightTopColor >> 24 & 255) / 255.0F;
        float rtr = (float) (rightTopColor >> 16 & 255) / 255.0F;
        float rtg = (float) (rightTopColor >> 8 & 255) / 255.0F;
        float rtb = (float) (rightTopColor & 255) / 255.0F;


        Matrix4f matrix = matrices.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) right, (float) top, 0.0F).color(rtr, rtg, rtb, rta).next();
        bufferBuilder.vertex(matrix, (float) left, (float) top, 0.0F).color(ltr, ltg, ltb, lta).next();
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, 0.0F).color(lbr, lbg, lbb, lba).next();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, 0.0F).color(rbr, rbg, rbb, rba).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.disableBlend();
    }


    public static void drawGradientRound(MatrixStack ms, float v, float v1, int i, int i1, float v2, Color darker, Color darker1, Color darker2, Color darker3) {
        renderRoundedQuad2(ms, darker, darker1, darker2, darker3, v, v1, v + i, v1 + i1, v2, 9);
    }

    public static float scrollAnimate(float endPoint, float current, float speed) {
        boolean shouldContinueAnimation = endPoint > current;
        if (speed < 0.0f) {
            speed = 0.0f;
        } else if (speed > 1.0f) {
            speed = 1.0f;
        }

        float dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        float factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }

    public static Color injectAlpha(final Color color, final int alpha) {
        // 5 - минимальное для фонтрендерера
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 5, 255));
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed) {
        double thing = speed / 4.0 % 1.0;
        float val = MathHelper.clamp((float) Math.sin(Math.PI * 6 * thing) / 2.0f + 0.5f, 0.0f, 1.0f);
        return new Color(lerp((float) cl1.getRed() / 255.0f, (float) cl2.getRed() / 255.0f, val), lerp((float) cl1.getGreen() / 255.0f, (float) cl2.getGreen() / 255.0f, val), lerp((float) cl1.getBlue() / 255.0f, (float) cl2.getBlue() / 255.0f, val));
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static int getColor(Color color) {
        return getColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int getColor(int bright) {
        return getColor(bright, bright, bright, 255);
    }

    public static Color astolfo(float yDist, float yTotal, float saturation, float speedt) {
        float hue;
        float speed = 1800.0f;
        for (hue = (float) (System.currentTimeMillis() % (long) ((int) speed)) + (yTotal - yDist) * speedt; hue > speed; hue -= speed) {
        }
        if ((double) (hue /= speed) > 0.5) {
            hue = 0.5f - (hue - 0.5f);
        }
        return Color.getHSBColor(hue += 0.5f, saturation, 1.0f);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        color |= blue;
        return color;
    }


    public static Color astolfo(boolean clickgui, int yOffset) {
        float speed = clickgui ? 35 * 100 : 30 * 100;
        float hue = (System.currentTimeMillis() % (int) speed) + yOffset;
        if (hue > speed) {
            hue -= speed;
        }
        hue /= speed;
        if (hue > 0.5F) {
            hue = 0.5F - (hue - 0.5F);
        }
        hue += 0.5F;
        return Color.getHSBColor(hue, 0.4F, 1F);
    }


    public static int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public static Color getColor2(int hex, int alpha) {
        float f1 = (float) (hex >> 16 & 255) / 255.0F;
        float f2 = (float) (hex >> 8 & 255) / 255.0F;
        float f3 = (float) (hex & 255) / 255.0F;
        return new Color((int) (f1 * 255f), (int) (f2 * 255f), (int) (f3 * 255f), alpha);
    }

    public static Color rainbow(int delay, float saturation, float brightness) {
        double rainbow = Math.ceil((System.currentTimeMillis() + delay) / 16f);
        rainbow %= 360;
        return Color.getHSBColor((float) (rainbow / 360), saturation, brightness);
    }

    public static int fadeColor(int startColor, int endColor, float progress) {
        if (progress > 1) {
            progress = 1 - progress % 1;
        }
        return fade(startColor, endColor, progress);
    }

    public static int fade(int startColor, int endColor, float progress) {
        float invert = 1.0f - progress;
        int r = (int) ((startColor >> 16 & 0xFF) * invert + (endColor >> 16 & 0xFF) * progress);
        int g = (int) ((startColor >> 8 & 0xFF) * invert + (endColor >> 8 & 0xFF) * progress);
        int b = (int) ((startColor & 0xFF) * invert + (endColor & 0xFF) * progress);
        int a = (int) ((startColor >> 24 & 0xFF) * invert + (endColor >> 24 & 0xFF) * progress);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }


    public static Color skyRainbow(int speed, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        return Color.getHSBColor((double) ((float) ((angle %= 360.0) / 360.0)) < 0.5 ? -((float) (angle / 360.0))
                : (float) (angle / 360.0), 0.5F, 1.0F);
    }

    public static int fade(Color color, int delay) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(((float) (System.currentTimeMillis() % 2000L + delay) / 1000.0F) % 2F - 1.0F);
        brightness = 0.5F + 0.5F * brightness;
        hsb[2] = brightness % 2.0F;
        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    public static Color fade(int speed, int index, Color color, float alpha) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;

        Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360f));

        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int) (alpha * 255))));
    }


    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }


    public static Color getAnalogousColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float degree = 0.84f;
        float newHueSubtracted = hsb[0] - degree;
        return new Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity)).getRGB();
    }

    public static Color darker(Color color, float FACTOR) {
        return new Color(Math.max((int) (color.getRed() * FACTOR), 0), Math.max((int) (color.getGreen() * FACTOR), 0), Math.max((int) (color.getBlue() * FACTOR), 0), color.getAlpha());
    }


    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.max(0, Math.min(255, (int) (opacity * 255))));
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, angle / 360f)
                : interpolateColorC(start, end, angle / 360f);
    }

    public static int interpolateColor(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return interpolateColorC(color1, color2, amount).getRGB();
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        Color cColor1 = new Color(color1);
        Color cColor2 = new Color(color2);
        return interpolateColorC(cColor1, cColor2, amount).getRGB();
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount),
                interpolateFloat(color1HSB[1], color2HSB[1], amount),
                interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float) interpolate(oldValue, newValue, (float) interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int) interpolate(oldValue, newValue, (float) interpolationValue);
    }

    public static void drawRoundShader(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        buffer.vertex(matrix, x, y, 0).next();
        buffer.vertex(matrix, x, y + height, 0).next();
        buffer.vertex(matrix, x + width, y + height, 0).next();
        buffer.vertex(matrix, x + width, y, 0).next();

        ROUNDED_PROGRAM.setParameters(x, y, width, height, radius, color);
        ROUNDED_PROGRAM.use();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawMainMenuShader(MatrixStack matrices, float x, float y, float width, float height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(matrix, x, y, 0).next();
        buffer.vertex(matrix, x, y + height, 0).next();
        buffer.vertex(matrix, x + width, y + height, 0).next();
        buffer.vertex(matrix, x + width, y, 0).next();
        MAIN_MENU_PROGRAM.setParameters(x, y, width, height);
        MAIN_MENU_PROGRAM.use();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawGradientRoundShader(MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float radius) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(matrix, x, y, 0).next();
        buffer.vertex(matrix, x, y + height, 0).next();
        buffer.vertex(matrix, x + width, y + height, 0).next();
        buffer.vertex(matrix, x + width, y, 0).next();
        ROUNDED_GRADIENT_PROGRAM.setParameters(x, y, width, height, radius, color1, color2, color3, color4);
        ROUNDED_GRADIENT_PROGRAM.use();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawGradientGlow(MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float radius, float softness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(matrix, x - 10, y - 10, 0).next();
        buffer.vertex(matrix, x - 10, y + height + 20, 0).next();
        buffer.vertex(matrix, x + width + 20, y + height + 20, 0).next();
        buffer.vertex(matrix, x + width + 20, y -10, 0).next();
        GRADIENT_GLOW_PROGRAM.setParameters(x, y, width, height, radius, softness, color1, color2, color3, color4);
        GRADIENT_GLOW_PROGRAM.use();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawOrbiz(MatrixStack matrices,float z, final double r, Color c) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 20; i++) {
            final double x2 = Math.sin(((i * 18 * Math.PI) / 180)) * r;
            final double y2 = Math.cos(((i * 18 * Math.PI) / 180)) * r;
            bufferBuilder.vertex(matrix, (float) (x2), (float) (y2), z).color(c.getRed() / 255f,c.getGreen() / 255f,c.getBlue() / 255f,0.4f).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void drawStar(MatrixStack matrices, Color c, float scale) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0,star);
        RenderSystem.setShaderColor(c.getRed() / 255f,c.getGreen() / 255f,c.getBlue() / 255f,c.getAlpha() / 255f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(mc.player.age * 2));
        Render2DEngine.renderTexture(matrices,0, 0,scale,scale,0,0,128,128,128,128);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f,1f,1f,1f);
    }

    public static void drawHeart(MatrixStack matrices, Color c, float scale) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0,heart);
        RenderSystem.setShaderColor(c.getRed() / 255f,c.getGreen() / 255f,c.getBlue() / 255f,c.getAlpha() / 255f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(mc.player.age * 2));
        Render2DEngine.renderTexture(matrices,0, 0,scale, scale,0,0,128,128,128,128);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f,1f,1f,1f);
    }


    // http://www.java2s.com/example/java/2d-graphics/check-if-a-color-is-more-dark-than-light.html
    public static boolean isDark(Color color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        return isDark(r, g, b);
    }

    public static boolean isDark(double r, double g, double b) {
        double dWhite = colorDistance(r, g, b, 1.0, 1.0, 1.0);
        double dBlack = colorDistance(r, g, b, 0.0, 0.0, 0.0);
        return dBlack < dWhite;
    }

    public static double colorDistance(double r1, double g1, double b1, double r2, double g2, double b2) {
        double a = r2 - r1;
        double b = g2 - g1;
        double c = b2 - b1;
        return Math.sqrt(a * a + b * b + c * c);
    }
    //

    public static class BlurredShadow {
        Texture id;

        public BlurredShadow(BufferedImage bufferedImage) {
            this.id = new Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));
            registerBufferedImageTexture(id, bufferedImage);
        }

        public void bind() {
            RenderSystem.setShaderTexture(0, id);
        }
    }
}

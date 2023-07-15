package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;

import java.awt.*;

public class Particles {
    public double x, y, deltaX, deltaY, size, opacity;
    public Color color;




    public static Color mixColors(final Color color1, final Color color2, final double percent) {
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }


    public void render2D() {

        size /= 2;
        /*
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (double i = 0; i <= 90; i++) {
            final double angle = i * 4 * (Math.PI * 2) / 360;
            bufferbuilder.vertex(x + (size * Math.cos(angle)) + size, y + (size * Math.sin(angle)) + size, 0f).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1).next();
        }
     //   tessellator.draw();
        BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

         */


        drawPolygonPart(x,y, (int) size,360,color.getRGB(),-1);
    }



    public static void drawPolygonPart(double x, double y, int radius, int part, int startColor, int endColor) {
        float alpha = (float) (startColor >> 24 & 255) / 255.0F;
        float red = (float) (startColor >> 16 & 255) / 255.0F;
        float green = (float) (startColor >> 8 & 255) / 255.0F;
        float blue = (float) (startColor & 255) / 255.0F;
        float alpha1 = (float) (endColor >> 24 & 255) / 255.0F;
        float red1 = (float) (endColor >> 16 & 255) / 255.0F;
        float green1 = (float) (endColor >> 8 & 255) / 255.0F;
        float blue1 = (float) (endColor & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(x, y, 0).color(red, green, blue, alpha).next();
        final double TWICE_PI = Math.PI * 2;
        for (int i = part * 90; i <= part * 90 + 90; i++) {
            double angle = (TWICE_PI * i / 360) + Math.toRadians(180);
            bufferbuilder.vertex(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).color(red1, green1, blue1, alpha1).next();
        }
        tessellator.draw();
        RenderSystem.disableBlend();
    }

    public void updatePosition() {
        x += deltaX * 2;
        y += deltaY * 2;
        deltaY *= 0.95;
        deltaX *= 0.95;
        opacity -= 2f;
        if (opacity < 1) opacity = 1;
    }

    public void init(final double x, final double y, final double deltaX, final double deltaY, final double size, final Color color) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.size = size;
        this.opacity = 254;
        this.color = color;
    }
}
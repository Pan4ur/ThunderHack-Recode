package thunder.hack.gui.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.math.MathUtility;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import org.joml.Matrix4f;

import java.awt.Font;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;
public class FontRenderer {
    static final Map<Character, Integer> colorMap = Util.make(() -> {
        Map<Character, Integer> ci = new HashMap<>();
        ci.put('0', 0x000000);
        ci.put('1', 0x0000AA);
        ci.put('2', 0x00AA00);
        ci.put('3', 0x00AAAA);
        ci.put('4', 0xAA0000);
        ci.put('5', 0xAA00AA);
        ci.put('6', 0xFFAA00);
        ci.put('7', 0xAAAAAA);
        ci.put('8', 0x555555);
        ci.put('9', 0x5555FF);
        ci.put('A', 0x55FF55);
        ci.put('B', 0x55FFFF);
        ci.put('C', 0xFF5555);
        ci.put('D', 0xFF55FF);
        ci.put('E', 0xFFFF55);
        ci.put('F', 0xFFFFFF);
        return ci;
    });
    final Font f;
    final Map<Character, Glyph> glyphMap = new ConcurrentHashMap<>();
    final int size;
    final float cachedHeight;

    public FontRenderer(Font f, int size) {
        this.f = f;
        this.size = size;
        init();
        cachedHeight = (float) glyphMap.values()
                .stream()
                .max(Comparator.comparingDouble(value -> value.dimensions.getHeight()))
                .orElseThrow().dimensions.getHeight() * 0.25f;
    }

    public int getSize() {
        return size;
    }

    void init() {
        char[] chars = "ABCabc 123+-".toCharArray();
        for (char aChar : chars) {
            Glyph glyph = new Glyph(aChar, f);
            glyphMap.put(aChar, glyph);
        }
    }

    public void drawString(MatrixStack matrices, String s, float x, float y, float r, float g, float b, float a) {
        float roundedX = (float) MathUtility.roundToDecimal(x, 1);
        float roundedY = (float) MathUtility.roundToDecimal(y, 1);
        float r1 = r;
        float g1 = g;
        float b1 = b;
        matrices.push();
        matrices.translate(roundedX, roundedY, 0);
        matrices.scale(0.25F, 0.25F, 1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
       // RenderSystem.enableTexture();
        RenderSystem.disableCull();
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        boolean isInSelector = false;
        for (char c : s.toCharArray()) {
            if (isInSelector) {
                char upper = String.valueOf(c).toUpperCase().charAt(0);
                int color = colorMap.getOrDefault(upper, 0xFFFFFF);
                r1 = (float) (color >> 16 & 255) / 255.0F;
                g1 = (float) (color >> 8 & 255) / 255.0F;
                b1 = (float) (color & 255) / 255.0F;
                isInSelector = false;
                continue;
            }
            if (c == '§') {
                isInSelector = true;
                continue;
            }

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            double prevWidth = drawChar(bufferBuilder, matrix, c, r1, g1, b1, a);
            matrices.translate(prevWidth, 0, 0);
        }

        matrices.pop();
    }

    public void drawGradientString(MatrixStack matrices, String s, float x, float y, int offset) {
        float roundedX = (float) MathUtility.roundToDecimal(x, 1);
        float roundedY = (float) MathUtility.roundToDecimal(y, 1);
        float r1 = 0;
        float g1 = 0;
        float b1 = 0;
        matrices.push();
        matrices.translate(roundedX, roundedY, 0);
        matrices.scale(0.25F, 0.25F, 1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // RenderSystem.enableTexture();
        RenderSystem.disableCull();
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        int num = 0;
        for (char c : s.toCharArray()) {
            int color = HudEditor.getColor(num * offset).getRGB();
            r1 = (float) (color >> 16 & 255) / 255.0F;
            g1 = (float) (color >> 8 & 255) / 255.0F;
            b1 = (float) (color & 255) / 255.0F;

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            double prevWidth = drawChar(bufferBuilder, matrix, c, r1, g1, b1, 1f);
            matrices.translate(prevWidth, 0, 0);
            num++;
        }

        matrices.pop();
    }

    @SuppressWarnings("AssignmentToForLoopParameter")
    String stripControlCodes(String in) {
        char[] s = in.toCharArray();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            char current = s[i];
            if (current == '§') {
                i++;
                continue;
            }
            out.append(current);
        }
        return out.toString();
    }

    public float getStringWidth(String text) {
        float wid = 0;
        for (char c : stripControlCodes(text).toCharArray()) {
            Glyph g = glyphMap.computeIfAbsent(c, character -> new Glyph(character, this.f));
            wid += g.dimensions.getWidth();
        }
        return wid * 0.25f;
    }

    public String trimStringToWidth(String t, float maxWidth) {
        StringBuilder sb = new StringBuilder();
        for (char c : t.toCharArray()) {
            if (getStringWidth(sb.toString() + c) >= maxWidth) {
                return sb.toString();
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public void drawCenteredString(MatrixStack matrices, String s, float x, float y, float r, float g, float b, float a) {
        drawString(matrices, s, x - getStringWidth(s) / 2f, y, r, g, b, a);
    }

    public float getFontHeight() {
        return cachedHeight;
    }

    private double drawChar(BufferBuilder bufferBuilder, Matrix4f matrix, char c, float r, float g, float b, float a) {
        Glyph glyph = glyphMap.computeIfAbsent(c, character -> new Glyph(character, this.f));
        RenderSystem.setShaderTexture(0, glyph.getImageTex());

        float height = (float) glyph.dimensions.getHeight();
        float width = (float) glyph.dimensions.getWidth();

        float inOffsetX = glyph.offsetX / (width + 10);
        float inOffsetY = glyph.offsetY / (height + 10);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix, 0, height, 0).texture(0 + inOffsetX, 1 - inOffsetY).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, width, height, 0).texture(1 - inOffsetX, 1 - inOffsetY).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, width, 0, 0).texture(1 - inOffsetX, 0 + inOffsetY).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix, 0, 0, 0).texture(0 + inOffsetX, 0 + inOffsetY).color(r, g, b, a).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        return width;
    }
}

package thunder.hack.gui.font;




import thunder.hack.cmd.Command;
import thunder.hack.utility.Util;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Glyph {
    final Texture imageTex;
    final Font f;
    final char c;

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    final int offsetX = 0;

    final int offsetY = 0;
    Rectangle2D dimensions;

    public Glyph(char c, Font f) {
        this.imageTex = new Texture("font/glyphs/" + ((int) c) + "-" + f.getName().toLowerCase().hashCode() + (int) Math.floor(Math.random() * 0xFFFF));
        this.f = f;
        this.c = c;
        generateTexture();
    }

    void generateTexture() {
        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, true, true);
        Rectangle2D dim = f.getStringBounds(String.valueOf(c), fontRenderContext);
        this.dimensions = dim;
        BufferedImage bufferedImage = new BufferedImage((int) Math.ceil(dim.getWidth()), (int) Math.ceil(dim.getHeight()), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();

        g.setFont(f);
        // Set Color to Transparent
        g.setColor(new Color(255, 255, 255, 0));
        // Set the image background to transparent
        g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        g.setColor(Color.white);

        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fontMetrics = g.getFontMetrics();
        g.drawString(String.valueOf(c), offsetX, offsetY + fontMetrics.getAscent());

        registerBufferedImageTexture(imageTex, bufferedImage);
    }

    public Texture getImageTex() {
        return imageTex;
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
            Util.mc.execute(() -> Util.mc.getTextureManager().registerTexture(i, tex));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

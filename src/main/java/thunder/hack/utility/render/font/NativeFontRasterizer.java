package thunder.hack.utility.render.font;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL;

import java.util.*;

public class NativeFontRasterizer {
    private final List<GlyphData> glyphBuffer = new ArrayList<>();
    private LineMetrics lineMetrics;

    public Font loadFont(byte[] font, String name) {
        final int id = nativeLoadFont(font, name);
        if (id == -1) throw new RuntimeException("Failed to load font with name " + name);

        return new Font(id);
    }

    public int getGlyphCount(@NotNull Font font) {
        return nativeGetGlyphCount(font.id);
    }

    public String getName(@NotNull Font font) {
        return nativeGetName(font.id);
    }

    public Font searchFont(String name) {
        final int id = nativeSearchFont(name);
        if (id == -1) throw new RuntimeException("Failed to find font with name " + name);

        return new Font(id);
    }

    public void deleteFont(String name) {
        final int id = nativeDeleteFont(name);
        if (id == -1) throw new RuntimeException("Failed to delete font with name " + name);
    }

    public List<GlyphData> makeAtlas(@NotNull Font font, float size, int segment, int textureId) {
        glyphBuffer.clear();

        final boolean result = nativeMakeAtlas(font.id, size, segment, this, textureId);
        if (!result) throw new RuntimeException("Failed to make atlas");
        final List<GlyphData> bufferCopy = new ArrayList<>();
        Collections.copy(bufferCopy, glyphBuffer);

        glyphBuffer.clear();
        return bufferCopy;
    }

    private LineMetrics getLineMetrics(@NotNull Font font, float size, boolean horizontal) {
        lineMetrics = null;

        final boolean result = nativeGetLineMetrics(font.id, size, horizontal, this);
        if (!result) throw new RuntimeException("Failed to get line metrics");
        final LineMetrics metrics = Objects.requireNonNull(lineMetrics);

        lineMetrics = null;
        return metrics;
    }

    public LineMetrics getHorizontalLineMetrics(Font font, float size) {
        return getLineMetrics(font, size, true);
    }

    public LineMetrics getVerticalLineMetrics(Font font, float size) {
        return getLineMetrics(font, size, false);
    }

    public static native void nativeSetup(NativeFontRasterizer callback);

    private static native int nativeLoadFont(byte[] font, String name);

    private static native int nativeGetGlyphCount(int font);

    private static native String nativeGetName(int font);

    private static native int nativeSearchFont(String name);

    private static native int nativeDeleteFont(String name);

    private static native boolean nativeMakeAtlas(int font, float size, int segment, NativeFontRasterizer callback, int textureId);

    private static native boolean nativeGetLineMetrics(int font, float size, boolean horizontal, NativeFontRasterizer callback);

    private long glFunction(String name) {
        return Objects.requireNonNull(GL.getFunctionProvider()).getFunctionAddress(name);
    }

    private void pushCharData(char character, int u, int v, int width, int height, int xMin, int yMin, float advanceWidth, float advanceHeight, float boundsXMin, float boundsYMin, float boundsWidth, float boundsHeight) {
        glyphBuffer.add(new GlyphData(character, u, v, width, height, xMin, yMin, advanceWidth, advanceHeight, boundsXMin, boundsYMin, boundsWidth, boundsHeight));
    }

    private void pushLineMetrics(float ascent, float descent, float lineGap, float newLIneSize) {
        lineMetrics = new LineMetrics(ascent, descent, lineGap, newLIneSize);
    }

    record GlyphData(
            char character,
            int u,
            int v,

            int width,
            int height,
            int xMin,
            int yMin,

            float advanceWidth,
            float advanceHeight,

            float boundsXMin,
            float boundsYMin,
            float boundsWidth,
            float boundsHeight
    ) {
    }

    record LineMetrics(
            float ascent,
            float descent,
            float lineGap,
            float newLineSize
    ) {
    }

    record Font(int id) {
    }
}

package thunder.hack.setting.impl;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public final class ColorSetting {
    private int color;
    private final int defaultColor;
    private boolean rainbow;

    public ColorSetting(@NotNull Color color) {
        this(color.getRGB());
    }

    public ColorSetting(int color) {
        this.color = color;
        this.defaultColor = color;
    }

    public @NotNull ColorSetting withAlpha(int alpha) {
        int red = (getColor() >> 16) & 0xFF;
        int green = (getColor() >> 8) & 0xFF;
        int blue = (getColor()) & 0xFF;
        return new ColorSetting(((alpha & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                ((blue & 0xFF)));
    }

    public int getColor() {
        if (rainbow) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis()) / 20.0);
            rainbowState %= 360;
            int rgb = Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
            int alpha = (color >> 24) & 0xff;
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = (rgb) & 0xFF;
            return ((alpha & 0xFF) << 24) |
                    ((red & 0xFF) << 16) |
                    ((green & 0xFF) << 8) |
                    ((blue & 0xFF));
        }
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getRed() {
        if (rainbow) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis()) / 20.0);
            rainbowState %= 360;
            int rgb = Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
            return (rgb >> 16) & 0xFF;
        }
        return (color >> 16) & 0xFF;
    }

    public int getGreen() {
        if (rainbow) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis()) / 20.0);
            rainbowState %= 360;
            int rgb = Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
            return (rgb >> 8) & 0xFF;
        }
        return (color >> 8) & 0xFF;
    }

    public int getBlue() {
        if (rainbow) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis()) / 20.0);
            rainbowState %= 360;
            int rgb = Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
            return (rgb) & 0xFF;
        }
        return (color) & 0xFF;
    }

    public float getGlRed() {
        return getRed() / 255f;
    }

    public float getGlBlue() {
        return getBlue() / 255f;
    }

    public float getGlGreen() {
        return getGreen() / 255f;
    }

    public float getGlAlpha() {
        return getAlpha() / 255f;
    }

    public int getAlpha() {
        return (color >> 24) & 0xff;
    }

    public @NotNull Color getColorObject() {
        return new Color(color);
    }

    public int getRawColor() {
        return color;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public void setDefault() {
        setColor(defaultColor);
    }
}
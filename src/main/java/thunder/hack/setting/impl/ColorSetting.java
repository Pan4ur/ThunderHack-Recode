package thunder.hack.setting.impl;

import java.awt.*;

public final class ColorSetting {

    private int color;
    private boolean cycle = false;
    private int globalOffset = 0;

    public ColorSetting(Color color) {
        this.color = color.getRGB();
    }

    public ColorSetting(int color) {
        this.color = color;
    }

    public ColorSetting(int color, boolean cycle) {
        this.color = color;
        this.cycle = cycle;
    }

    public ColorSetting(int color, boolean cycle, int globalOffset) {
        this.color = color;
        this.cycle = cycle;
        this.globalOffset = globalOffset;
    }

    public ColorSetting withAlpha(int alpha) {
        int red = (getColor() >> 16) & 0xFF;
        int green = (getColor() >> 8) & 0xFF;
        int blue = (getColor()) & 0xFF;
        return new ColorSetting(((alpha & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                ((blue & 0xFF)));
    }

    public static int parseColor(String nm) throws NumberFormatException {
        Integer intval = Integer.decode(nm);
        return intval;
    }

    public int getColor() {
        if (cycle) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis() + 300 + globalOffset) / 20.0);
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

    public int getGlobalOffset() {
        return globalOffset;
    }

    public void setGlobalOffset(int globalOffset) {
        this.globalOffset = globalOffset;
    }

    public int getOffsetColor(int offset) {
        if (cycle) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis() + 300 + offset + globalOffset) / 20.0);
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

    public int getRed() {
        if (cycle) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis() + 300 + globalOffset) / 20.0);
            rainbowState %= 360;
            int rgb = Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
            return (rgb >> 16) & 0xFF;
        }
        return (color >> 16) & 0xFF;
    }

    public int getGreen() {
        if (cycle) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis() + 300 + globalOffset) / 20.0);
            rainbowState %= 360;
            int rgb = Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
            return (rgb >> 8) & 0xFF;
        }
        return (color >> 8) & 0xFF;
    }

    public int getBlue() {
        if (cycle) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis() + 300 + globalOffset) / 20.0);
            rainbowState %= 360;
            int rgb = Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
            return (rgb) & 0xFF;
        }
        return (color) & 0xFF;
    }

    public int getAlpha() {
        return (color >> 24) & 0xff;
    }

    public Color getColorObject() {
        int color = getColor();
        int alpha = (color >> 24) & 0xff;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color) & 0xFF;

        return new Color(red, green, blue, alpha);

    }

    public int getRawColor() {
        return color;
    }

    public boolean isCycle() {
        return cycle;
    }

    public void setCycle(boolean cycle) {
        this.cycle = cycle;
    }

    public void toggleCycle() {
        this.cycle = !this.cycle;
    }

}
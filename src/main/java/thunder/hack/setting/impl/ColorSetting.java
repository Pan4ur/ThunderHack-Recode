package thunder.hack.setting.impl;

import org.jetbrains.annotations.NotNull;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;

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
        return rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1f, 1, 1).getRGB() : color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getRed() {
        return rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1f, 1, 1).getRed() : (color >> 16) & 0xFF;
    }

    public int getGreen() {
        return rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1f, 1, 1).getGreen() : (color >> 8) & 0xFF;
    }

    public int getBlue() {
        return rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1f, 1, 1).getBlue() : (color) & 0xFF;
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
        return rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1f, 1, 1) : new Color(color, true);
    }

    public int getRawColor() {
        return rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1f, 1, 1).getRGB() : color;
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
package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class ColorPickerElement extends AbstractElement {
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;

    private boolean afocused;
    private boolean hfocused;
    private boolean sbfocused;

    private float spos, bpos, hpos, apos;

    private Color prevColor;

    private boolean firstInit, extended;

    private final Setting colorSetting;

    public ColorSetting getColorSetting() {
        return (ColorSetting) colorSetting.getValue();
    }

    public ColorPickerElement(Setting setting) {
        super(setting);
        this.colorSetting = setting;
        prevColor = getColorSetting().getColorObject();
        updatePos();
        firstInit = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        MatrixStack matrixStack = context.getMatrices();

        boolean colorHovered = Render2DEngine.isHovered(mouseX, mouseY, x, y + 5f, 90, 7);

        FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getName(), x + 6, y + 8, new Color(-1).getRGB());

        Render2DEngine.drawBlurredShadow(matrixStack, x + width - 22f, y + 5f, 14, 7, colorHovered ? 6 : 10, getColorSetting().getColorObject());
        if (colorHovered)
            Render2DEngine.drawRound(matrixStack, x + width - 22.5f, y + 4.5f, 15, 8, 1, getColorSetting().getColorObject());
        else
            Render2DEngine.drawRound(matrixStack, x + width - 22, y + 5, 14, 7, 1, getColorSetting().getColorObject());

        if (!extended)
            return;

        boolean rainbowHovered = Render2DEngine.isHovered(mouseX, mouseY, x + 36f, y + 54f, 24, 7);
        boolean copyHovered = Render2DEngine.isHovered(mouseX, mouseY, x + 9f, y + 54f, 24, 7);
        boolean pasteHovered = Render2DEngine.isHovered(mouseX, mouseY, x + 63f, y + 54f, 24, 7);
        boolean dark = Render2DEngine.isDark(ThunderHack.copy_color);
        boolean dark2 = Render2DEngine.isDark(getColorSetting().getColorObject());

        Render2DEngine.drawRect(matrixStack, x + 9f, y + 54f, 24, 7, new Color(0x424242));
        FontRenderers.sf_medium_mini.drawString(matrixStack, "Сopy", x + 13, y + 56.5f, copyHovered ? new Color(0xA3FFFFFF, true).getRGB() : Color.WHITE.getRGB());

        Render2DEngine.drawRect(matrixStack, x + 36f, y + 54f, 24, 7, getColorSetting().isRainbow() ? getColorSetting().getColorObject() : new Color(0x424242));
        FontRenderers.sf_medium_mini.drawString(matrixStack, "RB", x + 44f, y + 56.5f, rainbowHovered ? new Color(0xA3FFFFFF, true).getRGB() : (dark2 ? Color.WHITE.getRGB() : Color.BLACK.getRGB()));

        Render2DEngine.drawRect(matrixStack, x + 63f, y + 54f, 24, 7, ThunderHack.copy_color);
        FontRenderers.sf_medium_mini.drawString(matrixStack, "Paste", x + 67f, y + 56.5f, pasteHovered ? new Color(0xA3FFFFFF, true).getRGB() : dark ? Color.WHITE.getRGB() : Color.BLACK.getRGB());

        renderPicker(matrixStack, mouseX, mouseY, getColorSetting().getColorObject());
    }

    @Override
    public float getHeight() {
        return extended ? 66 : 15;
    }

    private void renderPicker(MatrixStack matrixStack, int mouseX, int mouseY, Color color) {
        double cx = x + 6;
        double cy = y + 16;
        double cw = width - 38;
        double ch = height - 30;

        if (prevColor != getColorSetting().getColorObject()) {
            updatePos();
            prevColor = getColorSetting().getColorObject();
        }

        if (firstInit) {
            spos = (float) ((cx + cw) - (cw - (cw * saturation)));
            bpos = (float) ((cy + (ch - (ch * brightness))));
            hpos = (float) ((cy + (ch - 3 + ((ch - 3) * hue))));
            apos = (float) ((cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))));
            firstInit = false;
        }

        spos = Render2DEngine.scrollAnimate(spos, (float) ((cx + cw) - (cw - (cw * saturation))), .6f);
        bpos = Render2DEngine.scrollAnimate(bpos, (float) (cy + (ch - (ch * brightness))), .6f);
        hpos = Render2DEngine.scrollAnimate(hpos, (float) (cy + (ch - 3 + ((ch - 3) * hue))), .6f);
        apos = Render2DEngine.scrollAnimate(apos, (float) (cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))), .6f);

        Color colorA = Color.getHSBColor(hue, 0.0F, 1.0F), colorB = Color.getHSBColor(hue, 1.0F, 1.0F);
        Color colorC = new Color(0, 0, 0, 0), colorD = new Color(0, 0, 0);

        Render2DEngine.horizontalGradient(matrixStack, (float) cx + 2, (float) cy, (float) (cx + cw), (float) (cy + ch), colorA, colorB);
        Render2DEngine.verticalGradient(matrixStack, (float) (cx + 2), (float) cy, (float) (cx + cw), (float) (cy + ch), colorC, colorD);

        for (float i = 1f; i < ch - 2f; i += 1f) {
            float curHue = (float) (1f / (ch / i));
            Render2DEngine.drawRect(matrixStack, (float) (cx + cw + 4), (float) (cy + i), 8, 1, Color.getHSBColor(curHue, 1f, 1f));
        }

        Render2DEngine.drawRect(matrixStack, (float) (cx + cw + 17), (float) (cy + 1f), 8f, (float) (ch - 3), new Color(0xFFFFFFFF));

        Render2DEngine.verticalGradient(matrixStack, (float) (cx + cw + 17), (float) (cy + 0.8f), (float) (cx + cw + 25), (float) (cy + ch - 2), new Color(color.getRed(), color.getGreen(), color.getBlue(), 255), new Color(0, 0, 0, 0));

        Render2DEngine.drawRect(matrixStack, (float) (cx + cw + 3), hpos + 0.5f, 10, 1, Color.WHITE);
        Render2DEngine.drawRect(matrixStack, (float) (cx + cw + 16), apos + 0.5f, 10, 1, Color.WHITE);
        Render2DEngine.drawRound(matrixStack, spos - 1.5f, bpos - 1.5f, 3, 3, 1.5f, new Color(-1));

        Color value = Color.getHSBColor(hue, saturation, brightness);

        if (sbfocused) {
            saturation = (float) ((MathUtility.clamp((float) (mouseX - cx), 0f, (float) cw)) / cw);
            brightness = (float) ((ch - MathUtility.clamp((float) (mouseY - cy), 0, (float) ch)) / ch);
            value = Color.getHSBColor(hue, saturation, brightness);
            setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
        }

        if (hfocused) {
            hue = (float) -((ch - MathUtility.clamp((float) (mouseY - cy), 0, (float) ch)) / ch);
            value = Color.getHSBColor(hue, saturation, brightness);
            setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
        }

        if (afocused) {
            alpha = (int) (((ch - MathUtility.clamp((float) (mouseY - cy), 0, (float) ch)) / ch) * 255);
            setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
        }
    }

    private void updatePos() {
        float[] hsb = Color.RGBtoHSB(getColorSetting().getColorObject().getRed(), getColorSetting().getColorObject().getGreen(), getColorSetting().getColorObject().getBlue(), null);
        hue = -1 + hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = getColorSetting().getAlpha();
    }

    private void setColor(Color color) {
        getColorSetting().setColor(color.getRGB());
        prevColor = color;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        double cx = x + 4;
        double cy = y + 17;
        double cw = width - 34;
        double ch = height - 30;

        boolean rainbowHovered = Render2DEngine.isHovered(mouseX, mouseY, x + 36f, y + 54f, 24, 7);
        boolean copyHovered = Render2DEngine.isHovered(mouseX, mouseY, x + 9f, y + 54f, 24, 7);
        boolean pasteHovered = Render2DEngine.isHovered(mouseX, mouseY, x + 63f, y + 54f, 24, 7);
        boolean colorHovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, 90, 11);

        if (colorHovered)
            extended = !extended;

        if (!extended)
            return;

        if (Render2DEngine.isHovered(mouseX, mouseY, cx + cw + 17, cy, 8, ch) && button == 0) afocused = true;

        else if (Render2DEngine.isHovered(mouseX, mouseY, cx + cw + 4, cy, 8, ch) && button == 0) hfocused = true;

        else if (Render2DEngine.isHovered(mouseX, mouseY, cx, cy, cw, ch) && button == 0) sbfocused = true;

        else if (rainbowHovered && button == 0) getColorSetting().setRainbow(!getColorSetting().isRainbow());

        else if (copyHovered) ThunderHack.copy_color = getColorSetting().getColorObject();

        else if (pasteHovered) getColorSetting().setColor(ThunderHack.copy_color.getRGB());

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        hfocused = false;
        afocused = false;
        sbfocused = false;
    }

    @Override
    public void onClose() {
        hfocused = false;
        afocused = false;
        sbfocused = false;
    }
}

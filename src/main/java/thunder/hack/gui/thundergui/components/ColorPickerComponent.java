package thunder.hack.gui.thundergui.components;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class ColorPickerComponent extends SettingElement {
    private final Setting colorSetting;
    private boolean open;
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;

    private boolean afocused;
    private boolean hfocused;
    private boolean sbfocused;

    private boolean copy_focused;
    private boolean paste_focused;
    private boolean rainbow_focused;

    private float spos, bpos, hpos, apos;
    private Color prevColor;
    private boolean firstInit;

    public ColorPickerComponent(Setting setting) {
        super(setting);
        this.colorSetting = setting;
        firstInit = true;
    }

    public ColorSetting getColorSetting() {
        return (ColorSetting) colorSetting.getValue();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
        super.render(stack, mouseX, mouseY, delta);
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        FontRenderers.modules.drawString(stack, getSetting().getName(), getX(), getY() + 5, isHovered() ? -1 : new Color(0xB0FFFFFF, true).getRGB());
        Render2DEngine.drawBlurredShadow(stack, (int) (x + width - 20), (int) (y + 5), 14, 6, 10, getColorSetting().getColorObject());
        Render2DEngine.drawRound(stack, x + width - 20, y + 5, 14, 6, 1, getColorSetting().getColorObject());
        if (open)
            renderPicker(stack, mouseX, mouseY, getColorSetting().getColorObject());
    }

    @Override
    public void onTick() {
        super.onTick();
    }

    private void renderPicker(MatrixStack stack, int mouseX, int mouseY, Color color) {
        double cx = x + 6;
        float cy = y + 20;
        double cw = width - 38;
        double ch = height - 20;

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

        spos = Render2DEngine.scrollAnimate(spos, (float) (((cx + 40) + (cw - 40)) - ((cw - 40) - ((cw - 40) * saturation))), .6f);
        bpos = Render2DEngine.scrollAnimate(bpos, (float) (cy + (ch - (ch * brightness))), .6f);
        hpos = Render2DEngine.scrollAnimate(hpos, (float) (cy + (ch - 3 + ((ch - 3) * hue))), .6f);
        apos = Render2DEngine.scrollAnimate(apos, (float) (cy + (ch - 3 - ((ch - 3) * (alpha / 255f)))), .6f);

        Color colorA = Color.getHSBColor(hue, 0.0F, 1.0F), colorB = Color.getHSBColor(hue, 1.0F, 1.0F);
        Color colorC = new Color(0, 0, 0, 0), colorD = new Color(0, 0, 0);

        Render2DEngine.horizontalGradient(stack, (float) (cx + 40), cy, (float) (cx + cw), (float) (cy + ch), colorA, colorB);
        Render2DEngine.verticalGradient(stack, (float) (cx + 40), cy, (float) (cx + cw), (float) (cy + ch), colorC, colorD);

        for (float i = 1f; i < ch - 2f; i += 1f) {
            float curHue = (float) (1f / (ch / i));
            Render2DEngine.drawRect(stack, (float) (cx + cw + 4), cy + i, 8, 1, Color.getHSBColor(curHue, 1f, 1f));
        }

        Render2DEngine.drawRect(stack, (float) (cx + cw + 17), cy + 1, 8, (float) (ch - 3), new Color(-1));

        Render2DEngine.verticalGradient(stack, (float) (cx + cw + 17), (float) (cy + 0.8), (float) (cx + cw + 25), (float) (cy + ch - 2), new Color(color.getRed(), color.getGreen(), color.getBlue(), 255), new Color(0, 0, 0, 0));

        Render2DEngine.drawRect(stack, (float) (cx + cw + 3), hpos + 0.5f, 10, 1, Color.WHITE);
        Render2DEngine.drawRect(stack, (float) (cx + cw + 16), apos + 0.5f, 10, 1, Color.WHITE);
        Render2DEngine.drawRound(stack, spos, bpos, 3, 3, 1.5f, new Color(-1));

        Color value = Color.getHSBColor(hue, saturation, brightness);

        if (sbfocused) {
            saturation = (float) (MathUtility.clamp(mouseX - (cx + 40), 0f, cw - 40) / (cw - 40));

            brightness = (float) ((ch - MathUtility.clamp((mouseY - cy), 0, ch)) / ch);
            value = Color.getHSBColor(hue, saturation, brightness);
            setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
        }

        if (hfocused) {
            hue = (float) -((ch - MathUtility.clamp(mouseY - cy, 0, (float) ch)) / ch);
            value = Color.getHSBColor(hue, saturation, brightness);
            setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
        }

        if (afocused) {
            alpha = (int) (((ch - MathUtility.clamp(mouseY - cy, 0, (float) ch)) / ch) * 255);
            setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha));
        }

        rainbow_focused = Render2DEngine.isHovered(mouseX, mouseY, getX(), cy, 40, 10);
        copy_focused = Render2DEngine.isHovered(mouseX, mouseY, getX(), cy + 13, 40, 10);
        paste_focused = Render2DEngine.isHovered(mouseX, mouseY, getX(), cy + 26, 40, 10);

        Render2DEngine.drawRound(stack, getX(), cy, 40, 10, 2f, getColorSetting().isRainbow() ? new Color(86, 63, 105, 250) : (rainbow_focused ? new Color(66, 48, 80, 250) : new Color(50, 35, 60, 250)));
        Render2DEngine.drawRound(stack, getX(), cy + 13, 40, 10, 2f, copy_focused ? new Color(66, 48, 80, 250) : new Color(50, 35, 60, 250));
        Render2DEngine.drawRound(stack, getX(), cy + 26, 40, 9.5f, 2f, paste_focused ? new Color(66, 48, 80, 250) : new Color(50, 35, 60, 250));

        FontRenderers.modules.drawCenteredString(stack, "rainbow", getX() + 20, cy + 3, rainbow_focused ? -1 : (getColorSetting().isRainbow() ? getColorSetting().getColor() : new Color(0xB5FFFFFF, true).getRGB()));
        FontRenderers.modules.drawCenteredString(stack, "copy", getX() + 20, cy + 15.5f, copy_focused ? -1 : new Color(0xB5FFFFFF, true).getRGB());
        FontRenderers.modules.drawCenteredString(stack, "paste", getX() + 20, cy + 28.5f, paste_focused ? -1 : new Color(0xB5FFFFFF, true).getRGB());
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
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        double cx = x + 4;
        double cy = y + 21;
        double cw = width - 34;
        double ch = height - 20;

        if (Render2DEngine.isHovered(mouseX, mouseY, (x + width - 20), (y + 5), 14, 6))
            open = !open;

        if (!open)
            return;

        if (Render2DEngine.isHovered(mouseX, mouseY, cx + cw + 17, cy, 8, ch) && button == 0)
            afocused = true;

        else if (Render2DEngine.isHovered(mouseX, mouseY, cx + cw + 4, cy, 8, ch) && button == 0)
            hfocused = true;

        else if (Render2DEngine.isHovered(mouseX, mouseY, cx + 40, cy, cw - 40, ch) && button == 0)
            sbfocused = true;


        if (rainbow_focused) getColorSetting().setRainbow(!getColorSetting().isRainbow());
        if (copy_focused) ThunderHack.copy_color = getColorSetting().getColorObject();
        if (paste_focused)
            setColor(ThunderHack.copy_color == null ? getColorSetting().getColorObject() : ThunderHack.copy_color);
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

    public boolean isOpen() {
        return open;
    }

}

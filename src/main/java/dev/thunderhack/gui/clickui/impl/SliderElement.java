package dev.thunderhack.gui.clickui.impl;

import dev.thunderhack.ThunderHack;
import dev.thunderhack.gui.font.FontRenderers;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.math.MathUtility;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import dev.thunderhack.gui.clickui.AbstractElement;

import java.awt.*;
import java.util.Objects;

public class SliderElement extends AbstractElement {

    private float animation;
    private double stranimation;
    private boolean dragging;

    private final float min;
    private final float max;

    public SliderElement(Setting setting) {
        super(setting);
        this.min = ((Number) setting.getMin()).floatValue();
        this.max = ((Number) setting.getMax()).floatValue();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        double currentPos = (((Number) setting.getValue()).floatValue() - min) / (max - min);
        stranimation = stranimation + (((Number) setting.getValue()).floatValue() * 100 / 100 - stranimation) / 2.0D;
        animation = Render2DEngine.scrollAnimate(animation, (float) currentPos, .5f);

        MatrixStack matrixStack = context.getMatrices();

        String value = "";

        if (setting.getValue() instanceof Float)
            value = String.valueOf(MathUtility.round((Float) setting.getValue(), 2));

        if (setting.getValue() instanceof Integer)
            value = String.valueOf(MathUtility.round((Integer) setting.getValue(), 2));


        FontRenderers.settings.drawString(matrixStack, setting.getName(), (int) (x + 6), (int) (y + 4), new Color(-1).getRGB());

        if (!listening) {
            FontRenderers.getSettingsRenderer().drawString(matrixStack, value, (int) (x + width - 6 - FontRenderers.getSettingsRenderer().getStringWidth(value)), (int) y + 5, new Color(-1).getRGB());
        } else {
            if (Objects.equals(Stringnumber, ""))
                FontRenderers.getSettingsRenderer().drawString(matrixStack, "...", (int) (x + width - 6 - FontRenderers.getSettingsRenderer().getStringWidth(value)), (int) y + 5, new Color(-1).getRGB());
            else
                FontRenderers.getSettingsRenderer().drawString(matrixStack, Stringnumber, (int) (x + width - 6 - FontRenderers.getSettingsRenderer().getStringWidth(value)), (int) y + 5, new Color(-1).getRGB());
        }

        Render2DEngine.drawRound(matrixStack, (float) (x + 6), (float) (y + height - 6), (float) (width - 12), 1, 0.5f, new Color(0xff0E0E0E));
        Render2DEngine.drawRound(matrixStack, (float) (x + 6), (float) (y + height - 6), (float) ((width - 12) * animation), 1, 0.5f, new Color(0xFFE1E1E1));
        Render2DEngine.drawRound(matrixStack, (float) ((x + 6 + (width - 16) * animation)), (float) (y + height - 7.5f), 4, 4, 1.5f, new Color(0xFFE1E1E1));

        animation = MathUtility.clamp(animation, 0, 1);

        if (dragging)
            setValue(mouseX, x + 7, width - 14);
    }

    private void setValue(int mouseX, double x, double width) {
        double diff = ((Number) setting.getMax()).floatValue() - ((Number) setting.getMin()).floatValue();
        double percentBar = MathHelper.clamp(((float) mouseX - x) / width, 0.0, 1.0);
        double value = ((Number) setting.getMin()).floatValue() + percentBar * diff;

        if (this.setting.getValue() instanceof Float) {
            this.setting.setValue((float) value);
        } else if (this.setting.getValue() instanceof Integer) {
            this.setting.setValue((int) value);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && hovered) {
            this.dragging = true;
        } else if (hovered) {
            Stringnumber = "";
            this.listening = true;
        }
        if (listening)
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.Sliders;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        this.dragging = false;
    }

    public boolean listening;
    public String Stringnumber = "";

    @Override
    public void keyTyped(int keyCode) {
        if (ThunderHack.currentKeyListener != ThunderHack.KeyListening.Sliders)
            return;

        if (this.listening) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    listening = false;
                    Stringnumber = "";
                    return;
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    try {
                        this.searchNumber();

                    } catch (Exception e) {
                        Stringnumber = "";
                        listening = false;
                    }
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    this.Stringnumber = removeLastChar(this.Stringnumber);
                    return;
                }
            }

            this.Stringnumber = this.Stringnumber + GLFW.glfwGetKeyName(keyCode, 0);
        }
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    private void searchNumber() {
        if (this.setting.getValue() instanceof Float) {
            this.setting.setValue(Float.valueOf(Stringnumber));
            Stringnumber = "";
            listening = false;
        } else if (this.setting.getValue() instanceof Integer) {
            this.setting.setValue(Integer.valueOf(Stringnumber));
            Stringnumber = "";
            listening = false;
        }
    }
}
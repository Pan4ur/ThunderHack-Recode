package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.ClickGui;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.Objects;

public class SliderElement extends AbstractElement {

    private final float min, max;
    private float animation;
    private boolean dragging, listening;
    public String Stringnumber = "";

    public SliderElement(Setting setting, boolean small) {
        super(setting, small);
        min = ((Number) setting.getMin()).floatValue();
        max = ((Number) setting.getMax()).floatValue();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        animation = Render2DEngine.scrollAnimate(animation, (((Number) setting.getValue()).floatValue() - min) / (max - min), 0.9f);

        MatrixStack matrixStack = context.getMatrices();

        if(setting.parent != null)
            Render2DEngine.drawRect(context.getMatrices(), (float) x + 4, (float) y, 1f, 18, ClickGui.getInstance().getColor(1));

        if(!isSmall()) {
            FontRenderers.settings.drawString(matrixStack, setting.getName(), (setting.parent != null ? 2f : 0f) + x + 6, y + 4, new Color(-1).getRGB());
            FontRenderers.settings.drawString(matrixStack, listening ? (Objects.equals(Stringnumber, "") ? "..." : Stringnumber) : setting.getValue() + "", (int) (x + width - 6 - FontRenderers.getSettingsRenderer().getStringWidth(setting.getValue() + "")), y + 5, new Color(-1).getRGB());
        } else {
            FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getName(), (setting.parent != null ? 2f : 0f) + x + 6, y + 4, new Color(-1).getRGB());
            FontRenderers.sf_medium_mini.drawString(matrixStack, listening ? (Objects.equals(Stringnumber, "") ? "..." : Stringnumber) : setting.getValue() + "", (int) (x + width - 6 - FontRenderers.sf_medium_mini.getStringWidth(setting.getValue() + "")), y + 5, new Color(-1).getRGB());
        }

        Render2DEngine.drawRound(matrixStack, (float) (x + 6), (float) (y + height - 6), (float) (width - 12), 1, 0.5f, new Color(0xff0E0E0E));
        Render2DEngine.drawRound(matrixStack, (float) (x + 6), (float) (y + height - 6), (float) ((width - 12) * animation), 1, 0.5f, new Color(0xFFE1E1E1));
        Render2DEngine.drawRound(matrixStack, (float) ((x + 6 + (width - 16) * animation)), (float) (y + height - 7.5f), 4, 4, 1.5f, new Color(0xFFE1E1E1));

        animation = MathUtility.clamp(animation, 0, 1);

        if (dragging)
            setValue(mouseX, x + 7, width - 14);
    }

    private void setValue(int mouseX, double x, double width) {
        float value = Render2DEngine.interpolateFloat(((Number) setting.getMin()).floatValue(), ((Number) setting.getMax()).floatValue(), MathHelper.clamp(((float) mouseX - x) / width, 0.0, 1.0));
        if (setting.getValue() instanceof Float) {
            setting.setValue(MathUtility.round2(value));
        } else if (setting.getValue() instanceof Integer) {
            setting.setValue((int) value);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && hovered) {
            dragging = true;
        } else if (hovered) {
            Stringnumber = "";
            listening = true;
        }
        if (listening)
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.Sliders;
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
    }

    @Override
    public void keyTyped(int keyCode) {
        if (ThunderHack.currentKeyListener != ThunderHack.KeyListening.Sliders)
            return;

        if (listening) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    listening = false;
                    Stringnumber = "";
                    return;
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    try {
                        searchNumber();
                    } catch (Exception e) {
                        Stringnumber = "";
                        listening = false;
                    }
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    Stringnumber = removeLastChar(Stringnumber);
                    return;
                }

                case GLFW.GLFW_KEY_DELETE -> {
                    Stringnumber = "";
                    listening = false;
                    return;
                }
            }
            Stringnumber = Stringnumber + GLFW.glfwGetKeyName(keyCode, 0);
        }
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    private void searchNumber() {
        if (setting.getValue() instanceof Float) {
            setting.setValue(Float.valueOf(Stringnumber));
            Stringnumber = "";
            listening = false;
        } else if (setting.getValue() instanceof Integer) {
            setting.setValue(Integer.valueOf(Stringnumber));
            Stringnumber = "";
            listening = false;
        }
    }
}
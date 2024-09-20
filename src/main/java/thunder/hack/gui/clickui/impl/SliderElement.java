package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.Objects;

import static thunder.hack.core.manager.IManager.mc;

public class SliderElement extends AbstractElement {
    private final float min, max;
    private float animation, prevValue;
    private boolean dragging, listening;
    public String Stringnumber = "";

    public SliderElement(Setting setting) {
        super(setting);
        min = ((Number) setting.getMin()).floatValue();
        max = ((Number) setting.getMax()).floatValue();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        animation = Render2DEngine.scrollAnimate(animation, (((Number) setting.getValue()).floatValue() - min) / (max - min), 0.4f);

        MatrixStack matrixStack = context.getMatrices();

        if (setting.group != null)
            Render2DEngine.drawRect(context.getMatrices(), x + 4, y, 1f, 18, HudEditor.getColor(1));

        if (!dragging) {
            FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getName(), (setting.group != null ? 2f : 0f) + x + 6, y + 4, new Color(-1).getRGB());
            FontRenderers.sf_medium_mini.drawString(matrixStack, listening ? (Objects.equals(Stringnumber, "") ? "..." : Stringnumber) : setting.getValue() + "",
                    (int) (x + width - 6 - FontRenderers.sf_medium_mini.getStringWidth(listening ? (Objects.equals(Stringnumber, "") ? "..." : Stringnumber) : setting.getValue() + "")), y + 5, new Color(-1).getRGB());
        } else {
            if (animation > 0.2f)
                FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getMin() + "", x + 6, y + 4, new Color(-1).getRGB());
            if (animation < 0.8f)
                FontRenderers.sf_medium_mini.drawString(matrixStack, setting.getMax() + "", x + width - FontRenderers.sf_medium_mini.getStringWidth(setting.getMax() + "") - 6, y + 4, new Color(-1).getRGB());

            FontRenderers.sf_medium_mini.drawString(matrixStack, listening ? (Objects.equals(Stringnumber, "") ? "..." : Stringnumber) : setting.getValue() + "", animation > 0.2f ? animation < 0.8f ? x + 6 + (width - 14) * animation - FontRenderers.sf_medium_mini.getStringWidth(setting.getValue() + "") / 2f : x + width - FontRenderers.sf_medium_mini.getStringWidth(setting.getMax() + "") - 6 : x + 6, y + 4, new Color(-1).getRGB());
        }

        Render2DEngine.drawRect(matrixStack, x + 6, y + height - 6, width - 12, 2, new Color(0x28FFFFFF, true));
        Render2DEngine.draw2DGradientRect(matrixStack, x + 6, y + height - 6, x + 6 + (width - 12) * animation, y + height - 4, HudEditor.getColor(180), HudEditor.getColor(180), HudEditor.getColor(0), HudEditor.getColor(0));
        Render2DEngine.drawRect(matrixStack, (x + 6 + (width - 14) * animation), y + height - 7.5f, 2, 5, new Color(0xFFE1E1E1));

        animation = MathUtility.clamp(animation, 0, 1);

        if (dragging)
            setValue(mouseX, x + 7, width - 14);

        if (Render2DEngine.isHovered(mouseX, mouseY, (x + 6), y + height - 7, width - 12, 3)) {
            if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
                GLFW.glfwSetCursor(mc.getWindow().getHandle(),
                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
            }
            ClickGUI.anyHovered = true;
        }
    }

    private void setValue(int mouseX, double x, double width) {
        float value = Render2DEngine.interpolateFloat(((Number) setting.getMin()).floatValue(), ((Number) setting.getMax()).floatValue(), MathHelper.clamp(((float) mouseX - x) / width, 0.0, 1.0));
        if (setting.getValue() instanceof Float) {
            setting.setValue(MathUtility.round2(value));
        } else if (setting.getValue() instanceof Integer) {
            setting.setValue((int) value);
        }

        if (value != prevValue)
            Managers.SOUND.playSlider();

        prevValue = value;
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
        }
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (StringHelper.isValidChar(key)) {
            String k = (key == '-' ? "-" : ".");
            try {
                k = String.valueOf(Integer.parseInt(String.valueOf(key)));
            } catch (Exception ignored) {
            }
            Stringnumber = Stringnumber + k;
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
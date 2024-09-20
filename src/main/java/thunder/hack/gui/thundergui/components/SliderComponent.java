package thunder.hack.gui.thundergui.components;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.Objects;

public class SliderComponent extends SettingElement {
    private final float min;
    private final float max;
    public boolean listening;
    public String Stringnumber = "";
    private float animation;
    private double stranimation;
    private boolean dragging;

    public SliderComponent(Setting setting) {
        super(setting);
        this.min = ((Number) setting.getMin()).floatValue();
        this.max = ((Number) setting.getMax()).floatValue();
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && !str.isEmpty()) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }

        FontRenderers.modules.drawString(stack, getSetting().getName(), getX(), getY() + 5, isHovered() ? -1 : new Color(0xB0FFFFFF, true).getRGB());

        double currentPos = (((Number) setting.getValue()).floatValue() - min) / (max - min);
        stranimation = stranimation + (((Number) setting.getValue()).floatValue() * 100 / 100 - stranimation) / 2.0D;
        animation = Render2DEngine.scrollAnimate(animation, (float) currentPos, .5f);

        Color color = new Color(0xFFE1E1E1);
        Render2DEngine.drawRound(stack, x + 54, y + height - 8, (float) (90), 1, 0.5f, new Color(0xff0E0E0E));
        Render2DEngine.drawRound(stack, x + 54, y + height - 8, (90) * animation, 1, 0.5f, color);
        Render2DEngine.drawRound(stack, (x + 52 + (90) * animation), y + height - 9.5f, (float) 4, 4, 1.5f, color);

        if (mouseX > x + 154 && mouseX < x + 176 && mouseY > y + height - 11 && mouseY < y + height - 4) {
            Render2DEngine.drawRound(stack, x + 154, y + height - 11, 22, 7, 0.5f, new Color(82, 57, 100, 178));
        } else {
            Render2DEngine.drawRound(stack, x + 154, y + height - 11, 22, 7, 0.5f, new Color(50, 35, 60, 178));
        }

        if (!listening) {
            if (setting.getValue() instanceof Float)
                FontRenderers.modules.drawString(stack, String.valueOf(MathUtility.round((Float) setting.getValue(), 2)), x + 156, y + height - 9, new Color(0xBAFFFFFF, true).getRGB());
            if (setting.getValue() instanceof Integer)
                FontRenderers.modules.drawString(stack, String.valueOf(setting.getValue()), x + 156, y + height - 9, new Color(0xBAFFFFFF, true).getRGB());
        } else {
            if (Objects.equals(Stringnumber, "")) {
                FontRenderers.modules.drawString(stack, "...", x + 156, y + height - 9, new Color(0xBAFFFFFF, true).getRGB());
            } else {
                FontRenderers.modules.drawString(stack, Stringnumber, x + 156, y + height - 9, new Color(0xBAFFFFFF, true).getRGB());
            }
        }

        animation = MathUtility.clamp(animation, 0, 1);

        if (dragging)
            setValue(mouseX, x + 54, (90));

    }

    private void setValue(int mouseX, double x, double width) {
        double diff = ((Number) setting.getMax()).floatValue() - ((Number) setting.getMin()).floatValue();
        double percentBar = MathHelper.clamp((mouseX - x) / width, 0.0, 1.0);
        double value = ((Number) setting.getMin()).floatValue() + percentBar * diff;

        if (this.setting.getValue() instanceof Float) {
            this.setting.setValue((float) value);
        } else if (this.setting.getValue() instanceof Integer) {
            this.setting.setValue((int) value);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if ((getY() > ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height) || getY() < ThunderGui.getInstance().main_posY) {
            return;
        }
        if (mouseX > x + 154 && mouseX < x + 176 && mouseY > y + height - 11 && mouseY < y + height - 4) {
            Stringnumber = "";
            this.listening = true;
        } else {
            if (button == 0 && hovered) {
                this.dragging = true;
            }
        }

        if (listening)
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.Sliders;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        this.dragging = false;
    }

    @Override
    public void resetAnimation() {
        dragging = false;
        animation = 0f;
        stranimation = 0;
    }

    @Override
    public void keyTyped(String typedChar, int keyCode) {
        if (ThunderHack.currentKeyListener != ThunderHack.KeyListening.Sliders)
            return;

        if (this.listening) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE: {
                    listening = false;
                    Stringnumber = "";
                    return;
                }
                case GLFW.GLFW_KEY_ENTER: {
                    try {
                        searchNumber();
                    } catch (Exception e) {
                        Stringnumber = "";
                        listening = false;
                    }
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE: {
                    Stringnumber = removeLastChar(Stringnumber);
                    return;
                }
            }
            Stringnumber = Stringnumber + GLFW.glfwGetKeyName(keyCode, 0);
        }
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

    @Override
    public void checkMouseWheel(float value) {
        super.checkMouseWheel(value);
        if (isHovered()) {
            ThunderGui.scroll_lock = true;
        } else {
            return;
        }
        if (value < 0) {
            if (this.setting.getValue() instanceof Float) {
                this.setting.setValue((Float) setting.getValue() + 0.01f);
            } else if (this.setting.getValue() instanceof Integer) {
                this.setting.setValue((Integer) setting.getValue() + 1);
            }
        } else if (value > 0) {
            if (this.setting.getValue() instanceof Float) {
                this.setting.setValue((Float) setting.getValue() - 0.01f);
            } else if (this.setting.getValue() instanceof Integer) {
                this.setting.setValue((Integer) setting.getValue() - 1);
            }
        }
    }
}
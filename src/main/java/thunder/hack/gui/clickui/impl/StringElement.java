package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.modules.Module.mc;

public class StringElement extends AbstractElement {
    public StringElement(Setting setting, boolean small) {
        super(setting, small);
    }

    public boolean listening;
    private String currentString = "";

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if(!isSmall()) {
            Render2DEngine.drawRect(context.getMatrices(), (float) getX() + 5,(float) getY() + 2, (float) (getWidth() - 11f), 10, new Color(0x94000000, true));
            FontRenderers.getSettingsRenderer().drawString(context.getMatrices(), listening ? currentString + (mc.player == null || mc.player.age % 5 == 0 ? "_" : "") : (String) setting.getValue(), x + 6, y + height / 2, -1);
        } else {
            Render2DEngine.drawRect(context.getMatrices(), (float) getX() + 5,(float) getY() + 2, (float) (getWidth() - 11f), 10, new Color(0x94000000, true));
            FontRenderers.sf_medium_mini.drawString(context.getMatrices(), listening ? currentString + (mc.player == null || mc.player.age % 5 == 0 ? "_" : "") : (String) setting.getValue(), x + 6, y + height / 2, -1);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered && button == 0)
            listening = !listening;
        if (listening) {
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.Strings;
            currentString = (String) setting.getValue();
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void keyTyped(int keyCode) {
        if (ThunderHack.currentKeyListener != ThunderHack.KeyListening.Strings)
            return;

        if (listening) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    return;
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    setting.setValue(currentString == null || currentString.isEmpty() ? setting.getDefaultValue() : currentString);
                    currentString = "";
                    listening = !listening;
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    currentString = SliderElement.removeLastChar(currentString);
                    return;
                }
                case GLFW.GLFW_KEY_SPACE -> {
                    currentString = currentString + " ";
                    return;
                }
            }
            if (GLFW.glfwGetKeyName(keyCode, 0) == null)
                return;

            currentString = currentString + (
                    (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT ) || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.GLFW_KEY_RIGHT_SHIFT)) && GLFW.glfwGetKeyName(keyCode, 0) != null ?
                    GLFW.glfwGetKeyName(keyCode, 0).toUpperCase() : GLFW.glfwGetKeyName(keyCode, 0));
        }
    }
}

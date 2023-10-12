package dev.thunderhack.gui.clickui.impl;

import dev.thunderhack.ThunderHack;
import dev.thunderhack.gui.font.FontRenderers;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import dev.thunderhack.gui.clickui.AbstractElement;

public class StringElement extends AbstractElement {
    public StringElement(Setting setting) {
        super(setting);
    }

    public boolean listening;
    private String currentString;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        FontRenderers.getSettingsRenderer().drawString(context.getMatrices(), listening ? currentString + (Module.mc.player == null || Module.mc.player.age % 5 == 0 ? "_" : "") : (String) setting.getValue(), x + 6, y + height / 2, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered && button == 0)
            listening = !listening;
        if (listening)
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.Strings;
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
            if (GLFW.glfwGetKeyName(keyCode, 0) == null) return;
            currentString = currentString + GLFW.glfwGetKeyName(keyCode, 0);
        }
    }
}

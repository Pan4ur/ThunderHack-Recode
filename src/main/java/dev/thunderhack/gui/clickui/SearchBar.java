package dev.thunderhack.gui.clickui;

import dev.thunderhack.ThunderHack;
import dev.thunderhack.gui.font.FontRenderers;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.ClickGui;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import dev.thunderhack.gui.clickui.impl.SliderElement;

import java.awt.*;

public class SearchBar extends AbstractButton {
    public static String moduleName = "";
    public static boolean listening;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, Color color) {
        super.render(context, mouseX, mouseY, delta, color);

        boolean isHovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);

        Render2DEngine.drawRoundDoubleColor(context.getMatrices(), x + 4, y + 1f, width - 8, height - 2, 3f, ClickGui.getInstance().getColor(200), ClickGui.getInstance().getColor(0));
        Render2DEngine.drawRound(context.getMatrices(), (float) (x + 4.5f), (float) (y + 1.5f), (float) (width - 9), (float) (height - 3), 3f, Render2DEngine.injectAlpha(ClickGui.getInstance().plateColor.getValue().getColorObject(), isHovered ? 220 : 255));
        if (!listening) FontRenderers.sf_medium.drawGradientString(context.getMatrices(), "Search...", (float) (x + 7f), (float) (y + 7f), 2, false);
        else FontRenderers.sf_medium.drawGradientString(context.getMatrices(), moduleName + (Module.mc.player == null || ((Module.mc.player.age / 10) % 2 == 0) ? " " : "_"), (float) (x + 7f), (float) (y + 7f), 2, false);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        boolean isHovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);

        if(isHovered) listening = true;
        else {
            moduleName = "";
            listening = false;
        }

        if(listening) ThunderHack.currentKeyListener = ThunderHack.KeyListening.Search;
    }

    @Override
    public void keyTyped(int keyCode) {
        super.keyTyped(keyCode);

        if(ThunderHack.currentKeyListener != ThunderHack.KeyListening.Search)
            return;

        if (listening) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> {
                    listening = false;
                    moduleName = "";
                    return;
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    return;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    moduleName = SliderElement.removeLastChar(moduleName);
                    return;
                }
                case GLFW.GLFW_KEY_SPACE -> {
                    moduleName = moduleName + " ";
                    return;
                }
            }
            if(GLFW.glfwGetKeyName(keyCode, 0) == null) return;
            moduleName = moduleName + GLFW.glfwGetKeyName(keyCode, 0);
        }
    }
}
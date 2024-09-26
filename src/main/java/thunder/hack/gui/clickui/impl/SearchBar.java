package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.gui.clickui.AbstractButton;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

import static thunder.hack.features.modules.Module.mc;

public class SearchBar extends AbstractButton {
    public static String moduleName = "";
    public static boolean listening;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        Render2DEngine.drawGuiBase(context.getMatrices(), x + 4, y + 1f, width - 8, height - 2, 1f, Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height) ? 0.8f : 0f);
        if (!listening)
            FontRenderers.sf_medium.drawGradientString(context.getMatrices(), "Search...", x + 7f, y + height / 2f - 3, 2);
        else
            FontRenderers.sf_medium.drawGradientString(context.getMatrices(), moduleName + (mc.player == null || ((mc.player.age / 10) % 2 == 0) ? " " : "_"), x + 7f, y + 5f, 2);

        if (Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height)) {
            if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
                GLFW.glfwSetCursor(mc.getWindow().getHandle(),
                        GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR));
            }
            ClickGUI.anyHovered = true;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        boolean isHovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);

        if (isHovered) listening = true;
        else {
            moduleName = "";
            listening = false;
        }

        if (listening) ThunderHack.currentKeyListener = ThunderHack.KeyListening.Search;
    }

    @Override
    public void charTyped(char key, int keyCode) {
        if (StringHelper.isValidChar(key) && listening) {
            moduleName = moduleName + key;
        }
    }

    @Override
    public void keyTyped(int keyCode) {
        super.keyTyped(keyCode);

        if (keyCode == GLFW.GLFW_KEY_F && (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL))) {
            listening = !listening;
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.Search;
            return;
        }

        if (ThunderHack.currentKeyListener != ThunderHack.KeyListening.Search)
            return;

        if (listening) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER -> {
                    listening = false;
                    moduleName = "";
                }
                case GLFW.GLFW_KEY_BACKSPACE -> moduleName = SliderElement.removeLastChar(moduleName);
                case GLFW.GLFW_KEY_SPACE -> moduleName = moduleName + " ";
            }
        }
    }
}
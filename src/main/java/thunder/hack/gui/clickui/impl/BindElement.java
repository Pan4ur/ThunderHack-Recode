package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class BindElement extends AbstractElement {
    public BindElement(Setting setting) {
        super(setting);
    }

    public boolean isListening;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        FontRenderers.sf_medium_mini.drawString(context.getMatrices(), setting.getName(), getX() + 6, (getY() + height / 2 - 3) + 2, new Color(-1).getRGB());

        float tWidth = FontRenderers.sf_medium_mini.getStringWidth(isListening ? "..." : (((Bind) setting.getValue()).getBind()));

        Render2DEngine.drawRect(context.getMatrices(), getX() + (getWidth() - tWidth - 11), getY() + 2, tWidth + 4, 10, new Color(0x94000000, true));
        FontRenderers.sf_medium_mini.drawString(context.getMatrices(), isListening ? "..." : (((Bind) setting.getValue()).getBind()), getX() + (getWidth() - tWidth - 9), (getY() + height / 2 - 1), new Color(-1).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isListening) {
            Bind b = new Bind(button, true, false);
            setting.setValue(b);
            isListening = false;
        }
        if (hovered && button == 0) isListening = !isListening;

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void keyTyped(int keyCode) {
        if (isListening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                Bind b = new Bind(-1, false, false);
                setting.setValue(b);
            } else {
                Bind b = new Bind(keyCode, false, false);
                setting.setValue(b);
            }
            isListening = false;
        }
    }
}

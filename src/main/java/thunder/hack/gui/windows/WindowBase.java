package thunder.hack.gui.windows;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

import static thunder.hack.core.IManager.mc;

public class WindowBase {
    private float x, y, width, height, dragX, dragY;
    private final String name;
    private boolean mouseState, mouseButton;

    public WindowBase(float x, float y, float width, float height, String name) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        this.name = name;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Render2DEngine.drawHudBase(context.getMatrices(), x, y, width, height, 1, false);
        Render2DEngine.horizontalGradient(context.getMatrices(), x + 2, y + 16f, x + 2 + width / 2f - 2, y + 16.5f, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), x + 2 + width / 2f - 2, y + 16f, x + 2 + width - 4, y + 16.5f, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
        FontRenderers.sf_medium.drawString(context.getMatrices(), name, x + 4, y + 5.5f, -1);
        boolean hover1 = Render2DEngine.isHovered(mouseX, mouseY, x + width - 14, y + 3, 10, 10);
        Render2DEngine.drawRect(context.getMatrices(), x + width - 14, y + 3, 10, 10, hover1 ? new Color(0xC5777777, true) : new Color(0xC5575757, true));
        Render2DEngine.drawLine(x + width - 12, y + 5, x + width - 6, y + 11, Colors.WHITE);
        Render2DEngine.drawLine(x + width - 12, y + 11, x + width - 6, y + 5, Colors.WHITE);
        RenderSystem.disableBlend();

        if (mouseButton && mouseState) {
            setX(Render2DEngine.scrollAnimate((normaliseX() - dragX), getX(), .1f));
            setY(Render2DEngine.scrollAnimate((normaliseY() - dragY), getY(), .1f));
        }

        if (mouseButton) {
            if (!mouseState && Render2DEngine.isHovered(mouseX, mouseY, x, y, width, 10)) {
                dragX = (int) (mouseX - getX());
                dragY = (int) (mouseY - getY());
                mouseState = true;
            }
        } else {
            mouseState = false;
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        mouseButton = true;
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    public void charTyped(char key, int keyCode) {
    }

    public void mouseScrolled(int i) {
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        mouseButton = false;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int normaliseX() {
        return (int) (mc.mouse.getX() / Render3DEngine.getScaleFactor());
    }

    public int normaliseY() {
        return (int) (mc.mouse.getY() / Render3DEngine.getScaleFactor());
    }
}

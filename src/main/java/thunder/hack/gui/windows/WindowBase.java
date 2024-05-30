package thunder.hack.gui.windows;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import thunder.hack.cmd.Command;
import thunder.hack.core.impl.CombatManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

import static thunder.hack.core.IManager.mc;

public class WindowBase {
    private float x, y, width, height, dragX, dragY, scrollOffset, prevScrollOffset;
    private final String name;
    private boolean mouseState, dragging, hoveringWindow, scaling;

    protected WindowBase(float x, float y, float width, float height, String name) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        this.name = name;
    }

    protected void render(DrawContext context, int mouseX, int mouseY) {
        prevScrollOffset = AnimationUtility.fast(prevScrollOffset, scrollOffset, 12);
        Color color2 = new Color(0xC55B5B5B, true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Render2DEngine.drawHudBase(context.getMatrices(), x, y, width, height, 1, false);
        Render2DEngine.drawRectWithOutline(context.getMatrices(), x + 0.5f, y, width - 1, 16, new Color(0x5F000000, true), color2);
        Render2DEngine.horizontalGradient(context.getMatrices(), x + 2, y + 16f, x + 2 + width / 2f - 2, y + 16.5f, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), x + 2 + width / 2f - 2, y + 16f, x + 2 + width - 4, y + 16.5f, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
        FontRenderers.sf_medium.drawString(context.getMatrices(), name, x + 4, y + 5.5f, -1);
        boolean hover1 = Render2DEngine.isHovered(mouseX, mouseY, x + width - 14, y + 3, 10, 10);
        Render2DEngine.drawRectWithOutline(context.getMatrices(), x + width - 14, y + 3, 10, 10, hover1 ? new Color(0xC5777777, true) : new Color(0xC5575757, true), color2);

        Render2DEngine.drawLine(x + width - 12, y + 5, x + width - 6, y + 11, Colors.WHITE);
        Render2DEngine.drawLine(x + width - 12, y + 11, x + width - 6, y + 5, Colors.WHITE);
        RenderSystem.disableBlend();

        //  boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, x + width - 14, y + 3, 10, 10);
        //  Render2DEngine.drawRect(context.getMatrices(), x + width - 7, y + 23, 4, height - 20, hover2 ? new Color(0xC5777777, true) : new Color(0xC5575757, true));

        if (dragging) {
            setX(Render2DEngine.scrollAnimate((normaliseX() - dragX), getX(), .15f));
            setY(Render2DEngine.scrollAnimate((normaliseY() - dragY), getY(), .15f));
        }

        if (scaling) {
            setWidth(Math.max(Render2DEngine.scrollAnimate((normaliseX() - dragX), getWidth(), .15f), 150));
            setHeight(Math.max(Render2DEngine.scrollAnimate((normaliseY() - dragY), getHeight(), .15f), 150));
        }

        hoveringWindow = Render2DEngine.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight());

        Render2DEngine.drawLine(getX() + getWidth() - 10,getY() + getHeight() - 3, getX() + getWidth() - 3, getY() + getHeight() - 10, color2.getRGB());
        Render2DEngine.drawLine(getX() + getWidth() - 5,getY() + getHeight() - 3, getX() + getWidth() - 3, getY() + getHeight() - 5, color2.getRGB());

    }

    protected void mouseClicked(double mouseX, double mouseY, int button) {
        if (Render2DEngine.isHovered(mouseX, mouseY, x, y, width, 10)) {
            if (WindowsScreen.draggingWindow == null)
                dragging = true;

            if (WindowsScreen.draggingWindow == null)
                WindowsScreen.draggingWindow = this;

            WindowsScreen.lastClickedWindow = this;
            dragX = (int) (mouseX - getX());
            dragY = (int) (mouseY - getY());
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, x + width - 10, y + height - 10, 10, 10)) {
            WindowsScreen.lastClickedWindow = this;
            dragX = (int) (mouseX - getWidth());
            dragY = (int) (mouseY - getHeight());
            scaling = true;
        }
    }

    protected void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    protected void charTyped(char key, int keyCode) {
    }

    protected void mouseScrolled(int i) {
        if (hoveringWindow) {
            scrollOffset += i * 2;
            scrollOffset = Math.min(scrollOffset, 0);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        scaling = false;
        WindowsScreen.draggingWindow = null;
    }

    protected float getX() {
        return x;
    }

    protected void setX(float x) {
        this.x = x;
    }

    protected float getY() {
        return y;
    }

    protected void setY(float y) {
        this.y = y;
    }

    protected float getWidth() {
        return width;
    }

    protected void setWidth(float width) {
        this.width = width;
    }

    protected float getHeight() {
        return height;
    }

    protected void setHeight(float height) {
        this.height = height;
    }

    protected int normaliseX() {
        return (int) (mc.mouse.getX() / Render3DEngine.getScaleFactor());
    }

    protected int normaliseY() {
        return (int) (mc.mouse.getY() / Render3DEngine.getScaleFactor());
    }

    protected float getScrollOffset() {
        return prevScrollOffset;
    }

    protected void resetScroll() {
        prevScrollOffset = 0;
        scrollOffset = 0;
    }
}

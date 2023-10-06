package thunder.hack.gui.clickui;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public abstract class AbstractWindow {
    private String name;
    public double animationY;
    protected double prevTargetX;

    protected double x, y, width, height;
    private double prevX, prevY;
    protected boolean hovered;
    public boolean dragging;
    public float moduleOffset;

    private boolean open;

    public AbstractWindow(String name, double x, double y, double width, double height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.open = false;
    }

    public void init() {
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, Color color) {
        hovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);
        animationY = interpolate(y, animationY, 0.05);
        if (this.dragging) {
            prevTargetX = x;
            this.x = this.prevX + mouseX;
            this.y = this.prevY + mouseY;
        } else
            prevTargetX = x;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (this.hovered && button == 0) {
            this.dragging = true;
            this.prevX = this.x - mouseX;
            this.prevY = this.y - mouseY;
        }
    }

    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0)
            this.dragging = false;
    }

    public boolean keyTyped(int keyCode) {
        return true;
    }

    public void onClose() {
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getName() {
        return name;
    }

    public boolean isOpen() {
        return open;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setModuleOffset(float v, float mx, float my) {
        if (Render2DEngine.isHovered(mx, my, x, y, width, height + 1000)) {
            moduleOffset = moduleOffset + v;
        }
    }
}

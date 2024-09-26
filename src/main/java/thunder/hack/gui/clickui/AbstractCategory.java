package thunder.hack.gui.clickui;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.render.Render2DEngine;

public class AbstractCategory {
    private String name;
    public float animationY;
    protected float prevTargetX;

    protected float x, y, width, height, sx, sy;
    private float prevX, prevY;
    protected boolean hovered;
    public boolean dragging;
    public float moduleOffset;

    private boolean open;

    public AbstractCategory(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.open = false;
    }

    public void init() {
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);
        animationY = (float) interpolate(y, animationY, 0.05);
        if (this.dragging) {
            prevTargetX = x;
            this.x = this.prevX + mouseX;
            this.y = this.prevY + mouseY;
        } else prevTargetX = x;
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

    public void charTyped(char key, int modifier) {
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

    public void setModuleOffset(float v, float mx, float my) {
        if (Render2DEngine.isHovered(mx, my, x, y, width, height + 1000)) {
            moduleOffset = moduleOffset + v;
        }
    }

    public void tick() {
    }

    public void hudClicked(Module module) {
    }

    public void savePos() {
        sx = x;
        sy = y;
    }

    public void restorePos() {
        x = sx;
        y = sy;
    }
}

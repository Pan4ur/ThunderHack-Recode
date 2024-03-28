package thunder.hack.gui.clickui;

import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class AbstractButton {
    public float x, y, width, height;
    public float target_offset;
    public float offsetY;
    public void init() {
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
        this.y = y + offsetY;
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

    public void setOffsetY(float offsetY) {
        this.target_offset = offsetY;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
    }

    public void keyTyped(int keyCode) {
    }

    public void onGuiClosed() {
    }

    public void tick() {
    }
}

package thunder.hack.gui.clickui;

import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class AbstractButton {
    public double x, y, width, height;
    public double target_offset;
    public double offsetY;
    public boolean hiden;

    public void setHiden(boolean hiden) {
        this.hiden = hiden;
    }

    public boolean isHiden() {
        return hiden;
    }

    public void init() {
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
        this.y = y + offsetY;
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

    public void setOffsetY(double offsetY) {
        this.target_offset = offsetY;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, Color color) {
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
    }

    public void keyTyped(int keyCode) {
    }

    public void onGuiClosed() {
    }
}

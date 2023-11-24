package thunder.hack.gui.thundergui.components;

import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.io.IOException;

public class SettingElement {
    protected Setting setting;

    protected double x, y, width, height;
    protected double offsetY;

    protected double prev_offsetY;
    protected double scroll_offsetY;
    protected float scroll_animation;


    protected boolean hovered;

    public SettingElement(Setting setting) {
        this.setting = setting;
        scroll_animation = 0;
        prev_offsetY = y;
        scroll_offsetY = 0;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        hovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);
        if (scroll_offsetY != y) {
            scroll_animation = AnimationUtility.fast(scroll_animation, 1, 15f);
            y = (int) Render2DEngine.interpolate(prev_offsetY, scroll_offsetY, scroll_animation);
        }
    }

    public void init() {
    }

    public void onTick() {
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
    }

    public void tick() {
    }

    public boolean isHovered() {
        return hovered;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    public void handleMouseInput() throws IOException {
    }

    public void keyTyped(String chr, int keyCode) {
    }

    public void onClose() {
    }

    public void resetAnimation() {
    }

    public Setting getSetting() {
        return setting;
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
        prev_offsetY = this.y;
        this.scroll_offsetY = y + offsetY;
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
        this.offsetY = offsetY;
    }

    public boolean isVisible() {
        return setting.isVisible();
    }

    public void checkMouseWheel(float dWheel) {
        if (dWheel != 0) {
            scroll_animation = 0;
        }
    }
}

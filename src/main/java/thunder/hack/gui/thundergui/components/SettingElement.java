package thunder.hack.gui.thundergui.components;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.io.IOException;

public class SettingElement {
    protected Setting setting;

    protected float x, y, width, height;
    protected float offsetY;

    protected float prev_offsetY;
    protected float scroll_offsetY;
    protected float scroll_animation;


    protected boolean hovered;

    public SettingElement(Setting setting) {
        this.setting = setting;
        scroll_animation = 1f;
        prev_offsetY = y;
        scroll_offsetY = y;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        hovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);
        if (scroll_offsetY != y) {
            scroll_animation = AnimationUtility.fast(scroll_animation, 1, 5f);
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
        prev_offsetY = this.y;
        this.scroll_offsetY = y + offsetY;
    }

    public void setPrev_offsetY(float y) {
        prev_offsetY = y;
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

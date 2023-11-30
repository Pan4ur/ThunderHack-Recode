package thunder.hack.gui.clickui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import thunder.hack.cmd.Command;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

import static thunder.hack.modules.client.MainSettings.isRu;

public abstract class AbstractElement {
    protected Setting setting;

    protected double x, y, width, height;
    protected double offsetY;

    protected boolean hovered;
    protected boolean small;

    protected int bgcolor = new Color(24, 24, 27).getRGB();

    public AbstractElement(Setting setting,boolean small) {
        this.setting = setting;
        this.small = small;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);
    }

    public void init() {
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_DELETE) && button == 2 && hovered) {
            setting.setValue(setting.getDefaultValue());
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    public void keyTyped(int keyCode) {
    }

    public void onClose() {
    }

    public Setting getSetting() {
        return setting;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y + offsetY;
    }

    public void setWidth(double width) {
        this.width = width;
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

    public boolean isSmall() {
        return small;
    }
}
package thunder.hack.gui.clickui;

import java.awt.Color;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import net.minecraft.client.util.math.MatrixStack;

public abstract class AbstractElement {
	protected Setting setting;

	protected double x, y, width, height;
	protected double offsetY;

	protected boolean hovered;
	
	protected int bgcolor = new Color(24, 24, 27).getRGB();

	public AbstractElement(Setting setting) {
		this.setting = setting;
	}

	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		hovered = Render2DEngine.isHovered(mouseX, mouseY, x, y, width, height);
	}

	public void init() {
	}

	public void mouseClicked(int mouseX, int mouseY, int button) {
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
}
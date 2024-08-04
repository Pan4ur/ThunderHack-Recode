package thunder.hack.gui.windows;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

import static thunder.hack.core.manager.IManager.mc;

public class WindowBase {
    public final Setting<PositionSetting> position;

    private float x;
    private float y;
    private float width;
    private float height;
    private float dragX;
    private float dragY;
    private float scrollOffset;
    private float prevScrollOffset;

    private float maxElementsHeight;
    private final String name;
    private boolean dragging, hoveringWindow, scaling, scrolling;

    private boolean visible = true;
    private final Identifier icon;

    protected WindowBase(float x, float y, float width, float height, String name, Setting<PositionSetting> pos, Identifier icon) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        this.name = name;
        this.position = pos;
        this.icon = icon;
    }

    protected void render(DrawContext context, int mouseX, int mouseY) {
        prevScrollOffset = AnimationUtility.fast(prevScrollOffset, scrollOffset, 12);
        Color color2 = new Color(0xC55B5B5B, true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Render2DEngine.drawHudBase(context.getMatrices(), x, y, width + 10, height, 1, false);
        Render2DEngine.drawRect(context.getMatrices(), x + 0.5f, y, width + 9, 16, new Color(0x5F000000, true));
        Render2DEngine.horizontalGradient(context.getMatrices(), x + 2, y + 16f, x + 2 + width / 2f - 2, y + 16.5f, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), x + 2 + width / 2f - 2, y + 16f, x + 2 + width - 4, y + 16.5f, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
        FontRenderers.sf_medium.drawString(context.getMatrices(), name, x + 4, y + 5.5f, -1);
        boolean hover1 = Render2DEngine.isHovered(mouseX, mouseY, x + width - 4, y + 3, 10, 10);
        Render2DEngine.drawRectWithOutline(context.getMatrices(), x + width - 4, y + 3, 10, 10, hover1 ? new Color(0xC5777777, true) : new Color(0xC5575757, true), color2);

        float ratio = (getHeight() - 35) / maxElementsHeight;

        boolean hover2 = Render2DEngine.isHovered(mouseX, mouseY, x + width, y + 19, 6, getHeight() - 34);
        Render2DEngine.drawRectWithOutline(context.getMatrices(), x + width, y + 19, 6, getHeight() - 34, hover2 ? new Color(0x5F131313, true) : new Color(0x5F000000, true), color2);
        Render2DEngine.drawRect(context.getMatrices(), x + width, Math.max(y + 19 - (scrollOffset * ratio), y + 19), 6, Math.min((getHeight() - 34) * ratio, getHeight() - 34), new Color(0xA1313131, true));

        Render2DEngine.drawLine(x + width - 2, y + 5, x + width + 4, y + 11, Colors.WHITE);
        Render2DEngine.drawLine(x + width - 2, y + 11, x + width + 4, y + 5, Colors.WHITE);
        RenderSystem.disableBlend();

        if (dragging) {
            setX(Render2DEngine.scrollAnimate((normaliseX() - dragX), getX(), .15f));
            setY(Render2DEngine.scrollAnimate((normaliseY() - dragY), getY(), .15f));
            if (position != null) {
                position.getValue().setX(getX() / mc.getWindow().getScaledWidth());
                position.getValue().setY(getY() / mc.getWindow().getScaledHeight());
            }
        }

        if (scaling) {
            setWidth(Math.max(Render2DEngine.scrollAnimate((normaliseX() - dragX), getWidth(), .15f), getMinWidth()));
            setHeight(Math.max(Render2DEngine.scrollAnimate((normaliseY() - dragY), getHeight(), .15f), getMinHeight()));
        }

        if (scrolling) {
            float diff = ((float) mouseY - y - 19) / (getHeight() - 34);
            scrollOffset =  -(diff * maxElementsHeight);
            scrollOffset = MathUtility.clamp(scrollOffset, -maxElementsHeight + (getHeight() - 40), 0);
        }

        hoveringWindow = Render2DEngine.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), getHeight());

        Render2DEngine.drawLine(getX() + getWidth(), getY() + getHeight() - 3, getX() + getWidth() + 7, getY() + getHeight() - 10, color2.getRGB());
        Render2DEngine.drawLine(getX() + getWidth() + 5, getY() + getHeight() - 3, getX() + getWidth() + 7, getY() + getHeight() - 5, color2.getRGB());
    }

    protected void mouseClicked(double mouseX, double mouseY, int button) {

        if (Render2DEngine.isHovered(mouseX, mouseY, x + width - 4, y + 3, 10, 10)) {
            setVisible(false);
            return;
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, x, y, width, 10)) {
            if (WindowsScreen.draggingWindow == null)
                dragging = true;

            if (WindowsScreen.draggingWindow == null)
                WindowsScreen.draggingWindow = this;

            WindowsScreen.lastClickedWindow = this;
            dragX = (int) (mouseX - getX());
            dragY = (int) (mouseY - getY());
            return;
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, x + width, y + height - 10, 10, 10)) {
            WindowsScreen.lastClickedWindow = this;
            dragX = (int) (mouseX - getWidth());
            dragY = (int) (mouseY - getHeight());
            scaling = true;
            return;
        }

        if (Render2DEngine.isHovered(mouseX, mouseY, x + width, y + 19, 6, getHeight() - 34)) {
            WindowsScreen.lastClickedWindow = this;
            dragX = (int) (mouseX - getWidth());
            dragY = (int) (mouseY - getHeight());
            scrolling = true;
        }
    }

    protected void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    protected void charTyped(char key, int keyCode) {
    }

    protected void mouseScrolled(int i) {
        if (hoveringWindow) {
            scrollOffset += i * 2;
            scrollOffset = MathUtility.clamp(scrollOffset, -maxElementsHeight + (getHeight() - 40), 0);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        scaling = false;
        scrolling = false;
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

    protected int getMinWidth() {
        return 150;
    }

    protected int getMinHeight() {
        return 150;
    }

    protected void setMaxElementsHeight(float maxElementsHeight) {
        this.maxElementsHeight = maxElementsHeight;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Identifier getIcon() {
        return icon;
    }
}

package thunder.hack.gui.hud;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.utility.render.Render3DEngine;

public class HudElement extends Module {
    private final Setting<PositionSetting> pos = new Setting<>("Position", new PositionSetting(0.5f, 0.5f));
    private boolean mouseState = false, mouseButton = false;
    private float x, y, dragX, dragY;
    private int height, width;

    public HudElement(String name, int width, int height) {
        super(name, Category.HUD);
        this.height = height;
        this.width = width;
    }

    @Override
    public void onRender2D(DrawContext context) {
        y = mc.getWindow().getScaledHeight() * pos.getValue().getY();
        x = mc.getWindow().getScaledWidth() * pos.getValue().getX();

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui) {
            if (mouseButton && mouseState) {
                pos.getValue().setX((normaliseX() - dragX) / mc.getWindow().getScaledWidth());
                pos.getValue().setY((normaliseY() - dragY) / mc.getWindow().getScaledHeight());
            }
        }
        if (mouseButton) {
            if (!mouseState && isHovering()) {
                dragX = (int) (normaliseX() - (pos.getValue().getX() * mc.getWindow().getScaledWidth()));
                dragY = (int) (normaliseY() - (pos.getValue().getY() * mc.getWindow().getScaledHeight()));
                mouseState = true;
            }
        } else {
            mouseState = false;
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onMouse(@NotNull EventMouse event) {
        if (event.getAction() == 0) {
            HudEditorGui.currentlyDragging = null;
            mouseButton = false;
        }
        if (event.getAction() == 1 && isHovering() && HudEditorGui.currentlyDragging == null) {
            HudEditorGui.currentlyDragging = this;
            mouseButton = true;
        }
    }

    public int normaliseX() {
        return (int) (mc.mouse.getX() / Render3DEngine.getScaleFactor());
    }

    public int normaliseY() {
        return (int) (mc.mouse.getY() / Render3DEngine.getScaleFactor());
    }

    public boolean isHovering() {
        return normaliseX() > x && normaliseX() < x + width && normaliseY() > y && normaliseY() < y + height;
    }

    public void setWidth(int width){
        this.width = width;
    }

    public void setHeight(int height){
        this.height = height;
    }

    public void setBounds(int w, int h){
        setWidth(w);
        setHeight(h);
    }

    public float getPosX() {
        return x;
    }

    public float getPosY() {
        return y;
    }

    public float getX() {
        return pos.getValue().x;
    }

    public float getY() {
        return pos.getValue().y;
    }
}

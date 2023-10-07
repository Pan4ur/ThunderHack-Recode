package thunder.hack.gui.hud;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;

public class HudElement extends Module {
    int height;
    int width;
    int dragX, dragY = 0;
    private boolean mousestate = false;
    float x1 = 0;
    float y1 = 0;

    public HudElement(String name, String description, int width, int height) {
        super(name, description, Category.HUD);
        this.height = height;
        this.width = width;
    }

    public final Setting<PositionSetting> pos = new Setting<>("Position", new PositionSetting(0.5f, 0.5f));

    public int normaliseX() {
        return (int) (mc.mouse.getX() / mc.getWindow().getScaleFactor());
    }

    public int normaliseY() {
        return (int) (mc.mouse.getY() / mc.getWindow().getScaleFactor());
    }

    public boolean isHovering() {
        return normaliseX() > x1 && normaliseX() < x1 + width && normaliseY() > y1 && normaliseY() < y1 + height;
    }

    private boolean m_butt = false;

    public void onRender2D(DrawContext context) {
        y1 = mc.getWindow().getScaledHeight() * pos.getValue().getY();
        x1 = mc.getWindow().getScaledWidth() * pos.getValue().getX();

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui) {
            if (m_butt && mousestate) {
                pos.getValue().setX((float) (normaliseX() - dragX) / mc.getWindow().getScaledWidth());
                pos.getValue().setY((float) (normaliseY() - dragY) / mc.getWindow().getScaledHeight());
            }
        }
        if (m_butt) {
            if (!mousestate && isHovering()) {
                dragX = (int) (normaliseX() - (pos.getValue().getX() * mc.getWindow().getScaledWidth()));
                dragY = (int) (normaliseY() - (pos.getValue().getY() * mc.getWindow().getScaledHeight()));
                mousestate = true;
            }
        } else {
            mousestate = false;
        }
    }

    @EventHandler
    public void onMouse(EventMouse event) {
        if (event.getAction() == 0) {
            m_butt = false;
        }
        if (event.getAction() == 1 && isHovering()) {
            m_butt = true;
        }
    }

    public float getPosX() {
        return x1;
    }

    public float getPosY() {
        return y1;
    }

    public float getX() {
        return pos.getValue().x;
    }

    public float getY() {
        return pos.getValue().y;
    }

    public void setHeight(int h) {
        this.height = h;
    }
}

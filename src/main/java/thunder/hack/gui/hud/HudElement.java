package thunder.hack.gui.hud;


import com.google.common.eventbus.Subscribe;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Util;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.modules.Module;
import net.minecraft.client.gui.screen.ChatScreen;


public class HudElement extends Module {

    int height;
    int width;
    int dragX, dragY = 0;
    private boolean mousestate = false;
    float x1 = 0;
    float y1 = 0;


    public HudElement(String name, String description,int width, int height) {
        super(name, description, Category.HUD);
        this.height = height;
        this.width = width;
    }

    public final Setting<PositionSetting> pos = new Setting<>("Position", new PositionSetting(0.5f, 0.5f));

    public int normaliseX() {
        return (int) (Util.mc.mouse.getX() / Util.mc.getWindow().getScaleFactor());
    }

    public int normaliseY() {
        return (int) (Util.mc.mouse.getY() / Util.mc.getWindow().getScaleFactor());
    }

    public boolean isHovering() {
        return normaliseX() > x1 && normaliseX() < x1 + width && normaliseY() > y1 && normaliseY() < y1 + height;
    }

    private boolean m_butt = false;

    public void onRender2D(Render2DEvent e) {
        y1 = Util.getScaledResolution().getScaledHeight() * pos.getValue().getY();
        x1 = Util.getScaledResolution().getScaledWidth() * pos.getValue().getX();

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui) {
            if (m_butt && mousestate) {
                pos.getValue().setX((float) (normaliseX() - dragX) / Util.getScaledResolution().getScaledWidth());
                pos.getValue().setY((float) (normaliseY() - dragY) / Util.getScaledResolution().getScaledHeight());
            }
        }
        if (m_butt) {
            if (!mousestate && isHovering()) {
                dragX = (int) (normaliseX() - (pos.getValue().getX() * Util.getScaledResolution().getScaledWidth()));
                dragY = (int) (normaliseY() - (pos.getValue().getY() * Util.getScaledResolution().getScaledHeight()));
                mousestate = true;
            }
        } else {
            mousestate = false;
        }
    }


    @Subscribe
    public void onMouse(EventMouse event){
        if(event.getAction() == 0){
            m_butt = false;
        }
        if(event.getAction() == 1 && isHovering()){
            m_butt = true;
        }
    }

    public float getPosX(){
        return x1;
    }

    public float getPosY(){
        return y1;
    }

    public float getX(){
        return pos.getValue().x;
    }

    public float getY(){
        return pos.getValue().y;
    }

    public void setHeight(int h){
        this.height = h;
    }
}

package thunder.hack.notification;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.events.impl.RenderBlurEvent;
import thunder.hack.modules.client.Notifications;
import thunder.hack.setting.Setting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.modules.Module.mc;

public class NotificationManager {

    public NotificationManager(){
        Thunderhack.EVENT_BUS.register(this);
    }

    private static final List<Notification> notificationsnew = new CopyOnWriteArrayList<>();
    private final Setting<Float> position = new Setting("Position", 1f, 0f, 1f);

    public void publicity(String title,String content, int second, Notification.Type type) {
        notificationsnew.add(new Notification(title,content, type, second * 1000));
        if(type == Notification.Type.SUCCESS){
         //   SoundUtil.playSound(SoundUtil.ThunderSound.SUCCESS);
        }
        if(type == Notification.Type.ERROR){
          //  SoundUtil.playSound(SoundUtil.ThunderSound.ERROR);
        }
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if(!ModuleManager.notifications.isEnabled()) return;
        if (notificationsnew.size() > 8)
            notificationsnew.remove(0);
        float startY = mc.getWindow().getScaledHeight() * position.getValue() - 36f;
        for (int i = 0; i < notificationsnew.size(); i++) {
            Notification notification = notificationsnew.get(i);
            notificationsnew.removeIf(Notification::shouldDelete);
            notification.render(event.getMatrixStack(),startY);
            startY -= notification.getHeight() + 3;
        }
    }

    @Subscribe
    public void onRenderShader(RenderBlurEvent event) {
        if(!ModuleManager.notifications.isEnabled()) return;
        float startY = mc.getWindow().getScaledHeight() * position.getValue() - 36f;
        for (int i = 0; i < notificationsnew.size(); i++) {
            Notification notification = notificationsnew.get(i);
            notification.renderShaders(event.getMatrixStack(),startY);
            startY -= notification.getHeight() + 3;
        }
    }


}

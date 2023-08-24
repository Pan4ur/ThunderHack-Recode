package thunder.hack.notification;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.ModuleManager;
import thunder.hack.setting.Setting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.modules.Module.mc;

public class NotificationManager {
    private static final List<Notification> notificationsnew = new CopyOnWriteArrayList<>();
    private final Setting<Float> position = new Setting<>("Position", 1f, 0f, 1f);

    public void publicity(String title, String content, int second, Notification.Type type) {
        notificationsnew.add(new Notification(title, content, type, second * 1000));
        if (type == Notification.Type.SUCCESS) {
            //   SoundUtil.playSound(SoundUtil.ThunderSound.SUCCESS);
        }
        if (type == Notification.Type.ERROR) {
            //  SoundUtil.playSound(SoundUtil.ThunderSound.ERROR);
        }
    }

    public void onRender2D(DrawContext event) {
        if (!ModuleManager.notifications.isEnabled()) return;
        if (notificationsnew.size() > 8)
            notificationsnew.remove(0);
        float startY = mc.getWindow().getScaledHeight() * position.getValue() - 36f;
        for (int i = 0; i < notificationsnew.size(); i++) {
            Notification notification = notificationsnew.get(i);
            notificationsnew.removeIf(Notification::shouldDelete);
            notification.render(event.getMatrices(), startY);
            startY -= notification.getHeight() + 3;
        }
    }

    public void onRenderShader(DrawContext context) {
        if (!ModuleManager.notifications.isEnabled()) return;
        float startY = mc.getWindow().getScaledHeight() * position.getValue() - 36f;
        for (int i = 0; i < notificationsnew.size(); i++) {
            Notification notification = notificationsnew.get(i);
            notification.renderShaders(context.getMatrices(), startY);
            startY -= notification.getHeight() + 3;
        }
    }


}

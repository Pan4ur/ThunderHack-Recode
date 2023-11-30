package thunder.hack.gui.notification;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.setting.Setting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.modules.Module.mc;

public class NotificationManager {
    private final Setting<Float> position = new Setting<>("Position", 1f, 0f, 1f);

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    {
        ThunderHack.EVENT_BUS.subscribe(this);
    }

    public void publicity(String title, String content, int second, Notification.Type type) {
        notifications.add(new Notification(title, content, type, second * 1000));
    }

    public void onRender2D(DrawContext event) {
        if (!ModuleManager.notifications.isEnabled()) return;
        if (notifications.size() > 8)
            notifications.remove(0);
        float startY = mc.getWindow().getScaledHeight() * position.getValue() - 36f;
        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);
            notification.render(event.getMatrices(), startY);
            startY -= (float) (notification.getHeight() + 3);
        }
    }

    public void onRenderShader(DrawContext context) {
        if (!ModuleManager.notifications.isEnabled()) return;
        float startY = mc.getWindow().getScaledHeight() * position.getValue() - 36f;

        for (Notification notification : notifications) {
            notification.renderShaders(context.getMatrices(), startY);
            startY -= (float) (notification.getHeight() + 3);
        }
    }
}
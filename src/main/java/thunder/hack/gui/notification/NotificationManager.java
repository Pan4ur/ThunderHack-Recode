package thunder.hack.gui.notification;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.cmd.Command;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.modules.client.Notifications;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.modules.Module.mc;

public class NotificationManager {
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public void publicity(String title, String content, int second, Notification.Type type) {
        if(ModuleManager.notifications.mode.getValue() == Notifications.Mode.Text)
            Command.sendMessage(Formatting.GRAY + "[" + Formatting.DARK_PURPLE + title + Formatting.GRAY + "] " + type.getColor() + content);
        else notifications.add(new Notification(title, content, type, second * 1000));
    }

    public void onRender2D(DrawContext event) {
        if (!ModuleManager.notifications.isEnabled()) return;

        if (notifications.size() > 8)
            notifications.remove(0);

        float startY = ModuleManager.notifications.mode.getValue() == Notifications.Mode.Default ? mc.getWindow().getScaledHeight() - 36f : mc.getWindow().getScaledHeight() / 2f + 15;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);
            notification.render(event.getMatrices(), startY);
            startY = (float) (ModuleManager.notifications.mode.getValue() == Notifications.Mode.Default ? startY - notification.getHeight() - 3: startY + notification.getHeight() + 3);
        }
    }

    public void onUpdate() {
        if (!ModuleManager.notifications.isEnabled()) return;
        notifications.forEach(Notification::onUpdate);
    }

    public void onRenderShader(DrawContext context) {
        if (!ModuleManager.notifications.isEnabled()) return;

        float startY = ModuleManager.notifications.mode.getValue() == Notifications.Mode.Default ? mc.getWindow().getScaledHeight() - 36f : mc.getWindow().getScaledHeight() / 2f + 15;

        for (Notification notification : notifications) {
            notification.renderShaders(context.getMatrices(), startY);
            startY = (float) (ModuleManager.notifications.mode.getValue() == Notifications.Mode.Default ? startY - notification.getHeight() - 3 : startY + notification.getHeight() + 3);
        }
    }
}
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
    public final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public void publicity(String title, String content, int second, Notification.Type type) {
        if(ModuleManager.notifications.mode.getValue() == Notifications.Mode.Text)
            Command.sendMessage(Formatting.GRAY + "[" + Formatting.DARK_PURPLE + title + Formatting.GRAY + "] " + type.getColor() + content);
        else notifications.add(new Notification(title, content, type, second * 1000));
    }

    public void onRender2D(DrawContext context) {
        if (!ModuleManager.notifications.isEnabled()) return;

        float startY = isDefault() ? mc.getWindow().getScaledHeight() - 36f : mc.getWindow().getScaledHeight() / 2f + 25;

        for (Notification notification : notifications) {
            notification.renderShaders(context.getMatrices(), startY  + (isDefault() ? 0 : notifications.size() * 16));
            startY = (float) (startY - notification.getHeight() - 3f);
        }

        if (notifications.size() > 8)
            notifications.remove(0);

        float startY1 = isDefault() ? mc.getWindow().getScaledHeight() - 36f : mc.getWindow().getScaledHeight() / 2f + 25;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);
            notification.render(context.getMatrices(), startY1  + (isDefault() ? 0 : notifications.size() * 16));
            startY1 = (float) (startY1 - notification.getHeight() - 3f);
        }
    }

    public void onUpdate() {
        if (!ModuleManager.notifications.isEnabled()) return;
        notifications.forEach(Notification::onUpdate);
    }

    public static boolean isDefault() {
        return ModuleManager.notifications.mode.getValue() == Notifications.Mode.Default;
    }
}
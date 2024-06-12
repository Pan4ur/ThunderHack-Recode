package thunder.hack.core.impl;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.util.Bytecode;
import thunder.hack.cmd.Command;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.notification.Notification;
import thunder.hack.modules.client.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static thunder.hack.modules.Module.mc;

public class NotificationManager {
    public final List<Notification> notifications = new ArrayList<>();

    public void publicity(String title, String content, int second, Notification.Type type) {
        if(ModuleManager.notifications.mode.getValue() == Notifications.Mode.Text)
            Command.sendMessage(Formatting.GRAY + "[" + Formatting.DARK_PURPLE + title + Formatting.GRAY + "] " + type.getColor() + content);
        else if (ModuleManager.notifications.mode.getValue() == Notifications.Mode.Programming) {
            if(type == Notification.Type.ENABLED){
                Command.sendMessage(type.getColor() + title + ".enable();");
            }
            else{
                Command.sendMessage(type.getColor() + title + ".disable();");
            }
        } else notifications.add(new Notification(title, content, type, second * 1000));
    }

    public void onRender2D(DrawContext context) {
        if (!ModuleManager.notifications.isEnabled()) return;

        float startY = isDefault() ? mc.getWindow().getScaledHeight() - 36f : mc.getWindow().getScaledHeight() / 2f + 25;

        if (notifications.size() > 8)
            notifications.removeFirst();

        notifications.removeIf(Notification::shouldDelete);

        for (Notification n : Lists.newArrayList(notifications)) {
            startY = (float) (startY - n.getHeight() - 3f);
            n.renderShaders(context.getMatrices(), startY  + (isDefault() ? 0 : notifications.size() * 16));
            n.render(context.getMatrices(), startY  + (isDefault() ? 0 : notifications.size() * 16));
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
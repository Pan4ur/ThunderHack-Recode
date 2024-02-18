package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.AntiBot;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;

import static thunder.hack.modules.client.MainSettings.isRu;

public class TotemPopCounter extends Module {
    public TotemPopCounter() {
        super("TotemPopCounter", Category.MISC);
    }

    public Setting<Boolean> notification = new Setting<>("Notification", true);

    @EventHandler
    public void onTotemPop(@NotNull TotemPopEvent event) {
        if (event.getEntity() == mc.player) return;

        String s;
        if (isRu()) s = Formatting.GREEN + event.getEntity().getName().getString() + Formatting.WHITE + " попнул " + Formatting.AQUA + (event.getPops() > 1 ? event.getPops() + "" + Formatting.WHITE + " тотемов!" : Formatting.WHITE + "тотем!");
        else s = Formatting.GREEN + event.getEntity().getName().getString() + Formatting.WHITE + " popped " + Formatting.AQUA + (event.getPops() > 1 ? event.getPops() + "" + Formatting.WHITE + " totems!" : Formatting.WHITE + " a totem!");

        sendMessage(s);
        if (notification.getValue())
            ThunderHack.notificationManager.publicity("TotemPopCounter", s, 2, Notification.Type.INFO);
    }

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || AntiBot.bots.contains(player) || player.getHealth() > 0 || !ThunderHack.combatManager.popList.containsKey(player.getName().getString()))
                continue;

            String s;
            if (isRu()) s = Formatting.GREEN + player.getName().getString() + Formatting.WHITE + " попнул " + (ThunderHack.combatManager.popList.get(player.getName().getString()) > 1 ? ThunderHack.combatManager.popList.get(player.getName().getString()) + "" + Formatting.WHITE + " тотемов и сдох!" : Formatting.WHITE + "тотем и сдох!");
            else s = Formatting.GREEN + player.getName().getString() + Formatting.WHITE + " popped " + (ThunderHack.combatManager.popList.get(player.getName().getString()) > 1 ? ThunderHack.combatManager.popList.get(player.getName().getString()) + "" + Formatting.WHITE + " totems and died EZ LMAO!" : Formatting.WHITE + "totem and died EZ LMAO!");

            sendMessage(s);
            if (notification.getValue())
                ThunderHack.notificationManager.publicity("TotemPopCounter", s, 2, Notification.Type.INFO);
        }
    }
}

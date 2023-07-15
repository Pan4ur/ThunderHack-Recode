package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.AntiBot;
import thunder.hack.notification.Notification;
import thunder.hack.notification.NotificationManager;

public class TotemPopCounter extends Module {

    public TotemPopCounter() {
        super("TotemPopCounter", Category.MISC);
    }

    @Subscribe
    public void onTotemPop(TotemPopEvent event) {
        if (event.getEntity() == mc.player) return;
        String s =  Formatting.GREEN + event.getEntity().getName().getString() + Formatting.WHITE + " попнул " + Formatting.AQUA + (event.getPops() > 1 ? event.getPops() + "" + Formatting.WHITE + " тотемов!" :  Formatting.WHITE + " тотем!");
        Command.sendMessage(s);
        Thunderhack.notificationManager.publicity("TotemPopCounter", s,2, Notification.Type.INFO);
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || AntiBot.bots.contains(player) || player.getHealth() > 0 || !Thunderhack.combatManager.popList.containsKey(player.getName().getString())) continue;
            String s = Formatting.GREEN + player.getName().getString() + Formatting.WHITE + " попнул " + (Thunderhack.combatManager.popList.get(player.getName().getString()) > 1 ? Thunderhack.combatManager.popList.get(player.getName().getString()) + "" + Formatting.WHITE +  " тотемов и сдох!" : Formatting.WHITE + " тотем и сдох!");
            Command.sendMessage(s);
            Thunderhack.notificationManager.publicity("TotemPopCounter", s,2, Notification.Type.INFO);
        }
    }
}

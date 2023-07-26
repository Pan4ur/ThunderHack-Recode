package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventEntityRemoved;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.modules.Module;
import thunder.hack.notification.Notification;
import thunder.hack.setting.Setting;

import java.util.ArrayList;
import java.util.Objects;

public class VisualRange extends Module {

    public VisualRange() {
        super("VisualRange", Category.MISC);
    }

    private static final ArrayList<String> entities = new ArrayList<>();
    public Setting<Boolean> leave = new Setting<>("Leave", true);
    public Setting<Boolean> enter = new Setting<>("Enter", true);
    public Setting<Boolean> friends = new Setting<>("Friends", true);
    public Setting<Boolean> soundpl = new Setting<>("Sound", true);
    public Setting<mode> Mode = new Setting<>("Mode", mode.Notification);

    @Subscribe
    public void onEntityAdded(EventEntitySpawn event) {
        if (!isValid(event.getEntity())) return;

        if (!entities.contains(event.getEntity().getName().getString()))
            entities.add(event.getEntity().getName().getString());
        else return;

        if (enter.getValue()) notify(event.getEntity(), true);
    }

    @Subscribe
    public void onEntityRemoved(EventEntityRemoved event) {
        if (!isValid(event.entity)) return;

        if (entities.contains(event.entity.getName().getString()))
            entities.remove(event.entity.getName().getString());
        else return;

        if (leave.getValue()) notify(event.entity, false);
    }

    public void notify(Entity entity, boolean enter) {
        String message = "";
        if (Thunderhack.friendManager.isFriend(entity.getName().getString())) {
            message = Formatting.AQUA + entity.getName().getString();
        } else {
            message = Formatting.GRAY + entity.getName().getString();
        }

        if (enter) {
            message += Formatting.GREEN + " was found!";
        } else {
            message += Formatting.RED + " left!";
        }

        if (Mode.getValue() == mode.Chat) {
            Command.sendMessage(message);
        }
        if (Mode.getValue() == mode.Notification) {
            Thunderhack.notificationManager.publicity("VisualRange",message, 2, Notification.Type.WARNING);
        }

        if (soundpl.getValue()) {
            try {
                if (enter) {
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1f, 1f);
                } else {
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1f, 1f);
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean isValid(Entity entity) {
        if (!(entity instanceof PlayerEntity)) return false;
        if (entity == mc.player || (Thunderhack.friendManager.isFriend(entity.getName().getString()) && !friends.getValue())) return false;
        return true;
    }

    public enum mode {Chat, Notification}
}

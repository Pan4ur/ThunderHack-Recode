package thunder.hack.core;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventPostTick;
import thunder.hack.events.impl.EventTick;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.AntiBot;

import java.util.HashMap;

import static thunder.hack.utility.Util.mc;

public class CombatManager {
    public CombatManager(){
        Thunderhack.EVENT_BUS.register(this);
    }

    public HashMap<String, Integer> popList = new HashMap<>();

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if (Module.fullNullCheck()) return;

        if (event.getPacket() instanceof EntityStatusS2CPacket pac) {
            if (pac.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity ent = pac.getEntity(mc.world);
                if(!(ent instanceof PlayerEntity)) return;
                if (popList == null) {
                    popList = new HashMap<>();
                }
                if (popList.get(ent.getName().getString()) == null) {
                    popList.put(ent.getName().getString(), 1);
                } else if (popList.get(ent.getName().getString()) != null) {
                    popList.put(ent.getName().getString(),  popList.get(ent.getName().getString()) + 1);
                }
                Thunderhack.EVENT_BUS.post(new TotemPopEvent((PlayerEntity) ent, popList.get(ent.getName().getString())));
            }
        }
    }

    @Subscribe
    public void onPostTick(EventPostTick event) {
        if (Module.fullNullCheck()) {
            return;
        }
        for (PlayerEntity player : mc.world.getPlayers()) {
            if(AntiBot.bots.contains(player)) return;
            if (player.getHealth() <= 0 && popList.containsKey(player.getName().getString())) {
                popList.remove(player.getName().getString(), popList.get(player.getName().getString()));
            }
        }
    }

    public int getPops(PlayerEntity entity){
        if(popList.get(entity.getName().getString()) == null) return 0;
        return popList.get(entity.getName().getString());
    }


}

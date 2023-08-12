package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;

import static thunder.hack.modules.combat.Criticals.getEntity;

public class AntiFriendAttack extends Module {
    public AntiFriendAttack() {
        super("AntiFriendAttack", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e){
        if(e.getPacket() instanceof PlayerInteractEntityC2SPacket pac)
            if(Thunderhack.friendManager.isFriend(getEntity(pac).getName().getString())) e.cancel();
    }
}

package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;

import static thunder.hack.modules.combat.Criticals.getEntity;
import static thunder.hack.modules.combat.Criticals.getInteractType;

public class WTap extends Module {
    public WTap() {
        super("WTap", Category.COMBAT);
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket && getInteractType(event.getPacket()) == Criticals.InteractType.ATTACK && !(getEntity(event.getPacket()) instanceof EndCrystalEntity)) {
            mc.player.setSprinting(false);
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            mc.player.setSprinting(true);
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }
}

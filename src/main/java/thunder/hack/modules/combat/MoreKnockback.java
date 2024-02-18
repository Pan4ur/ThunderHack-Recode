package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Formatting;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.modules.client.MainSettings.isRu;
import static thunder.hack.modules.combat.Criticals.getEntity;
import static thunder.hack.modules.combat.Criticals.getInteractType;

public class MoreKnockback extends Module {
    public MoreKnockback() {
        super("MoreKnockback", Category.COMBAT);
    }

    public Setting<Boolean> inMove = new Setting<>("InMove", true);

    @Override
    public void onEnable() {
        sendMessage(Formatting.RED + (isRu() ? "Внимание! С этим модулем не будут проходить криты" : "Attention! With this module your hits will not be critical"));
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Send event) {
        if ((!MovementUtility.isMoving() || inMove.getValue()) && event.getPacket() instanceof PlayerInteractEntityC2SPacket && getInteractType(event.getPacket()) == Criticals.InteractType.ATTACK && !(getEntity(event.getPacket()) instanceof EndCrystalEntity)) {
            mc.player.setSprinting(false);
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            mc.player.setSprinting(true);
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }
}

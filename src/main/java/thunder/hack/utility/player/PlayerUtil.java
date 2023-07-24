package thunder.hack.utility.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.Thunderhack;
import thunder.hack.modules.player.FreeCam;

import static thunder.hack.utility.Util.mc;

public class PlayerUtil {

    public ClientPlayerEntity getPlayer(){
        return mc.player;
    }
}

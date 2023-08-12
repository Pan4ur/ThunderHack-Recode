package thunder.hack.utility.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import thunder.hack.injection.accesors.IClientWorldMixin;

import static thunder.hack.modules.Module.mc;

public final class PlayerUtility {
    public ClientPlayerEntity getPlayer() {
        return mc.player;
    }

    public static int getWorldActionId(ClientWorld world) {
        PendingUpdateManager pum = getUpdateManager(world);
        int p = pum.getSequence();
        pum.close();
        return p;
    }

    static PendingUpdateManager getUpdateManager(ClientWorld world) {
        return ((IClientWorldMixin) world).acquirePendingUpdateManager();
    }
}

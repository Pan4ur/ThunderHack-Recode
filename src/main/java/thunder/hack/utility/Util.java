package thunder.hack.utility;

import thunder.hack.injection.accesors.IClientWorldMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;

public interface Util {
    MinecraftClient mc = MinecraftClient.getInstance();

    static Window getScaledResolution(){
        return mc.getWindow();
    }

    static int getWorldActionId(ClientWorld world) {
        PendingUpdateManager pum = getUpdateManager(world);
        int p = pum.getSequence();
        pum.close();
        return p;
    }
    static PendingUpdateManager getUpdateManager(ClientWorld world) {
        return ((IClientWorldMixin) world).acquirePendingUpdateManager();
    }
}


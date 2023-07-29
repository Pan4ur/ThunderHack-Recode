package thunder.hack.utility;

import java.util.concurrent.atomic.AtomicReference;

import static thunder.hack.modules.Module.mc;


public class ThunderUtils {
    public static String solvename(String notsolved) {
        AtomicReference<String> mb = new AtomicReference<>("FATAL ERROR");
        mc.getNetworkHandler().getListedPlayerListEntries().forEach(player -> {
            if (notsolved.contains(player.getProfile().getName())) {
                mb.set(player.getProfile().getName());
            }
        });
        return mb.get();
    }
}

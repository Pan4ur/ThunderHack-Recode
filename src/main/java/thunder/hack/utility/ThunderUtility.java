package thunder.hack.utility;

import java.util.concurrent.atomic.AtomicReference;

import static thunder.hack.modules.Module.mc;


public final class ThunderUtility {
    public static String solveName(String notSolved) {
        AtomicReference<String> mb = new AtomicReference<>("FATAL ERROR");
        mc.getNetworkHandler().getListedPlayerListEntries().forEach(player -> {
            if (notSolved.contains(player.getProfile().getName())) {
                mb.set(player.getProfile().getName());
            }
        });

        return mb.get();
    }
}

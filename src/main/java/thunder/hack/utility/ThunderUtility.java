package thunder.hack.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static thunder.hack.modules.Module.mc;

public final class ThunderUtility {
    public static List<String> changeLog = new ArrayList<>();

    public static String solveName(String notSolved) {
        AtomicReference<String> mb = new AtomicReference<>("FATAL ERROR");
        mc.getNetworkHandler().getListedPlayerListEntries().forEach(player -> {
            if (notSolved.contains(player.getProfile().getName())) {
                mb.set(player.getProfile().getName());
            }
        });

        return mb.get();
    }

    public static void parseChangeLog() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/changeLog.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                changeLog.add(inputLine.trim());
        } catch (Exception ignored) {
            changeLog.add("ChangeLog parsing error :(");
        }
    }
}
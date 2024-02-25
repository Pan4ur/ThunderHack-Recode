package thunder.hack.utility;

import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.utility.math.MathUtility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static thunder.hack.core.impl.ConfigManager.IMAGES_FOLDER;
import static thunder.hack.modules.Module.mc;

public final class ThunderUtility {
    public static List<String> changeLog = new ArrayList<>();

    public static @NotNull String getAuthors() {
        List<String> names = ThunderHack.MOD_META.getAuthors()
                .stream()
                .map(Person::getName)
                .toList();

        return String.join(", ", names);
    }

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

    public static Identifier getCustomImg(String name) throws IOException {
        return mc.getTextureManager().registerDynamicTexture("th-" + name + "-" + (int) MathUtility.random(0, 1000), new NativeImageBackedTexture(NativeImage.read(new FileInputStream(IMAGES_FOLDER + "/" + name + ".png"))));
    }
}
package thunder.hack.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.util.internal.StringUtil;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ConfigManager;
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
    public static List<String> starGazer = new ArrayList<>();

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
            URL url = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/changeLogBeta.txt");
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

    public static void parseStarGazer() {
        try {
            for(int page = 1; page <= 3; page++){
                URL url = new URL("https://api.github.com/repos/Pan4ur/ThunderHack-Recode/stargazers?per_page=100;page=" + page);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    JsonParser parser = new JsonParser();
                    JsonArray array = null;
                    array = (JsonArray) parser.parse(inputLine);

                    for (int i = 0; i < array.size(); i++) {
                        JsonObject jsonObject = (JsonObject) array.get(i);
                        starGazer.add(jsonObject.getAsJsonPrimitive("login").getAsString());
                    }
                }
                in.close();
                Thread.sleep(1500);
            }
        } catch (Exception ignored) {
        }
    }
}
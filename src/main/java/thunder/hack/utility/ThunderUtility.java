package thunder.hack.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.utility.math.MathUtility;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
        Objects.requireNonNull(mc.getNetworkHandler()).getListedPlayerListEntries().forEach(player -> {
            if (notSolved.contains(player.getProfile().getName())) {
                mb.set(player.getProfile().getName());
            }
        });

        return mb.get();
    }

    public static Identifier getCustomImg(String name) throws IOException {
        return mc.getTextureManager().registerDynamicTexture("th-" + name + "-" + (int) MathUtility.random(0, 1000), new NativeImageBackedTexture(NativeImage.read(new FileInputStream(IMAGES_FOLDER + "/" + name + ".png"))));
    }

    public static void parseStarGazer() {
        try {
            for (int page = 1; page <= 3; page++) {
                URL url = new URL("https://api.github.com/repos/Pan4ur/ThunderHack-Recode/stargazers?per_page=100;page=" + page);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    JsonArray array = (JsonArray) JsonParser.parseString(inputLine);

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


    public static String readManifestField(String fieldName) {
        try {
            Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
            while (en.hasMoreElements()) {
                try {
                    URL url = en.nextElement();
                    InputStream is = url.openStream();
                    if (is != null) {
                        String s = new Manifest(is).getMainAttributes().getValue(fieldName);
                        if(s != null) 
                            return s;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return "0";
    }


    public static void parseCommits() {
        try {
            URL url = new URL("https://api.github.com/repos/Pan4ur/ThunderHack-Recode/commits?per_page=50");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String inputLine;
            changeLog.add("Changelog [Recode; Date: " + ThunderHack.BUILD_DATE + "; GitHash:" + ThunderHack.GITH_HASH + "]");
            changeLog.add("\n");

            while ((inputLine = in.readLine()) != null) {
                JsonArray array = (JsonArray) JsonParser.parseString(inputLine);

                for (int i = 0; i < array.size(); i++) {
                    JsonObject jsonObject = (JsonObject) array.get(i);
                    JsonObject commitBlock = jsonObject.getAsJsonObject("commit");
                    JsonObject nameBlock = commitBlock.getAsJsonObject("author");

                    String name = nameBlock.get("name").getAsString();
                    String date = nameBlock.get("date").getAsString();
                    String info = commitBlock.get("message").getAsString();

                    name = name.replace("\n", "");
                    date = date.replace("\n", "");
                    info = info.replace("\n", "");

                    if(name.contains("ImgBot"))
                        continue;

                    if(info.startsWith("Merge"))
                        continue;

                    if(info.startsWith("Revert"))
                        continue;

                    changeLog.add("- " + info + " [" + Formatting.GRAY + date.split("T")[0] + Formatting.RESET + "]  (@" + Formatting.RED + name + Formatting.RESET + ")");
                }
            }
            in.close();
        } catch (Exception e) {
        }
    }
}

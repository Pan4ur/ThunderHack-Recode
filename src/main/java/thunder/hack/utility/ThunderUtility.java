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
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static thunder.hack.core.manager.client.ConfigManager.IMAGES_FOLDER;
import static thunder.hack.features.modules.Module.mc;

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

    public static void syncVersion() {
        try {
            if (!new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/syncVersion121.txt").openStream())).readLine().equals(ThunderHack.VERSION))
                ThunderHack.isOutdated = true;
        } catch (Exception ignored) {
        }
    }

    public static void parseStarGazer() {
        List<String> starGazers = new ArrayList<>();

        try {
            for (int page = 1; page <= 3; page++) {
                URL url = new URL("https://api.github.com/repos/Pan4ur/ThunderHack-Recode/stargazers?per_page=100&page=" + page);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonArray jsonArray = JsonParser.parseString(response.toString()).getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                    starGazers.add(jsonObject.getAsJsonPrimitive("login").getAsString());
                }

                Thread.sleep(1500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncContributors() {
        try {
            URL list = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/thTeam.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(list.openStream(), StandardCharsets.UTF_8));
            String inputLine;
            int i = 0;
            while ((inputLine = in.readLine()) != null) {
                ThunderHack.contributors[i] = inputLine.trim();
                i++;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
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
                        if (s != null)
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

            changeLog.add("Changelog [Recode; Date: " + ThunderHack.BUILD_DATE + "; GitHash:" + ThunderHack.GITHUB_HASH + "]");
            changeLog.add("\n");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                JsonArray jsonArray = JsonParser.parseString(inputLine).getAsJsonArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                    JsonObject commitObject = jsonObject.getAsJsonObject("commit");
                    JsonObject authorObject = commitObject.getAsJsonObject("author");

                    String name = authorObject.get("name").getAsString().replace("\n", "");
                    String date = authorObject.get("date").getAsString().replace("\n", "");
                    String info = commitObject.get("message").getAsString().replace("\n", "");

                    if (name.contains("ImgBot") || info.startsWith("Merge") || info.startsWith("Revert")) {
                        continue;
                    }

                    String formattedDate = Formatting.GRAY + date.split("T")[0] + Formatting.RESET;
                    String formattedName = "@" + Formatting.RED + name + Formatting.RESET;

                    changeLog.add("- " + info + " [" + formattedDate + "]  (" + formattedName + ")");
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

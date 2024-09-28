package thunder.hack.utility;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.client.Capes;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OptifineCapes {
    /**
     * author: @dragonostic
     * of-capes
     */

    public interface ReturnCapeTexture {
        void response(Identifier id);
    }

    public static void loadPlayerCape(GameProfile player, ReturnCapeTexture response) {
        try {
            String uuid = player.getId().toString();
            NativeImageBackedTexture optifineCape = getCapeFromURL(String.format("http://s.optifine.net/capes/%s.png", player.getName()));
            NativeImageBackedTexture minecraftcapesCape = getCapeFromURL(String.format("https://api.minecraftcapes.net/profile/%s/cape/map", player.getId().toString().replace("-","")));
            NativeImageBackedTexture minecraftcapesCapeCrack = getCapeFromURL(String.format("https://api.minecraftcapes.net/profile/%s/cape/map", getUUID(player)));
            switch (ModuleManager.capes.priority.getValue()) {
                case Capes.capePriority.Optifine:
                    if (optifineCape != null && ModuleManager.capes.optifineCapes.getValue()) {
                        Identifier capeTexture = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-cape-" + uuid, optifineCape);
                        response.response(capeTexture);
                    } else if (ModuleManager.capes.minecraftcapesCapes.getValue() && minecraftcapesCape != null) {
                        Identifier capeTexture = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-cape-" + uuid, minecraftcapesCape);
                        response.response(capeTexture);
                    } else if(ModuleManager.capes.minecraftcapesCapes.getValue()) {
                        Identifier capeTexture = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-cape-" + uuid, minecraftcapesCapeCrack);
                        response.response(capeTexture);
                    }
                    break;
                case Capes.capePriority.Minecraftcapes:
                    if (minecraftcapesCape != null && ModuleManager.capes.minecraftcapesCapes.getValue()) {
                        Identifier capeTexture = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-cape-" + uuid, minecraftcapesCape);
                        response.response(capeTexture);
                    } else if (minecraftcapesCapeCrack != null && ModuleManager.capes.minecraftcapesCapes.getValue()) {
                        Identifier capeTexture = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-cape-" + uuid, minecraftcapesCapeCrack);
                        response.response(capeTexture);
                    } else if (ModuleManager.capes.optifineCapes.getValue()) {
                        Identifier capeTexture = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-cape-" + uuid, optifineCape);
                        response.response(capeTexture);
                    }
                    break;
            }
        } catch (Exception ignored) {
        }
    }

    public static String getUUID(GameProfile player) {
        StringBuffer content = null;
        try {
            URL request = new URL(String.format("https://api.mojang.com/users/profiles/minecraft/%s", player.getName()));
            HttpsURLConnection connection = (HttpsURLConnection) request.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
            String inputLine;
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            content = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();
        } catch (Exception ignored) {
        }
        // ЭТО САМЫЙ СЕКСУАЛЬНЫЙ JSON ПАРСЕР В ИСТОРИИ ЧЕЛОВЕЧЕСТВА
        Pattern uuidPattern = Pattern.compile("id");
        Matcher uuidMatch = uuidPattern.matcher(content.toString());
        if (!uuidMatch.find())
            return null;
        String[] parsin = content.toString().split("\"");
        return parsin[3];
    }

    public static NativeImageBackedTexture getCapeFromURL(String capeStringURL) {
        try {
            URL capeURL = new URL(capeStringURL);
            return getCapeFromStream(capeURL.openStream());
        } catch (IOException e) {
            return null;
        }
    }

    public static NativeImageBackedTexture getCapeFromStream(InputStream image) {
        NativeImage cape = null;
        try {
            cape = NativeImage.read(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (cape != null) {
            return new NativeImageBackedTexture(parseCape(cape));
        }
        return null;
    }

    public static NativeImage parseCape(NativeImage image) {
        int imageWidth = 64;
        int imageHeight = 32;
        int imageSrcWidth = image.getWidth();
        int srcHeight = image.getHeight();
        for (int imageSrcHeight = image.getHeight(); imageWidth < imageSrcWidth || imageHeight < imageSrcHeight; imageHeight *= 2) {
            imageWidth *= 2;
        }

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < imageSrcWidth; x++) {
            for (int y = 0; y < srcHeight; y++) {
                imgNew.setColor(x, y, image.getColor(x, y));
            }
        }
        image.close();
        return imgNew;
    }
}

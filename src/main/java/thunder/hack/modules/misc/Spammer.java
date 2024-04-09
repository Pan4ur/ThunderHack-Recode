package thunder.hack.modules.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.IOUtils;
import thunder.hack.ThunderHack;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClientSettings;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.integrated.IntegratedPlayerManager;

public class Spammer extends Module {
    public static ArrayList<String> SpamList = new ArrayList<>();
    public Setting<Mode> mode = new Setting<>("mode",Mode.File);
    public Setting<Apis> api = new Setting<>("api",Apis.Cat,v -> mode.getValue() == Mode.Api);
    public Setting<Boolean> global = new Setting<>("global", true);
    public Setting<Integer> delay = new Setting<>("delay", 5, 1, 30);
    private final Timer timer_delay = new Timer();
    private String message;

    public Spammer() {
        super("Spammer", Category.MISC);
    }

    private void changeMessage(){
        ThunderHack.asyncManager.run(() -> {
            try{
                switch (api.getValue()){
                    case Cat: {
                        String jsonResponse = IOUtils.toString(new URL("https://catfact.ninja/fact?max_length=256"), StandardCharsets.UTF_8);
                        JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
                        this.message = jsonObject.get("fact").getAsString();
                        break;
                    }
                    case Joke: {
                        URL response = new URL("https://v2.jokeapi.dev/joke/Any?format=txt&type=single");
                        this.message = new String(response.openStream().readAllBytes());
                        break;
                    }
                    case Activities:{
                        String jsonResponse = IOUtils.toString(new URL("http://www.boredapi.com/api/activity"), StandardCharsets.UTF_8);
                        JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
                        this.message = jsonObject.get("activity").getAsString();
                        break;
                    }
                }
                if(this.message.length() > 256){
                    message = null;
                    changeMessage();
                }
            }catch (IOException e){
                disable(ClientSettings.isRu() ? "Не удалось загрузить факт,может ты включишь интернет?" : "Failed to load the fact, can you turn on the Internet?");
            }
        });
    }
    public static void loadSpammer() {
        try {
            File file = new File("ThunderHackRecode/misc/spammer.txt");
            if (!file.exists()) file.createNewFile();
            new Thread(() -> {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(isr);
                    ArrayList<String> lines = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }

                    boolean newline = false;

                    for (String l : lines) {
                        if (l.equals("")) {
                            newline = true;
                            break;
                        }
                    }

                    SpamList.clear();
                    ArrayList<String> spamList = new ArrayList<>();

                    if (newline) {
                        StringBuilder spamChunk = new StringBuilder();

                        for (String l : lines) {
                            if (l.equals("")) {
                                if (!spamChunk.isEmpty()) {
                                    spamList.add(spamChunk.toString());
                                    spamChunk = new StringBuilder();
                                }
                            } else {
                                spamChunk.append(l).append(" ");
                            }
                        }
                        spamList.add(spamChunk.toString());
                    } else spamList.addAll(lines);
                    SpamList = spamList;
                } catch (Exception ignored) {
                }
            }).start();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onEnable() {
        loadSpammer();
        changeMessage();

    }

    @Override
    public void onUpdate() {
        if (timer_delay.passedS(delay.getValue())) {
            String c;
            if(mode.getValue() == Mode.File){
                if (SpamList.isEmpty()) {
                    disable(ClientSettings.isRu() ? "Файл spammer пустой!" : "The spammer file is empty!");
                    return;
                }
                c = SpamList.get(new Random().nextInt(SpamList.size()));
            }
            else{
                if(message == null){return;}
                c = message;
            }
            if (c.charAt(0) == '/') {
                c = c.replace("/", "");
                mc.player.networkHandler.sendCommand(c);
            } else mc.player.networkHandler.sendChatMessage(global.getValue() ? "!" + c : c);
            changeMessage();
            timer_delay.reset();
        }
    }


    private enum Mode {File, Api}
    private enum Apis {Cat,Joke,Activities}
}

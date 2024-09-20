package thunder.hack.features.modules.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import thunder.hack.core.Managers;
import thunder.hack.features.hud.impl.StaffBoard;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class Spammer extends Module {
    public static ArrayList<String> SpamList = new ArrayList<>();
    public Setting<Mode> mode = new Setting<>("mode", Mode.Chat);
    public Setting<Messages> messages = new Setting<>("messages", Messages.File);
    public Setting<WhisperPrefix> whisper_prefix = new Setting<>("prefix", WhisperPrefix.W, v -> mode.getValue() == Mode.Whispers);
    public Setting<Boolean> global = new Setting<>("global", true, v -> mode.getValue() == Mode.Chat);
    public Setting<Boolean> antiSpam = new Setting<>("AntiSpam", false);
    public Setting<Float> delay = new Setting<>("delay", 5f, 0f, 30f);
    private final Timer timer_delay = new Timer();
    private final Random random = new Random();
    private String fact;

    public Spammer() {
        super("Spammer", Category.MISC);
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

    public String getPlayerName() {
        try {
            List<String> list = StaffBoard.getOnlinePlayer();
            if (list.isEmpty())
                return "";
            return list.get(random.nextInt(0, list.size() - 1));
        } catch (NullPointerException e) {
            return null;
        }
    }

    private void changeFact() {
        Managers.ASYNC.run(() -> {
            try {
                String jsonResponse = IOUtils.toString(new URL("https://catfact.ninja/fact?max_length=200"), StandardCharsets.UTF_8);
                JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
                fact = jsonObject.get("fact").getAsString();
            } catch (IOException e) {
                disable(isRu() ? "Не удалось загрузить факт, может ты включишь интернет?" : "Failed to load the fact, can you turn on the Internet?");
            }
        });
    }

    public static String generateRandomSymbol() {
        Random random = new Random();
        String randomSymbol = "[";
        randomSymbol += (char) (random.nextInt(26) + 'a');
        randomSymbol += random.nextInt(10);
        randomSymbol += (char) (random.nextInt(26) + 'a');
        randomSymbol += "]";
        return randomSymbol;
    }

    @Override
    public void onEnable() {
        loadSpammer();
        changeFact();
    }

    @Override
    public void onUpdate() {
        if (timer_delay.passedMs((long) (delay.getValue() * 1000))) {
            String c;
            if (messages.getValue() == Messages.File) {
                if (SpamList.isEmpty()) {
                    disable(isRu() ? "Файл spammer пустой!" : "The spammer file is empty!");
                    return;
                }
                c = SpamList.get(new Random().nextInt(SpamList.size()));
            } else {
                if (fact == null) {
                    return;
                }
                c = fact;
                changeFact();
            }
            if (antiSpam.getValue()) {
                c += generateRandomSymbol();
            }
            if (mode.getValue() == Mode.Chat) {
                if (c.charAt(0) == '/') {
                    c = c.replace("/", "");
                    mc.player.networkHandler.sendCommand(c);
                } else mc.player.networkHandler.sendChatMessage(global.getValue() ? "!" + c : c);
            } else {
                try {
                    String prefix = whisper_prefix.getValue().prefix;
                    mc.player.networkHandler.sendCommand(prefix + getPlayerName() + " " + c);
                } catch (NullPointerException e) {
                }
            }

            timer_delay.reset();
        }
    }

    private enum Messages {File, CatFacts}

    private enum Mode {Chat, Whispers}

    private enum WhisperPrefix {
        W("w "),
        Msg("msg "),
        Tell("tell ");

        final String prefix;

        WhisperPrefix(String p) {
            prefix = p;
        }
    }
}

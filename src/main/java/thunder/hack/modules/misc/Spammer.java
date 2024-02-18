package thunder.hack.modules.misc;

import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class Spammer extends Module {
    public static ArrayList<String> SpamList = new ArrayList<>();
    public Setting<Boolean> global = new Setting<>("global", true);
    public Setting<Integer> delay = new Setting<>("delay", 5, 1, 30);
    private final Timer timer_delay = new Timer();

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

    @Override
    public void onEnable() {
        loadSpammer();
    }

    @Override
    public void onUpdate() {
        if (timer_delay.passedS(delay.getValue())) {
            if (SpamList.isEmpty()) {
                disable(MainSettings.isRu() ? "Файл spammer пустой!" : "The spammer file is empty!");
                return;
            }
            String c = SpamList.get(new Random().nextInt(SpamList.size()));
            if (c.charAt(0) == '/') {
                c = c.replace("/", "");
                mc.player.networkHandler.sendCommand(c);
            } else mc.player.networkHandler.sendChatMessage(global.getValue() ? "!" + c : c);

            timer_delay.reset();
        }
    }
}

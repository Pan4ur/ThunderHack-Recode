package thunder.hack.modules.misc;

import thunder.hack.cmd.Command;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Spammer extends Module {

    public static ArrayList<String> SpamList = new ArrayList<>();
    public Setting<Boolean> global = new Setting<>("global", true);
    public Setting<Integer> delay = new Setting<>("delay", 5, 1, 30);
    private final Setting<ModeEn> Mode = new Setting("Mode", ModeEn.API);
    private final Timer timer_delay = new Timer();
    private String word_from_api = "-";

    public Spammer() {
        super("Spammer", "спаммер", Category.MISC);
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
                                if (spamChunk.length() > 0) {
                                    spamList.add(spamChunk.toString());
                                    spamChunk = new StringBuilder();
                                }
                            } else {
                                spamChunk.append(l).append(" ");
                            }
                        }
                        spamList.add(spamChunk.toString());
                    } else {
                        spamList.addAll(lines);
                    }
                    SpamList = spamList;
                } catch (Exception e) {
                    System.err.println("Could not load file ");
                }
            }).start();
        } catch (IOException e) {
            System.err.println("Could not load file ");
        }

    }

    @Override
    public void onEnable() {
        loadSpammer();
    }

    @Override
    public void onUpdate() {
        if (timer_delay.passedS(delay.getValue())) {
            if (Mode.getValue() != ModeEn.Custom) {
                getMsg();
                if (!Objects.equals(word_from_api, "-")) {
                    word_from_api = word_from_api.replace("<p>", "");
                    word_from_api = word_from_api.replace("</p>", "");
                    word_from_api = word_from_api.replace(".", "");
                    word_from_api = word_from_api.replace(",", "");
                    mc.player.networkHandler.sendCommand(global.getValue() ? "!" + word_from_api : word_from_api);
                }
            } else {
                if (SpamList.isEmpty()) {
                    Command.sendMessage("Файл spammer пустой!");
                    this.toggle();
                    return;
                }
                String c = SpamList.get(new Random().nextInt(SpamList.size()));
                if(word_from_api.charAt(0) == '/'){
                    c = c.replace("/","");
                    mc.player.networkHandler.sendCommand(global.getValue() ? "!" + c : c);
                } else {
                    mc.player.networkHandler.sendChatMessage(global.getValue() ? "!" + c : c);
                }
            }
            timer_delay.reset();
        }
    }

    public void getMsg() {
        new Thread(() -> {
            try {
                URL api = new URL("https://fish-text.ru/get?format=html&number=1");
                BufferedReader in = new BufferedReader(new InputStreamReader(api.openStream(), StandardCharsets.UTF_8));
                String inputLine;
                if ((inputLine = in.readLine()) != null) {
                    word_from_api = inputLine;
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    public enum ModeEn {
        Custom,
        API
    }
}

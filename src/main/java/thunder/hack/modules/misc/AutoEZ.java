package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.DeathEvent;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.Aura;
import thunder.hack.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;
import thunder.hack.utility.ThunderUtility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static thunder.hack.modules.client.MainSettings.isRu;

public final class AutoEZ extends Module {
    public static ArrayList<String> EZWORDS = new ArrayList<>();
    public Setting<Boolean> global = new Setting<>("global", true);

    String[] EZ = new String[]{
            "%player% ПОЗОРИЩЕЕЕЕЕ ДАЖЕ БОТИХА НА ХЕЛЛРАЙДЕРЕ ВГЕТАЛА БАФЫ",
            "%player% АНБРЕЙН ГЕТАЙ ТХ РЕКОД",
            "%player% ТВОЯ МАТЬ БУДЕТ СЛЕДУЮЩЕЙ))))",
            "%player% БИЧАРА БЕЗ ТХ",
            "%player% ЧЕ ТАК БЫСТРО СЛИЛСЯ ТО А?",
            "%player% ПЛАЧЬ",
            "%player% УПССС ЗАБЫЛ КИЛЛКУ ВЫРУБИТЬ",
            "ОДНОКЛЕТОЧНЫЙ %player% БЫЛ ВПЕНЕН",
            "%player% ИЗИ БЛЯТЬ АХААХАХАХАХААХ",
            "%player% БОЖЕ МНЕ ТЕБЯ ЖАЛКО ВГЕТАЙ ТХ",
            "%player% ОПРАВДЫВАЙСЯ В ХУЙ ЧЕ СДОХ ТО)))",
            "%player% СПС ЗА ОТСОС)))"
    };

    private final Setting<ModeEn> mode = new Setting<>("Mode", ModeEn.Basic);
    private final Setting<ServerMode> server = new Setting<>("Server", ServerMode.Universal);

    private static AutoEZ instance;

    public AutoEZ() {
        super("AutoEZ", Category.MISC);
        loadEZ();
        instance = this;
    }

    public static void loadEZ() {
        try {
            File file = new File("ThunderHackRecode/misc/AutoEZ.txt");
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
                    EZWORDS.clear();
                    ArrayList<String> spamList = new ArrayList<>();
                    if (newline) {
                        StringBuilder spamChunk = new StringBuilder();
                        for (String l : lines) {
                            if (l.equals("")) {
                                if (spamChunk.length() > 0) {
                                    spamList.add(spamChunk.toString());
                                    spamChunk = new StringBuilder();
                                }
                            } else spamChunk.append(l).append(" ");
                        }
                        spamList.add(spamChunk.toString());
                    } else spamList.addAll(lines);

                    EZWORDS = spamList;
                } catch (Exception ignored) {
                }
            }).start();
        } catch (IOException ignored) {
        }
    }

    public static AutoEZ getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        loadEZ();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (fullNullCheck()) return;
        if (server.getValue() == ServerMode.Universal) return;
        if (e.getPacket() instanceof GameMessageS2CPacket) {
            final GameMessageS2CPacket packet = e.getPacket();
            if (packet.content().getString().contains("Вы убили игрока")) {
                String name = ThunderUtility.solveName(packet.content().getString());
                if (Objects.equals(name, "FATAL ERROR")) return;

                String finalword;
                if (mode.getValue() == ModeEn.Basic) {
                    int n;
                    n = (int) Math.floor(Math.random() * EZ.length);
                    finalword = EZ[n].replace("%player%", name);
                } else {
                    if (EZWORDS.isEmpty()) {
                        sendMessage(isRu() ? "Файл с AutoEZ пустой!" : "AutoEZ.txt is empty!");
                        return;
                    }
                    finalword = EZWORDS.get(new Random().nextInt(EZWORDS.size()));
                    finalword = finalword.replaceAll("%player%", name);
                }
                mc.player.networkHandler.sendChatMessage(global.getValue() ? "!" + finalword : finalword);
            }
        }
    }

    @EventHandler
    public void onDeath(DeathEvent e) {
        if (server.getValue() != ServerMode.Universal) return;
        if (Aura.target != null && Aura.target == e.getPlayer()) {
            sayEZ(e.getPlayer().getName().getString());
            return;
        }
        if (AutoCrystal.target != null && AutoCrystal.target == e.getPlayer())
            sayEZ(e.getPlayer().getName().getString());
    }

    public void sayEZ(String pn) {
        String finalword;
        if (mode.getValue() == ModeEn.Basic) {
            int n;
            n = (int) Math.floor(Math.random() * EZ.length);
            finalword = EZ[n].replace("%player%", pn);
        } else {
            if (EZWORDS.isEmpty()) {
                sendMessage(isRu() ? "Файл с AutoEZ пустой!" : "AutoEZ.txt is empty!");
                return;
            }
            finalword = EZWORDS.get(new Random().nextInt(EZWORDS.size()));
            finalword = finalword.replaceAll("%player%", pn);
        }
        mc.player.networkHandler.sendChatMessage(global.getValue() ? "!" + finalword : finalword);
    }

    public enum ModeEn {
        Custom,
        Basic
    }

    public enum ServerMode {
        Universal,
        FunnyGame
    }
}
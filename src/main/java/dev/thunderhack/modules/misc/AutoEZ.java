package dev.thunderhack.modules.misc;

import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.MainSettings;
import dev.thunderhack.modules.combat.Aura;
import dev.thunderhack.modules.combat.AutoCrystal;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.ThunderUtility;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class AutoEZ extends Module {
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


    public AutoEZ() {
        super("AutoEZ", Category.MISC);
        loadEZ();
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
                        sendMessage(MainSettings.isRu() ? "Файл с AutoEZ пустой!" : "AutoEZ.txt is empty!");
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
    public void onPacket(PacketEvent.Receive e) {
        if (!(e.getPacket() instanceof EntityStatusS2CPacket pac)) return;
        if (pac.getStatus() == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
            if (server.getValue() != ServerMode.Universal) return;
            if (Aura.target != null && Aura.target == pac.getEntity(mc.world)) {
                sayEZ(pac.getEntity(mc.world).getName().getString());
                return;
            }
            if (AutoCrystal.target != null && AutoCrystal.target == pac.getEntity(mc.world)) {
                sayEZ(pac.getEntity(mc.world).getName().getString());
            }
        }
    }

    public void sayEZ(String pn) {
        String finalword;
        if (mode.getValue() == ModeEn.Basic) {
            int n;
            n = (int) Math.floor(Math.random() * EZ.length);
            finalword = EZ[n].replace("%player%", pn);
        } else {
            if (EZWORDS.isEmpty()) {
                sendMessage(MainSettings.isRu() ? "Файл с AutoEZ пустой!" : "AutoEZ.txt is empty!");
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
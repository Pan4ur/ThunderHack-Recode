package dev.thunderhack.modules.misc;

import dev.thunderhack.event.events.EventAttack;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EbatteSratte extends Module {
    public EbatteSratte() {
        super("EbatteSratte", Module.Category.MISC);
        loadEZ();
    }

    Timer timer = new Timer();
    String chatprefix = "";
    public static ArrayList<String> words = new ArrayList<>();

    public Setting<Integer> delay = new Setting<>("Delay", 5, 1, 30);
    private Setting<Server> server = new Setting<>("Server", Server.FunnyGame);
    private Setting<Messages> mode = new Setting<>("Mode", Messages.Default);

    @Override
    public void onEnable() {
        loadEZ();
    }

    @EventHandler
    public void onAttackEntity(EventAttack event) {
        if (event.getEntity() instanceof PlayerEntity) {
            if (timer.passedS(delay.getValue())) {
                PlayerEntity entity = (PlayerEntity) event.getEntity();
                if (entity == null) return;

                int n;

                if (mode.getValue() == Messages.Default) n = (int) Math.floor(Math.random() * myString.length);
                else n = (int) Math.floor(Math.random() * words.size());

                chatprefix = switch (server.getValue()) {
                    case FunnyGame -> "!";
                    case OldServer -> ">";
                    case DirectMessage -> "/w ";
                    case Local -> "";
                };

                mc.player.networkHandler.sendChatMessage(chatprefix + entity.getName().getString() + " " +
                        (mode.getValue() == Messages.Default ? myString[n] : words.get(n))
                );

                timer.reset();
            }
        }
    }

    public static void loadEZ() {
        try {
            File file = new File("ThunderHackRecode/misc/EbatteSratte.txt");
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
                    words.clear();
                    ArrayList<String> spamList = new ArrayList<>();
                    if (newline) {
                        StringBuilder spamChunk = new StringBuilder();
                        for (String l : lines) {
                            if (l.equals("")) {
                                if (!spamChunk.isEmpty()) {
                                    spamList.add(spamChunk.toString());
                                    spamChunk = new StringBuilder();
                                }
                            } else spamChunk.append(l).append(" ");
                        }
                        spamList.add(spamChunk.toString());
                    } else spamList.addAll(lines);

                    words = spamList;
                } catch (Exception ignored) {}
            }).start();
        } catch (IOException ignored) {}
    }

    String[] myString = new String[]{"Я TBOЮ MATЬ БЛЯTЬ ПOДВEСИЛ НА КОЛ ОНА EБAHAЯ БЛЯДИHA",
            "МАМАШУ ТВОЮ АРМАТУРОЙ С ШИПАМИ ПО ХРЕБТУ ПИЗДИЛ",
            "Я ТВОЕЙ МАТЕРИ ПИЗДАК РАЗОРВАЛ СЫН БЛЯДИНЫ ТЫ ЕБАННОЙ",
            "ВГЕТАЙ ТАНДЕРХАК СЫН ЕБАННОЙ ШЛЮХИ",
            "ТЫ ПСИНА БЕЗ БРЕЙНА ДАВАЙ ТЕРПИ ТЕРПИ",
            "я твою мать об стол xуяpил сын тупорылой овчарки мать продал чит на кубики купил?",
            "СКУЛИ СВИHЬЯ ЕБAHAЯ , Я ТВОЮ MATЬ ПОДBECИЛ НА ЦЕПЬ И С ОКНА СБРОСИЛ ОНА ФЕМИНИСТКА ЕБАHAЯ ОНА СВОИМ ВЕСОМ 180КГ ПРОБУРИЛАСЬ ДО ЯДРА ЗЕМЛИ И СГОРЕЛА HAXУЙ АХАХАХАХА ЕБATЬ ОНА ГОРИТ ПРИКОЛЬНО",
            "ты мейн сначало свой пукни потом чет овысирай, с основы пиши нищ",
            "БАБКА СДОХЛА ОТ СТАРОСТИ Т.К. КОГДА ТВОЮ МATЬ РОДИЛИ ЕЙ БЫЛО 99 ЛЕТ И ОТ НЕРВОВ РАДОСТИ ОНА СДОХЛА ОЙ БЛ9TЬ ОТ РАДОСТИ ДЕД ТОЖЕ ОТ РАДОСТИ СДОХ HAXУЙ ДOЛБAЁБ EБAHЫЙ ЧТОБЫ ВЫЖИТЬ НА ПОМОЙКА МATЬ ТВOЯ ПOКА НЕ СДОХЛА EБAЛAСЬ С МУЖИКАМИ ЗА 2 КОПЕЙКИ",
            "ТЫ ПОНИМАЕШЬ ЧТО Я ТВОЮ МАТЬ ОТПРАВИЛ СО СВОЕГО XУЯ В НЕБО, ЧТОБ ОНА СВОИМ ПИЗДAKOМ ПРИНИМАЛА МИТЕОРИТНУЮ АТАКУ?)",
            "ТЫ ПОНИМАЕШЬ ЧТО ТBОЯ МATЬ СИДИТ У МЕНЯ НА ЦЕПИ И КАК БУЛЬДОГ EБАHЫЙ НА МОЙ XУЙ СЛЮНИ БЛ9ДЬ ПУСКАЕТ?))",
            "В ДЕТДОМЕ ТЕБЯ ПИЗДUЛИ ВСЕ КТО МОГ В ИТОГЕ ТЫ СДОХ НА УЛИЦЕ В 13 ЛЕТ ОТ НЕДОСТАТКА ЕДЫ ВОДУ ТЫ ЖЕ БРАЛ ЭТИМ ФИЛЬТРОМ И МОЧОЙ ДOЛБAЁБ ЕБAHЫЙ СУКA БЕЗ МATEPHAЯ ХУETА.",
            "Чё как нищий, купи тандерхак не позорься",
            "Your mom owned by Thunderhack recode",
            "АЛО БОМЖАТИНА БЕЗ МАТЕРИ Я ТВОЮ МАТЬ ОБ СТОЛ УБИЛ ЧЕРЕП ЕЙ РАЗБИЛ НОГОЙ БАТЮ ТВОЕГО С ОКНА ВЫКИНУЛ СУКА ЧМО ЕБАННОЕ ОТВЕТЬ ЧМО ЕБЛАН ТВАРЬ ШАЛАВА"
    };

    public enum Server {
        FunnyGame, DirectMessage, OldServer, Local;
    }

    public enum Messages {
        Default, Custom;
    }
}
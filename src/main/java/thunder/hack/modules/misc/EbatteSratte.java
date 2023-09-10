package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EbatteSratte extends Module {
    public EbatteSratte() {
        super("EbatteSratte", "авто токсик и не только xD", Module.Category.MISC);
        loadEZ();
    }

    Timer timer = new Timer();
    String chatprefix = "";
    public static ArrayList<String> words = new ArrayList<>();

    public Setting<Integer> delay = new Setting<>("Delay", 5, 1, 30);
    private Setting<mode> Mode = new Setting("Server", mode.FunnyGame);

    public enum mode {
        FunnyGame, DirectMessage, OldServer, Local;
    }

    private Setting<mode2> Mode2 = new Setting("Mode", mode2.Default);

    public enum mode2 {
        Default, Custom;
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

    @EventHandler
    public void onAttackEntity(EventAttack event) {
        if (event.getEntity() instanceof PlayerEntity) {
            if (timer.passedS(delay.getValue())) {
                PlayerEntity entity = (PlayerEntity) event.getEntity();
                if (entity == null) return;

                int n = 0;

                if (Mode2.getValue() == mode2.Default) {
                    n = (int) Math.floor(Math.random() * myString.length);
                } else {
                    n = (int) Math.floor(Math.random() * words.size());
                }

                if (Mode.getValue() == mode.FunnyGame) {
                    chatprefix = ("!");
                }
                if (Mode.getValue() == mode.OldServer) {
                    chatprefix = (">");
                }
                if (Mode.getValue() == mode.DirectMessage) {
                    chatprefix = ("/w ");
                }

                if (Mode2.getValue() == mode2.Default) {
                    mc.player.networkHandler.sendChatMessage(chatprefix + entity.getName().getString() + " " + myString[n]);
                } else  {
                    mc.player.networkHandler.sendChatMessage(chatprefix + entity.getName().getString() + " " + words.get(n));
                }
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

                    words = spamList;
                } catch (Exception ignored) {}
            }).start();
        } catch (IOException ignored) {}
    }

    @Override
    public void onEnable() {
        loadEZ();
    }
}
package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IGameMessageS2CPacket;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.text.SimpleDateFormat;
import java.util.*;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class ChatUtils extends Module {
    private final Setting<Welcomer> welcomer = new Setting<>("Welcomer", Welcomer.Off);
    private final Setting<Prefix> prefix = new Setting<>("Prefix", Prefix.None);
    private final Setting<Boolean> totems = new Setting<>("Totems", false);
    private final Setting<Boolean> time = new Setting<>("Time", false);
    private final Setting<Boolean> mention = new Setting<>("Mention", false);
    private final Setting<PMSound> pmSound = new Setting<>("PMSound", PMSound.Default);
    private final Setting<Boolean> zov = new Setting<>("ZOV", false);
    private final Setting<Boolean> wavy = new Setting<>("wAvY", false);
    private final Setting<Boolean> translit = new Setting<>("Translit", false);
    private final Setting<Boolean> antiCoordLeak = new Setting<>("AntiCoordLeak", false);

    private final Timer timer = new Timer();
    private final Timer antiSpam = new Timer();
    private final Timer messageTimer = new Timer();
    private final LinkedHashMap<UUID, String> nameMap = new LinkedHashMap<>();
    private String skip;

    Map<String, String> ruToEng = Map.ofEntries(
            Map.entry("а", "a"),
            Map.entry("б", "6"),
            Map.entry("в", "B"),
            Map.entry("г", "r"),
            Map.entry("д", "d"),
            Map.entry("е", "e"),
            Map.entry("ё", "e"),
            Map.entry("ж", ">I<"),
            Map.entry("з", "3"),
            Map.entry("и", "u"),
            Map.entry("й", "u"),
            Map.entry("к", "k"),
            Map.entry("л", "JI"),
            Map.entry("м", "m"),
            Map.entry("н", "H"),
            Map.entry("о", "o"),
            Map.entry("п", "n"),
            Map.entry("р", "p"),
            Map.entry("с", "c"),
            Map.entry("т", "T"),
            Map.entry("у", "y"),
            Map.entry("ф", "f"),
            Map.entry("х", "x"),
            Map.entry("ц", "lI"),
            Map.entry("ч", "4"),
            Map.entry("ш", "w"),
            Map.entry("щ", "w"),
            Map.entry("ь", "b"),
            Map.entry("ы", "bI"),
            Map.entry("ъ", "b"),
            Map.entry("э", "-)"),
            Map.entry("ю", "I-O"),
            Map.entry("я", "9I")
    );

    private final String[] bb = new String[]{
            "See you later, ",
            "Catch ya later, ",
            "See you next time, ",
            "Farewell, ",
            "Bye, ",
            "Good bye, ",
            "Later, "
    };

    private final String[] qq = new String[]{
            "Good to see you, ",
            "Greetings, ",
            "Hello, ",
            "Howdy, ",
            "Hey, ",
            "Good evening, ",
            "Welcome to SERVERIP1D5A9E, "
    };

    private final String[] popMessages = new String[]{
            " EZZZ POP <pop> TIMES PIECE OF SHIT GET GOOD",
            " ez pop <pop> times fuckin unbrain",
            " pop <pop> times get good kiddo ",
            " EZZZZZZZ pop <pop> times GO LEARN PVP PUSSY",
            " piece of shit popped <pop> times so ez",
            " easiest pop <pop> times in my life",
            " HAHAHAHA BRO POPPED <pop> TIMES SO EZ LMAO",
            " POP <pop> TIMES OMG MAN UR SO BAD LMAO",
            " my grandma has more skill than you nigga pop <pop> times",
            " trash pop <pop> times retard ",
            " ezz no skill dog pop <pop> times",
            " lame dude tryes to pvp with me but dyes) hahah pop <pop> times",
            " get better tbh bruh pop <pop> times",
            " pop <pop> times ur eyes don't work right? ",
            " cringelord popped <pop> times so ez "
    };


    public ChatUtils() {
        super("ChatUtils", Category.MISC);
    }

    @Override
    public void onDisable() {
        nameMap.clear();
    }

    @Override
    public void onUpdate() {
        if (timer.passedMs(15000)) {
            for (PlayerListEntry b : mc.player.networkHandler.getPlayerList()) {
                if (!nameMap.containsKey(b.getProfile().getId())) {
                    nameMap.put(b.getProfile().getId(), b.getProfile().getName());
                }
            }
            timer.reset();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (welcomer.getValue() != Welcomer.Off && antiSpam.passedMs(3000)) {
            if (event.getPacket() instanceof PlayerListS2CPacket pck) {
                int n2 = (int) Math.floor(Math.random() * qq.length);
                String string1;
                if (mc.player.networkHandler.getServerInfo() != null) {
                    string1 = qq[n2].replace("SERVERIP1D5A9E", mc.player.networkHandler.getServerInfo().address);
                } else string1 = "server";
                if (pck.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                    for (PlayerListS2CPacket.Entry ple : pck.getPlayerAdditionEntries()) {
                        if (antiBot(ple.profile().getName())) return;
                        if (Objects.equals(ple.profile().getName(), mc.player.getName().getString())) return;
                        if (welcomer.getValue() == Welcomer.Server) {
                            mc.player.networkHandler.sendChatMessage(getPrefix() + string1 + ple.profile().getName());
                            antiSpam.reset();
                        } else sendMessage(string1 + ple.profile().getName());
                        nameMap.put(ple.profile().getId(), ple.profile().getName());
                    }
                }
            }

            if (event.getPacket() instanceof PlayerRemoveS2CPacket pac) {
                for (UUID uuid2 : pac.profileIds) {
                    if (!nameMap.containsKey(uuid2)) return;
                    if (antiBot(nameMap.get(uuid2))) return;
                    if (Objects.equals(nameMap.get(uuid2), mc.player.getName().getString())) return;
                    int n = (int) Math.floor(Math.random() * bb.length);
                    if (welcomer.getValue() == Welcomer.Server) {
                        mc.player.networkHandler.sendChatMessage(getPrefix() + bb[n] + nameMap.get(uuid2));
                        antiSpam.reset();
                    } else sendMessage(bb[n] + nameMap.get(uuid2));
                    nameMap.remove(uuid2);
                }
            }
        }
        if (event.getPacket() instanceof GameMessageS2CPacket pac) {
            if (time.getValue()) {
                IGameMessageS2CPacket pac2 = event.getPacket();
                pac2.setContent(Text.of("[" + Formatting.GRAY + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + Formatting.RESET + "] ").copy().append(pac.content));
            }

            if (mention.getValue()) {
                if (pac.content.getString().contains(mc.player.getName().getString()) && messageTimer.passedMs(1000)) {
                    Managers.NOTIFICATION.publicity("ChatUtils", isRu() ? "Тебя помянули в чате!" : "You were mentioned in the chat!", 4, Notification.Type.WARNING);
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 5f, 1f);
                }
            }

            String content = pac.content.getString().toLowerCase();
            if (!pmSound.is(PMSound.Off) && (content.contains("whisper") || content.contains("-> я") || content.contains("-> " + NameProtect.getCustomName()) || content.contains("-> me") || content.contains(" says:"))) {
                Managers.SOUND.playPmSound(pmSound.getValue());
            }
        }
    }

    @EventHandler
    public void onTotem(TotemPopEvent e) {
        if (totems.getValue() && antiSpam.passedMs(3000) && e.getEntity() != mc.player) {
            int n = (int) Math.floor(Math.random() * popMessages.length);
            String s = popMessages[n].replace("<pop>", e.getPops() + "");
            mc.player.networkHandler.sendChatMessage(getPrefix() + e.getEntity().getName().getString() + s);
            antiSpam.reset();
        }
    }

    private @NotNull String getPrefix() {
        return switch (prefix.getValue()) {
            case Green -> ">";
            case Global -> "!";
            case None -> "";
        };
    }

    public boolean antiBot(@NotNull String s) {
        if (s.contains("soon_") || s.contains("_npc") || s.contains("CIT-")) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (Character.UnicodeBlock.of(s.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof ChatMessageC2SPacket pac) {
            if (antiCoordLeak.getValue() && pac.chatMessage.replaceAll("\\D", "").length() >= 6) {
                sendMessage("[ChatUtils] " + (isRu() ? "В сообщении содержатся координаты!" : "The message contains coordinates!"));
                e.cancel();
            }

            messageTimer.reset();
        }

        if (fullNullCheck()) return;
        if (e.getPacket() instanceof ChatMessageC2SPacket pac && (zov.getValue() || wavy.getValue() || translit.getValue())) {

            if (Objects.equals(pac.chatMessage(), skip)) {
                return;
            }

            if (mc.player.getMainHandStack().getItem() == Items.FILLED_MAP || mc.player.getOffHandStack().getItem() == Items.FILLED_MAP)
                return;

            if (pac.chatMessage().startsWith("/") || pac.chatMessage().startsWith(Managers.COMMAND.getPrefix()))
                return;

            String message = pac.chatMessage();
            if (zov.getValue()) {
                StringBuilder builder = new StringBuilder();
                for (char Z : message.toCharArray()) {
                    if ('З' == Z || 'з' == Z) {
                        builder.append("Z");
                    } else if ('В' == Z || 'в' == Z) {
                        builder.append("V");
                    } else {
                        builder.append(Z);
                    }
                }
                message = builder.toString();
            }
            if (wavy.getValue()) {
                StringBuilder builder = new StringBuilder();
                boolean up = false;
                for (char C : message.toCharArray()) {
                    if (up) {
                        builder.append(Character.toUpperCase(C));
                    } else {
                        builder.append(Character.toLowerCase(C));
                    }
                    up = Character.isLetter(C) != up;
                }
                message = builder.toString();
            }
            if (translit.getValue())
                message = transliterate(message);
            skip = message;
            mc.player.networkHandler.sendChatMessage(skip);
            e.cancel();
        }
    }

    public String transliterate(String text) {
        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            String str = ruToEng.get(ch + "");
            result.append(str != null ? str : ch);
        }

        return result.toString();
    }

    private enum Welcomer {
        Off, Server, Client
    }

    private enum Prefix {
        Green, Global, None
    }

    public enum PMSound {
        Off, Default, Custom
    }
}

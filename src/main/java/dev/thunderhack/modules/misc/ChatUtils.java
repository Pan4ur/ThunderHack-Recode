package dev.thunderhack.modules.misc;

import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.event.events.TotemPopEvent;
import dev.thunderhack.mixins.accesors.IGameMessageS2CPacket;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.MainSettings;
import dev.thunderhack.notification.Notification;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import dev.thunderhack.ThunderHack;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

public class ChatUtils extends Module {
    public ChatUtils() {
        super("ChatUtils", Category.MISC);
    }

    private final Setting<Welcomer> welcomer = new Setting("Welcomer", Welcomer.Off);
    private final Setting<Prefix> prefix = new Setting("Prefix", Prefix.None);
    public final Setting<Boolean> totems = new Setting<>("Totems", false);
    public final Setting<Boolean> time = new Setting<>("Time", false);
    public final Setting<Boolean> mention = new Setting<>("Mention", false);
    private final Setting<Boolean> antiCoordLeak = new Setting<>("AntiCoordLeak", false);

    private final Timer timer = new Timer();
    private final Timer antiSpam = new Timer();

    private final LinkedHashMap<UUID, String> nameMap = new LinkedHashMap<>();

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
    public void onPacketReceive(PacketEvent.Receive e) {
        if (welcomer.getValue() != Welcomer.Off && antiSpam.passedMs(3000)) {
            if (e.getPacket() instanceof PlayerListS2CPacket pck) {
                int n2 = (int) Math.floor(Math.random() * qq.length);
                String string1 = "server";
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
            if (e.getPacket() instanceof PlayerRemoveS2CPacket pac) {
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
        if (e.getPacket() instanceof GameMessageS2CPacket pac && time.getValue()) {
            IGameMessageS2CPacket pac2 = (IGameMessageS2CPacket) e.getPacket();
            pac2.setContent(Text.of("[" + Formatting.GRAY + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + Formatting.RESET + "] ").copy().append(pac.content));
        }

        if (e.getPacket() instanceof GameMessageS2CPacket pac && mention.getValue()) {
            if (pac.content.getString().contains(mc.player.getName().getString())) {
                ThunderHack.notificationManager.publicity("ChatUtils", MainSettings.language.getValue() == MainSettings.Language.RU ? "Тебя помянули в чате!" : "You were mentioned in the chat!", 4, Notification.Type.WARNING);
                mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 5f, 1f);
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof ChatMessageC2SPacket pac && antiCoordLeak.getValue()) {
            if (antiCoordLeak.getValue() && pac.chatMessage.replaceAll("\\D", "").length() >= 6) {
                sendMessage("[ChatUtils] " + (MainSettings.language.getValue() == MainSettings.Language.RU ? "В сообщении содержатся координаты!" : "The message contains coordinates!"));
                e.cancel();
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

    private String getPrefix() {
        if (prefix.getValue() == Prefix.Green) return ">";
        if (prefix.getValue() == Prefix.Global) return "!";
        return "";
    }

    public boolean antiBot(String s) {
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

    private enum Welcomer {
        Off, Server, Client
    }

    private enum Prefix {
        Green, Global, None
    }
}

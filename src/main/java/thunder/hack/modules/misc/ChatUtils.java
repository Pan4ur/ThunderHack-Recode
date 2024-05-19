package thunder.hack.modules.misc;

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
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.injection.accesors.IGameMessageS2CPacket;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.ClientSettings;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

public class ChatUtils extends Module {
    private final Setting<Welcomer> welcomer = new Setting<>("Welcomer", Welcomer.Off);
    private final Setting<Prefix> prefix = new Setting<>("Prefix", Prefix.None);
    private final Setting<Boolean> totems = new Setting<>("Totems", false);
    private final Setting<Boolean> time = new Setting<>("Time", false);
    private final Setting<Boolean> mention = new Setting<>("Mention", false);
    private final Setting<PMSound> pmSound = new Setting<>("PMSound", PMSound.Default);
    private final Setting<Boolean> zov = new Setting<>("ZOV", false);
    private final Setting<Boolean> wavy = new Setting<>("wAvY",false);
    private final Setting<Boolean> antiCoordLeak = new Setting<>("AntiCoordLeak", false);

    private final Timer timer = new Timer();
    private final Timer antiSpam = new Timer();
    private final Timer messageTimer = new Timer();

    private final LinkedHashMap<UUID, String> nameMap = new LinkedHashMap<>();
    private String skip;

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
                    ThunderHack.notificationManager.publicity("ChatUtils", ClientSettings.language.getValue() == ClientSettings.Language.RU ? "Тебя помянули в чате!" : "You were mentioned in the chat!", 4, Notification.Type.WARNING);
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 5f, 1f);
                }
            }

            String content = pac.content.getString().toLowerCase();
            if(!pmSound.is(PMSound.Off) && (content.contains("whisper") || content.contains("-> я") || content.contains("-> " + NameProtect.getCustomName()) || content.contains("-> me") || content.contains(" says:"))) {
                ThunderHack.soundManager.playPmSound(pmSound.getValue());
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
        return switch (prefix.getValue()){
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

    @EventHandler
    public void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof ChatMessageC2SPacket pac) {
            if (antiCoordLeak.getValue() && pac.chatMessage.replaceAll("\\D", "").length() >= 6) {
                sendMessage("[ChatUtils] " + (ClientSettings.language.getValue() == ClientSettings.Language.RU ? "В сообщении содержатся координаты!" : "The message contains coordinates!"));
                e.cancel();
            }

            messageTimer.reset();
        }

        if (fullNullCheck()) return;
        if (e.getPacket() instanceof ChatMessageC2SPacket pac && (zov.getValue() || wavy.getValue())) {

            if (Objects.equals(pac.chatMessage(), skip)) {
                return;
            }

            if (mc.player.getMainHandStack().getItem() == Items.FILLED_MAP || mc.player.getOffHandStack().getItem() == Items.FILLED_MAP)
                return;

            if (pac.chatMessage().startsWith("/") || pac.chatMessage().startsWith(ThunderHack.commandManager.getPrefix()))
                return;

            String message = pac.chatMessage();
            if(zov.getValue()){
                StringBuilder builder = new StringBuilder();
                for(char Z:message.toCharArray()){
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
            if(wavy.getValue()){
                StringBuilder builder = new StringBuilder();
                boolean up = false;
                for(char C : message.toCharArray()){
                    if(up){
                        builder.append(Character.toUpperCase(C));
                    }
                    else{
                        builder.append(Character.toLowerCase(C));
                    }
                    up = Character.isLetter(C) != up;
                }
                message = builder.toString();
            }
            skip = message;
            mc.player.networkHandler.sendChatMessage(skip);
            e.cancel();
        }
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

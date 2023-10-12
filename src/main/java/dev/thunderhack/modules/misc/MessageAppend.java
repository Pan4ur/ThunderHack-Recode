package dev.thunderhack.modules.misc;

import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import dev.thunderhack.ThunderHack;

import java.util.Objects;

public class MessageAppend extends Module {

    public Setting<String> word = new Setting<>("word", " TH RECODE");

    public MessageAppend() {
        super("MessageAppend", Category.MISC);
    }

    String skip;

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (fullNullCheck()) return;
        if (e.getPacket() instanceof ChatMessageC2SPacket pac) {
            if (Objects.equals(pac.chatMessage(), skip)) {
                return;
            }

            // Чтоб не добавляло когда ты вводишь капчу на сервере
            if(mc.player.getMainHandStack().getItem() == Items.FILLED_MAP || mc.player.getOffHandStack().getItem() == Items.FILLED_MAP)
                return;

            if (pac.chatMessage().startsWith("/") || pac.chatMessage().startsWith(ThunderHack.commandManager.getPrefix()))
                return;

            skip = pac.chatMessage() + word.getValue();
            mc.player.networkHandler.sendChatMessage(pac.chatMessage() + word.getValue());
            e.cancel();
        }
    }
}
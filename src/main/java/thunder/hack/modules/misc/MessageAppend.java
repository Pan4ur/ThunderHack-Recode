package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

import java.util.Objects;

public class MessageAppend extends Module {
    private final Setting<String> word = new Setting<>("word", " TH RECODE");
    private String skip;

    public MessageAppend() {
        super("MessageAppend", Category.MISC);
    }


    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (fullNullCheck()) return;
        if (e.getPacket() instanceof ChatMessageC2SPacket pac) {
            if (Objects.equals(pac.chatMessage(), skip)) {
                return;
            }

            // Чтоб не добавляло когда ты вводишь капчу на сервере
            if (mc.player.getMainHandStack().getItem() == Items.FILLED_MAP || mc.player.getOffHandStack().getItem() == Items.FILLED_MAP)
                return;

            if (pac.chatMessage().startsWith("/") || pac.chatMessage().startsWith(ThunderHack.commandManager.getPrefix()))
                return;

            skip = pac.chatMessage() + word.getValue();
            mc.player.networkHandler.sendChatMessage(pac.chatMessage() + word.getValue());
            e.cancel();
        }
    }
}
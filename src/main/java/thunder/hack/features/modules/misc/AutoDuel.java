package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class AutoDuel extends Module {
    public AutoDuel() {
        super("AutoDuel", Module.Category.MISC);
    }
    private final Setting<Boolean> send = new Setting<>("Send", false);
    private final Setting<Boolean> accept = new Setting<>("Accept", false);
    private final Setting<String> nickname = new Setting<>("Nickname", "06ED");
    private final  Setting<Float> delay = new Setting<>("Delay", 2f, 0f, 30f);
    private final Timer timer = new Timer();
    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof GameMessageS2CPacket pac){
            if(send.getValue()) {
                if (pac.content.getString().contains("[Duels]") && pac.content.getString().contains(nickname.toString()) && pac.content.getString().contains(mc.player.getName().toString())) {
                    sendChatCommand("duel " + nickname.getValue());
                    if (timer.passedMs((long) (1000 * delay.getValue()))) {
                        clickSlot(0);
                        timer.reset();
                    }
                }
            }
            else if(accept.getValue()){
                if(pac.content.getString().contains("Duel request received from " + nickname.getValue())){
                    sendChatCommand("duel accept " + nickname.getValue());
                }
            }
        }
    }
}

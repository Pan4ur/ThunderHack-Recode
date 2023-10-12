package dev.thunderhack.modules.client;

import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.mixins.accesors.IGameMessageS2CPacket;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class Media extends Module {
    public static final Setting<Boolean> skinProtect = new Setting<>("Skin Protect", true);
    public static final Setting<Boolean> nickProtect = new Setting<>("Nick Protect", true);

    public Media() {
        super("Media", Category.CLIENT);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive e) {
        if (e.getPacket() instanceof GameMessageS2CPacket pac && nickProtect.getValue()) {
            for (PlayerListEntry ple : mc.player.networkHandler.getPlayerList()) {
                if (pac.content().getString().contains(ple.getProfile().getName())) {
                    IGameMessageS2CPacket packet = e.getPacket();
                    packet.setContent(Text.of(pac.content().getString().replace(ple.getProfile().getName(), "Protected")));
                }
            }
        }
    }
}

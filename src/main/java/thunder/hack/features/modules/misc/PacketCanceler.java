package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class PacketCanceler extends Module {
    private final Setting<Boolean> cliclSlot = new Setting<>("ClickSlotC2SPacket", false);
    private final Setting<Boolean> playerMovePosAndOnGround = new Setting<>("PositionAndOnGround", false);
    private final Setting<Boolean> playerMoveOnGroundOnly = new Setting<>("OnGroundOnly", false);
    private final Setting<Boolean> playerMoveLookAndOnGround = new Setting<>("LookAndOnGround", false);
    public PacketCanceler() {
        super("PacketCanceler", Category.MISC);
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPacketSend(PacketEvent.@NotNull Send e) {
        if (e.getPacket() instanceof ClickSlotC2SPacket && cliclSlot.getValue()) {
            e.cancel();
        } else if (e.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround && playerMovePosAndOnGround.getValue()) {
            e.cancel();
        } else if (e.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly && playerMoveOnGroundOnly.getValue()) {
            e.cancel();
        } else if (e.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround && playerMoveLookAndOnGround.getValue()) {
            e.cancel();
        }
    }
}
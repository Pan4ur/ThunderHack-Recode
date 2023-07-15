package thunder.hack.cmd.impl;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import thunder.hack.cmd.Command;

public class VclipCommand extends Command {

    public VclipCommand() {
        super("vclip");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("Попробуй .vclip <число>");
            return;
        }
        if (commands.length == 2) {
            try {
                Command.sendMessage(Formatting.GREEN + "Клипаемся на " + Double.valueOf(commands[0]) + " блоков");
                for (int i = 0; i < 10; ++i) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
                }
                for (int i = 0; i < 10; ++i) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + Double.parseDouble(commands[0]), mc.player.getZ(), false));
                }
                mc.player.setPosition(mc.player.getX(), mc.player.getY() + Double.parseDouble(commands[0]), mc.player.getZ());
            } catch (Exception ignored) {}
        }
    }
}

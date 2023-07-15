package thunder.hack.cmd.impl;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import thunder.hack.cmd.Command;

import java.util.Objects;

public class HclipCommand extends Command {
    public HclipCommand() {
        super("hclip");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("Попробуй .hclip <число>, .hclip s");
            return;
        }
        if (commands.length == 2) {
            if(Objects.equals(commands[0], "s")){
                double x = -(MathHelper.sin(mc.player.getYaw() * MathHelper.RADIANS_PER_DEGREE) * 0.8);
                double z = MathHelper.cos(mc.player.getYaw() * MathHelper.RADIANS_PER_DEGREE) * 0.8;
                for(int i = 0; i < 10; i++){
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + x,mc.player.getY(),mc.player.getZ() + z,false));
                }
                mc.player.setPosition(mc.player.getX() + x, mc.player.getY(), mc.player.getZ() + z);
                return;
            }
            try {
                Command.sendMessage(Formatting.GREEN + "клипаемся на  " + Double.valueOf(commands[0]) + " блоков.");
                double speed = Double.valueOf(commands[0]);
                mc.player.setPosition(mc.player.getX() -((double) MathHelper.sin(mc.player.getYaw() * MathHelper.RADIANS_PER_DEGREE) * speed), mc.player.getY(), mc.player.getZ() + (double) MathHelper.cos(mc.player.getYaw() * MathHelper.RADIANS_PER_DEGREE) * speed);
            } catch (Exception exception) {
            }

        }
    }
}

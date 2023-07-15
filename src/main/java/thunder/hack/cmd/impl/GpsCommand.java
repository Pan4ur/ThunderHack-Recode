package thunder.hack.cmd.impl;

import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class GpsCommand extends Command {
    public GpsCommand() {
        super("gps");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage("Попробуй .gps off / .gps x y");
        } else if (commands.length == 2) {
            if (Objects.equals(commands[0], "off"))
                Thunderhack.gps_position = null;
        } else if (commands.length > 2) {
            BlockPos pos = new BlockPos(Integer.parseInt(commands[0]), 0, Integer.parseInt(commands[1]));
            Thunderhack.gps_position = pos;
            Command.sendMessage("GPS настроен на X: " + pos.getX() + " Z: " + pos.getZ());
        }
    }
}

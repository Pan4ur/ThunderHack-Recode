package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class GpsCommand extends Command {
    public GpsCommand() {
        super("gps");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("off").executes(context -> {
            Thunderhack.gps_position = null;
            return SINGLE_SUCCESS;
        }));

        builder.then(arg("x", IntegerArgumentType.integer())
                .then(arg("y", IntegerArgumentType.integer()).executes(context -> {
                    final int x = context.getArgument("x", Integer.class);
                    final int y = context.getArgument("y", Integer.class);
                    Thunderhack.gps_position = new BlockPos(x, 0, y);

                    sendMessage("GPS настроен на X: " + Thunderhack.gps_position.getX() + " Z: " + Thunderhack.gps_position.getZ());
                    return SINGLE_SUCCESS;
                })));

        builder.executes(context -> {
            sendMessage("Попробуй .gps off / .gps x y");
            return SINGLE_SUCCESS;
        });
    }
}

package thunder.hack.command.impl;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import thunder.hack.command.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class VClipCommand extends Command {
    public VClipCommand() {
        super("vclip");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("count", DoubleArgumentType.doubleArg()).executes(context -> {
            final double count = context.getArgument("count", Double.class);

            try {
                sendMessage(Formatting.GREEN + "Клипаемся на " + count + " блоков");

                for (int i = 0; i < 10; ++i) {
                    MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY(), MC.player.getZ(), false));
                }

                for (int i = 0; i < 10; ++i) {
                    MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(), MC.player.getY() + count, MC.player.getZ(), false));
                }

                MC.player.setPosition(MC.player.getX(), MC.player.getY() + count, MC.player.getZ());
            } catch (Exception ignored) {
            }

            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            sendMessage("Попробуй .vclip <число>");

            return SINGLE_SUCCESS;
        });
    }
}

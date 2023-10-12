package dev.thunderhack.cmd.impl;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.thunderhack.cmd.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HClipCommand extends Command {
    public HClipCommand() {
        super("hclip");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("s").executes(context -> {
            final double x = -(MathHelper.sin(MC.player.getYaw() * MathHelper.RADIANS_PER_DEGREE) * 0.8);
            final double z = MathHelper.cos(MC.player.getYaw() * MathHelper.RADIANS_PER_DEGREE) * 0.8;

            for (int i = 0; i < 10; i++) {
                MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX() + x, MC.player.getY(), MC.player.getZ() + z, false));
            }

            MC.player.setPosition(MC.player.getX() + x, MC.player.getY(), MC.player.getZ() + z);

            return SINGLE_SUCCESS;
        }));

        builder.then(arg("count", DoubleArgumentType.doubleArg()).executes(context -> {
            final double speed = context.getArgument("count", Double.class);

            try {
                sendMessage(Formatting.GREEN + "клипаемся на  " + speed + " блоков.");
                MC.player.setPosition(MC.player.getX() - ((double) MathHelper.sin(MC.player.getYaw() * MathHelper.RADIANS_PER_DEGREE) * speed), MC.player.getY(), MC.player.getZ() + (double) MathHelper.cos(MC.player.getYaw() * MathHelper.RADIANS_PER_DEGREE) * speed);
            } catch (Exception ignored) {
            }

            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            sendMessage("Попробуй .hclip <число>, .hclip s");
            return SINGLE_SUCCESS;
        });
    }
}

package dev.thunderhack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.cmd.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage("Commands: ");

            for (Command command : ThunderHack.commandManager.getCommands()) {
                sendMessageWithoutTH(Formatting.GRAY + ThunderHack.commandManager.getPrefix() + command.getName());
            }

            return SINGLE_SUCCESS;
        });
    }
}

package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage("Commands: ");

            for (Command command : Thunderhack.commandManager.getCommands()) {
                sendMessageWithoutTH(Formatting.GRAY + Thunderhack.commandManager.getPrefix() + command.getName());
            }

            return SINGLE_SUCCESS;
        });
    }
}

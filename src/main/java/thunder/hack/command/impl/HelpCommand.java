package thunder.hack.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.command.Command;
import thunder.hack.core.CommandManager2;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage("Commands: ");

            for (Command command : Thunderhack.commandManager2.getCommands()) {
                sendMessageWithoutTH(Formatting.GRAY + String.valueOf(CommandManager2.PREFIX) + command.getName());
            }

            return SINGLE_SUCCESS;
        });
    }
}

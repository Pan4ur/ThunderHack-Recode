package thunder.hack.cmd.impl;

import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import net.minecraft.util.Formatting;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] commands) {
        HelpCommand.sendMessage("Commands: ");
        for (Command command : Thunderhack.commandManager.getCommands()) {
            HelpCommand.sendMessageWithoutTH(Formatting.GRAY + Thunderhack.commandManager.getPrefix() + command.getName());
        }
    }
}


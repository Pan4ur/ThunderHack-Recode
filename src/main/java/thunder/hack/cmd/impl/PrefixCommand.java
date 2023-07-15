package thunder.hack.cmd.impl;

import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import net.minecraft.util.Formatting;

public class PrefixCommand
        extends Command {
    public PrefixCommand() {
        super("prefix");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            Command.sendMessage(Formatting.GREEN + "Текущий префикс:" + Thunderhack.commandManager.getPrefix());
            return;
        }
        Thunderhack.commandManager.setPrefix(commands[0]);
        Command.sendMessage("Префикс изменен на  " + Formatting.GRAY + commands[0]);
    }
}


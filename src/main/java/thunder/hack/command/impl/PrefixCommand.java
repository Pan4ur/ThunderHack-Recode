package thunder.hack.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.command.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        super("prefix");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage(Formatting.GREEN + "Текущий префикс:" + Thunderhack.commandManager.getPrefix());
            return SINGLE_SUCCESS;
        });
    }
}

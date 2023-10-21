package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage("Commands: ");

            ThunderHack.commandManager.getCommands().forEach(command ->
                    MC.player.sendMessage(Text.of(
                            Formatting.GRAY + ThunderHack.commandManager.getPrefix() + command.getName() + ": " + command.getDescription()
                    ))
            );

            return SINGLE_SUCCESS;
        });
    }
}

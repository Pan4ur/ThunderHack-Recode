package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            sendMessage("Commands: \n");

            AtomicBoolean flip = new AtomicBoolean(false);

            ThunderHack.commandManager.getCommands().forEach(command -> {
                        mc.player.sendMessage(Text.of(
                                (flip.get() ? Formatting.LIGHT_PURPLE : Formatting.DARK_PURPLE)
                                        + ThunderHack.commandManager.getPrefix()
                                        + (flip.get() ? Formatting.AQUA : Formatting.DARK_AQUA)
                                        + command.getName()
                                        + (command.getAliases().isEmpty() ? "" : " (" + command.getAliases() + ")")
                                        + Formatting.DARK_GRAY + " -> "
                                        + (flip.get() ? Formatting.WHITE : Formatting.GRAY)
                                        + command.getDescription()
                        ));
                        flip.set(!flip.get());
                    }
            );

            return SINGLE_SUCCESS;
        });
    }
}

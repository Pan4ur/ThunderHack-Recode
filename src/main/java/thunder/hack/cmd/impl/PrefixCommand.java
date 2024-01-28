package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.modules.client.MainSettings;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        super("prefix");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("set").then(arg("prefix", StringArgumentType.greedyString()).executes(context -> {
            String prefix = context.getArgument("prefix", String.class);
            ThunderHack.commandManager.setPrefix(prefix);
            if (MainSettings.language.getValue() == MainSettings.Language.RU) sendMessage(Formatting.GREEN + "Префикс изменен на " + prefix);
            else sendMessage(Formatting.GREEN + "Prefix changed to " + prefix);
            MainSettings.prefix.setValue(prefix);
            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) sendMessage(Formatting.GREEN + "Текущий префикс:" + ThunderHack.commandManager.getPrefix());
            else sendMessage(Formatting.GREEN + "Current prefix:" + ThunderHack.commandManager.getPrefix());
            return SINGLE_SUCCESS;
        });
    }
}

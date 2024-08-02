package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        super("prefix");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("set").then(arg("prefix", StringArgumentType.greedyString()).executes(context -> {
            String prefix = context.getArgument("prefix", String.class);
            Managers.COMMAND.setPrefix(prefix);
            if (ClientSettings.language.getValue() == ClientSettings.Language.RU) sendMessage(Formatting.GREEN + "Префикс изменен на " + prefix);
            else sendMessage(Formatting.GREEN + "Prefix changed to " + prefix);
            ClientSettings.prefix.setValue(prefix);
            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (ClientSettings.language.getValue() == ClientSettings.Language.RU) sendMessage(Formatting.GREEN + "Текущий префикс:" + Managers.COMMAND.getPrefix());
            else sendMessage(Formatting.GREEN + "Current prefix:" + Managers.COMMAND.getPrefix());
            return SINGLE_SUCCESS;
        });
    }
}

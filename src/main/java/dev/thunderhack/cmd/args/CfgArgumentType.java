package dev.thunderhack.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.modules.client.MainSettings;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CfgArgumentType implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = ThunderHack.configManager.getConfigList().stream().limit(5).toList();

    public static CfgArgumentType create() {
        return new CfgArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String config = reader.readString();
        if (!ThunderHack.configManager.getConfigList().contains(config)) throw new DynamicCommandExceptionType(
                name -> Text.literal(MainSettings.language.getValue().equals(MainSettings.Language.RU) ? "Конфига " + name.toString() + " не существует(" : "Config " + name.toString() + " does not exists(")
        ).create(config);

        return config;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ThunderHack.configManager.getConfigList(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

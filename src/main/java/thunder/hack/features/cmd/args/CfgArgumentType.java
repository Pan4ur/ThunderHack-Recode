package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.client.ClientSettings;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CfgArgumentType implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Managers.CONFIG.getConfigList().stream()
            .limit(5)
            .toList();

    public static CfgArgumentType create() {
        return new CfgArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String config = reader.readString();
        if (!Managers.CONFIG.getConfigList().contains(config)) throw new DynamicCommandExceptionType(
                name -> Text.literal(ClientSettings.language.getValue().equals(ClientSettings.Language.RU) ? "Конфига " + name.toString() + " не существует(" : "Config " + name.toString() + " does not exists(")
        ).create(config);

        return config;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Managers.CONFIG.getConfigList(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

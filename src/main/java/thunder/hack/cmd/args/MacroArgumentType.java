package thunder.hack.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import thunder.hack.Thunderhack;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.utility.Macro;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class MacroArgumentType implements ArgumentType<Macro> {
    private static final Collection<String> EXAMPLES = Thunderhack.macroManager.getMacros().stream().map(Macro::getName).limit(5).toList();

    public static MacroArgumentType create() {
        return new MacroArgumentType();
    }

    @Override
    public Macro parse(StringReader reader) throws CommandSyntaxException {
        Macro macro = Thunderhack.macroManager.getMacroByName(reader.readString());

        if (macro == null) throw new DynamicCommandExceptionType(
                name -> Text.literal(MainSettings.language.getValue().equals(MainSettings.Language.RU) ? "Макроса " + name.toString() + " не существует(" : "Macro with name " + name.toString() + " does not exists(")
        ).create(reader.readString());

        return macro;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Thunderhack.macroManager.getMacros().stream().map(Macro::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

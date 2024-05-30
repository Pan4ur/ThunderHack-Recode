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
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.MacroManager;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static thunder.hack.modules.client.ClientSettings.isRu;

public class MacroArgumentType implements ArgumentType<MacroManager.Macro> {
    private static final Collection<String> EXAMPLES = ThunderHack.macroManager.getMacros().stream().map(MacroManager.Macro::getName).limit(5).toList();

    public static MacroArgumentType create() {
        return new MacroArgumentType();
    }

    @Override
    public MacroManager.Macro parse(StringReader reader) throws CommandSyntaxException {
        MacroManager.Macro macro = ThunderHack.macroManager.getMacroByName(reader.readString());

        if (macro == null) throw new DynamicCommandExceptionType(
                name -> Text.literal(isRu() ? "Макроса " + name.toString() + " не существует(" : "Macro with name " + name.toString() + " does not exists(")
        ).create(reader.readString());

        return macro;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ThunderHack.macroManager.getMacros().stream().map(MacroManager.Macro::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

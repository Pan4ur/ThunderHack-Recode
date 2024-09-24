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
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class CategoryArgumentType implements ArgumentType<String> {
    private static final List<String> EXAMPLES = Managers.MODULE.getCategories().stream()
            .map(Module.Category::getName)
            .toList();

    public static CategoryArgumentType create() {
        return new CategoryArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String cat = reader.readString();
        if (!EXAMPLES.contains(cat)) throw new DynamicCommandExceptionType(
                name -> Text.literal(isRu() ? "Категории " + name.toString() + " не существует(" : "Category " + name.toString() + " does not exist :(")
        ).create(cat);
        return cat;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(EXAMPLES, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

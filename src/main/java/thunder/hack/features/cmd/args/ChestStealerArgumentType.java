package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class ChestStealerArgumentType implements ArgumentType<String> {
    private static final List<String> EXAMPLES = getRegistered().stream().limit(5).toList();

    public static ChestStealerArgumentType create() {
        return new ChestStealerArgumentType();
    }

    @Override
    public String parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String blockName = reader.readString();
        if (!getRegistered().contains(blockName)) throw new DynamicCommandExceptionType(
                name -> Text.literal(isRu() ? "Такого предмета нет!" : "There is no such item!")
        ).create(blockName);
        return blockName;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(getRegistered(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static @NotNull List<String> getRegistered() {
        List<String> result = new ArrayList<>();

        for (Block block : Registries.BLOCK) {
            result.add(block.getTranslationKey().replace("block.minecraft.",""));
        }
        for (Item item : Registries.ITEM) {
            result.add(item.getTranslationKey().replace("item.minecraft.",""));
        }
        return result;
    }
}

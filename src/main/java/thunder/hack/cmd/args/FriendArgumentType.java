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
import thunder.hack.modules.client.MainSettings;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendArgumentType implements ArgumentType<String> {
    private static final List<String> EXAMPLES = ThunderHack.friendManager.getFriends().stream().limit(5).toList();

    public static FriendArgumentType create() {
        return new FriendArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String friend = reader.readString();
        if (!ThunderHack.friendManager.isFriend(friend)) throw new DynamicCommandExceptionType(
                name -> Text.literal(MainSettings.language.getValue().equals(MainSettings.Language.RU) ? "Друга с именем " + name.toString() + " не существует(" : "Friend with name " + name.toString() + " does not exists(")
        ).create(friend);

        return friend;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ThunderHack.friendManager.getFriends(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

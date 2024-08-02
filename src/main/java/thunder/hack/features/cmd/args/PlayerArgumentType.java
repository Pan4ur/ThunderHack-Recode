package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static thunder.hack.core.manager.IManager.mc;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class PlayerArgumentType implements ArgumentType<PlayerListEntry> {
    private static final Collection<String> EXAMPLES = List.of("pan4ur", "06ED");

    public static PlayerArgumentType create() {
        return new PlayerArgumentType();
    }

    @Override
    public PlayerListEntry parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readString();

        final PlayerListEntry player = mc.getNetworkHandler().getPlayerList().stream()
                .filter(p -> name.equals(p.getProfile().getName()))
                .findFirst()
                .orElse(null);
        if (player == null) {
            throw new DynamicCommandExceptionType(nickname -> Text.literal(isRu() ? "Игрок " + nickname + " не в сети" : "Player " + nickname + " offline")).create(name);
        }
        return player;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(mc.getNetworkHandler().getPlayerList().stream().map(p -> p.getProfile().getName()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

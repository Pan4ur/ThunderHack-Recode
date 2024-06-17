package thunder.hack.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static thunder.hack.core.IManager.mc;
import static thunder.hack.modules.client.ClientSettings.isRu;

public class PlayerArgumentType implements ArgumentType<PlayerEntity> {
    private static final Collection<String> EXAMPLES = List.of("pan4ur", "06ED");

    public static PlayerArgumentType create() {
        return new PlayerArgumentType();
    }

    @Override
    public PlayerEntity parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readString();

        final PlayerEntity player = mc.world.getPlayers().stream()
                .filter(p -> name.equals(p.getName().getString()))
                .findFirst()
                .orElse(null);
        if (player == null) {
            throw new DynamicCommandExceptionType(nickname -> Text.literal(isRu() ? "Игрок " + nickname + " не в сети" : "Player " + nickname + " offline")).create(name);
        }

        return player;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(mc.world.getPlayers().stream().map(p -> p.getName().getString()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

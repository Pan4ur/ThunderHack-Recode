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
import thunder.hack.core.impl.WayPointManager;
import thunder.hack.modules.client.ClientSettings;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class WayPointArgumentType implements ArgumentType<WayPointManager.WayPoint> {
    private static final Collection<String> EXAMPLES = ThunderHack.wayPointManager.getWayPoints().stream()
            .map(WayPointManager.WayPoint::getName)
            .limit(5)
            .toList();

    public static WayPointArgumentType create() {
        return new WayPointArgumentType();
    }

    @Override
    public WayPointManager.WayPoint parse(StringReader reader) throws CommandSyntaxException {
        WayPointManager.WayPoint wp = ThunderHack.wayPointManager.getWayPointByName(reader.readString());

        if (wp == null) throw new DynamicCommandExceptionType(
                name -> Text.literal(ClientSettings.language.getValue().equals(ClientSettings.Language.RU) ? "Вейпоинта " + name.toString() + " не существует(" : "Waypoint " + name.toString() + " does not exists(")
        ).create(reader.readString());

        return wp;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ThunderHack.wayPointManager.getWayPoints().stream().map(WayPointManager.WayPoint::getName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

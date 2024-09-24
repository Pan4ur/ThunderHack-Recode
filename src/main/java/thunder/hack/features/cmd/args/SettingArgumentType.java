package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SettingArgumentType implements ArgumentType<String> {
    public static SettingArgumentType create() {
        return new SettingArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(getSettings(context.getArgument("module", Module.class)), builder);
    }

    public static List<String> getSettings(Module module) {
        List<String> result = new ArrayList<>();
        for (Setting<?> setting : module.getSettings()) {
            result.add(setting.getName());
        }
        return result;
    }
}
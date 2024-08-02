package thunder.hack.features.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public abstract class Command {
    protected static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    protected final List<String> names;
    private final String description;

    public Command(String... names) {
        this.names = Arrays.asList(names);
        this.description = "descriptions.commands." + this.names.get(0);
    }

    public abstract void executeBuild(LiteralArgumentBuilder<CommandSource> builder);

    public static void sendMessage(String message) {
        if (mc.player == null) return;
        mc.player.sendMessage(Text.of(thunder.hack.core.manager.client.CommandManager.getClientMessage() + " " + message));
    }

    protected static <T> @NotNull RequiredArgumentBuilder<CommandSource, T> arg(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static @NotNull LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public void register(CommandDispatcher<CommandSource> dispatcher) {
        for (String name : names) {
            LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(name);
            executeBuild(builder);
            dispatcher.register(builder);
        }
    }

    public String getName() {
        return names.get(0);
    }

    public String getAliases() {
        return String.join(", ", names.stream().filter(n -> !n.equals(names.get(0))).toList());
    }

    public String getDescription() {
        return I18n.translate(description);
    }
}

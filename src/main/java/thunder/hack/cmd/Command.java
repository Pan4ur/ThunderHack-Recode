package thunder.hack.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

public abstract class Command {
    protected static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
    protected static final MinecraftClient MC = MinecraftClient.getInstance();

    protected final List<String> names;

    public Command(String... names) {
        this.names = Arrays.asList(names);
    }

    public abstract void executeBuild(LiteralArgumentBuilder<CommandSource> builder);

    @Deprecated
    public static void sendMessage(String message) {
        if (MC.player == null) return;
        MC.player.sendMessage(Text.of(thunder.hack.core.CommandManager.getClientMessage() + " "  + message));
    }

    public static void sendMessageWithoutTH(String message) {
        if (MC.player == null) return;
        MC.player.sendMessage(Text.of(message));
    }


    protected static <T> RequiredArgumentBuilder<CommandSource, T> arg(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(final String name) {
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
}

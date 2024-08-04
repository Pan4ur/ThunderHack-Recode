package thunder.hack.core.manager.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.impl.*;
import thunder.hack.core.manager.IManager;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements IManager {
    private String prefix = "@";

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance());
    private final List<Command> commands = new ArrayList<>();

    public CommandManager() {
        add(new RpcCommand());
        add(new KitCommand());
        add(new GpsCommand());
        add(new CfgCommand());
        add(new RctCommand());
        add(new BindCommand());
        add(new DrawCommand());
        add(new HelpCommand());
        add(new NukerCommand());
        add(new EClipCommand());
        add(new HClipCommand());
        add(new LoginCommand());
        add(new MacroCommand());
        add(new StaffCommand());
        add(new VClipCommand());
        add(new AddonsCommand());
        add(new GetNbtCommand());
        add(new FriendCommand());
        add(new ModuleCommand());
        add(new PrefixCommand());
        add(new TrackerCommand());
        add(new GamemodeCommand());
        add(new DropAllCommand());
        add(new TreasureCommand());
        add(new WayPointCommand());
        add(new TabParseCommand());
        add(new BlockESPCommand());
        add(new BenchMarkCommand());
        add(new HorseSpeedCommand());
        add(new OpenFolderCommand());
        add(new ResetBindsCommand());
        add(new InvCleanerCommand());
        add(new GotoWaypointCommand());
        add(new ChestStealerCommand());
        add(new GarbageCleanerCommand());
    }

    private void add(@NotNull Command command) {
        command.register(dispatcher);
        commands.add(command);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Command get(Class<? extends Command> commandClass) {
        for (Command command : commands)
            if (command.getClass().equals(commandClass)) return command;

        return null;
    }

    public static @NotNull String getClientMessage() {
        return Formatting.WHITE + "⌊" + Formatting.GOLD + "⚡" + Formatting.WHITE + "⌉" + Formatting.RESET;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public CommandSource getSource() {
        return source;
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return dispatcher;
    }

    public void registerCommand(Command command) {
        if (command == null) return;

        command.register(dispatcher);
        this.commands.add(command);
    }
}

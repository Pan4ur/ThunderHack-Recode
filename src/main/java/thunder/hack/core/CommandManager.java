package thunder.hack.core;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.impl.*;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private String prefix;

    private final CommandDispatcher<CommandSource> dispatcher;
    private final CommandSource source;
    private final List<Command> commands;

    public CommandManager() {
        prefix = "@";
        commands = new ArrayList<>();
        dispatcher = new CommandDispatcher<>();
        source = new ClientCommandSource(null, MinecraftClient.getInstance());

        add(new BindCommand());
        add(new CfgCommand());
        add(new DrawCommand());
        add(new EClipCommand());
        add(new FriendCommand());
        add(new GpsCommand());
        add(new HClipCommand());
        add(new HelpCommand());
        add(new KitCommand());
        add(new LoginCommand());
        add(new MacroCommand());
        add(new ModuleCommand());
        add(new PrefixCommand());
        add(new RpcCommand());
        add(new SearchCommand());
        add(new StaffCommand());
        add(new TrackerCommand());
        add(new VClipCommand());
        add(new WayPointCommand());
        add(new OpenFolderCommand());
        add(new ResetBindsCommand());
        add(new ChestStealerCommand());

    }

    private void add(Command command) {
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

    public static String getClientMessage() {
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
}
package dev.thunderhack.core;

import com.mojang.brigadier.CommandDispatcher;
import dev.thunderhack.cmd.Command;
import dev.thunderhack.cmd.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private String prefix = "@";

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
    private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance());
    private final List<Command> commands = new ArrayList<>();

    public CommandManager() {
        add(new RpcCommand());
        add(new KitCommand());
        add(new GpsCommand());
        add(new CfgCommand());
        add(new BindCommand());
        add(new DrawCommand());
        add(new HelpCommand());
        add(new EClipCommand());
        add(new HClipCommand());
        add(new LoginCommand());
        add(new MacroCommand());
        add(new StaffCommand());
        add(new VClipCommand());
        add(new FriendCommand());
        add(new ModuleCommand());
        add(new PrefixCommand());
        add(new SearchCommand());
        add(new TrackerCommand());
        add(new DropAllCommand());
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
package thunder.hack.core;

import thunder.hack.cmd.Command;
import net.minecraft.util.Formatting;
import thunder.hack.cmd.impl.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class CommandManager {
    private final ArrayList<Command> commands = new ArrayList();
    private String prefix = ".";

    public CommandManager() {
        this.commands.add(new BindCommand());
        this.commands.add(new ModuleCommand());
        this.commands.add(new PrefixCommand());
        this.commands.add(new ConfigCommand());
        this.commands.add(new CfgCommand());
        this.commands.add(new FriendCommand());
        this.commands.add(new HelpCommand());
        this.commands.add(new RpcCommand());
        this.commands.add(new DrawCommand());
        this.commands.add(new StaffCommand());
        this.commands.add(new GpsCommand());
        this.commands.add(new SearchCommand());
        this.commands.add(new MacroCommand());
        this.commands.add(new LoginCommand());
        this.commands.add(new HclipCommand());
        this.commands.add(new VclipCommand());
        this.commands.add(new EclipCommand());
        this.commands.add(new TrackerCommand());

    }

    public static String[] removeElement(String[] input, int indexToDelete) {
        LinkedList<String> result = new LinkedList<String>();
        for (int i = 0; i < input.length; ++i) {
            if (i == indexToDelete) continue;
            result.add(input[i]);
        }
        return result.toArray(input);
    }

    private static String strip(String str, String key) {
        if (str.startsWith(key) && str.endsWith(key)) {
            return str.substring(key.length(), str.length() - key.length());
        }
        return str;
    }

    public void executeCommand(String command) {
        String[] parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String name = parts[0].substring(1);
        String[] args = CommandManager.removeElement(parts, 0);
        for (int i = 0; i < args.length; ++i) {
            if (args[i] == null) continue;
            args[i] = CommandManager.strip(args[i], "\"");
        }
        for (Command c : this.commands) {
            if (!c.getName().equalsIgnoreCase(name)) continue;
            c.execute(parts);
            return;
        }
        Command.sendMessage(Formatting.GRAY + "Command not found, type 'help' for the commands list.");
    }


    public ArrayList<Command> getCommands() {
        return this.commands;
    }

    public String getClientMessage() {
        return Formatting.WHITE  + "⌊" + Formatting.GOLD + "⚡"+ Formatting.WHITE +"⌉"+ Formatting.RESET ;
    }


    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}


package thunder.hack.cmd.impl;

import thunder.hack.cmd.Command;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class StaffCommand extends Command {
    public static List<String> staffNames = new ArrayList<>();

    public StaffCommand() {
        super("staff");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if (staffNames.isEmpty()) {
                sendMessage("Staff list empty");
            } else {
                StringBuilder f = new StringBuilder("Staff: ");
                for (String staff : staffNames) {
                    try {
                        f.append(staff).append(", ");
                    } catch (Exception ignored) {
                    }
                }
                sendMessage(f.toString());
            }
            return;
        }
        if (commands.length == 2) {
            if ("reset".equals(commands[0])) {
                staffNames.clear();
                sendMessage("staff list got reset.");
                return;
            }
            return;
        }
        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add": {
                    staffNames.add(commands[1]);
                    sendMessage(Formatting.GREEN + commands[1] + " added to staff list");
                    return;
                }
                case "del": {
                    staffNames.remove(commands[1]);
                    sendMessage(Formatting.GREEN + commands[1] + " removed from staff list");
                    return;
                }
            }
            sendMessage("Unknown Command, try staff add/del (name)");
        }
    }
}
package thunder.hack.cmd.impl;

import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if (Thunderhack.friendManager.getFriends().isEmpty()) {
                FriendCommand.sendMessage("Friend list empty D:.");
            } else {
                String f = "Friends: ";
                for (String friend : Thunderhack.friendManager.getFriends()) {
                    try {
                        f = f + friend + ", ";
                    } catch (Exception ignored) {
                    }
                }
                FriendCommand.sendMessage(f);
            }
            return;
        }
        if (commands.length == 2) {
            if (commands[0].equals("reset")) {
                Thunderhack.friendManager.clear();
                FriendCommand.sendMessage("Friends got reset.");
                return;
            }
            FriendCommand.sendMessage(commands[0] + (Thunderhack.friendManager.isFriend(commands[0]) ? " is friended." : " isn't friended."));
            return;
        }
        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add" -> {
                    Thunderhack.friendManager.addFriend(commands[1]);
                    FriendCommand.sendMessage(commands[1] + " has been friended");
                    return;
                }
                case "del" -> {
                    Thunderhack.friendManager.removeFriend(commands[1]);
                    FriendCommand.sendMessage(commands[1] + " has been unfriended");
                    return;
                }
            }
            FriendCommand.sendMessage("Unknown Command, try friend add/del (name)");
        }
    }
}


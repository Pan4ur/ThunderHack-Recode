package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.FriendArgumentType;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            Thunderhack.friendManager.clear();
            sendMessage("Friends got reset.");

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("player", StringArgumentType.word()).executes(context -> {
            String nickname = context.getArgument("player", String.class);

            Thunderhack.friendManager.addFriend(nickname);
            sendMessage(nickname + " has been friended");
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(arg("player", FriendArgumentType.create()).executes(context -> {
            String nickname = context.getArgument("player", String.class);

            Thunderhack.friendManager.removeFriend(nickname);
            sendMessage(nickname + " has been unfriended");
            return SINGLE_SUCCESS;
        })));

        builder.then(arg("player", StringArgumentType.word()).executes(context -> {
            String nickname = context.getArgument("player", String.class);
            sendMessage(nickname + (Thunderhack.friendManager.isFriend(nickname) ? " is friended." : " isn't friended."));

            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            if (Thunderhack.friendManager.getFriends().isEmpty()) {
                thunder.hack.cmd.impl.FriendCommand.sendMessage("Friend list empty D:.");
            } else {
                StringBuilder f = new StringBuilder("Friends: ");
                for (String friend : Thunderhack.friendManager.getFriends()) {
                    try {
                        f.append(friend).append(", ");
                    } catch (Exception ignored) {
                    }
                }
                thunder.hack.cmd.impl.FriendCommand.sendMessage(f.toString());
            }
            return SINGLE_SUCCESS;
        });
    }
}

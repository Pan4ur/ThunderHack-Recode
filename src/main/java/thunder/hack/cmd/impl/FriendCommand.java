package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.FriendArgumentType;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.system.Systems.MANAGER;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend", "friends");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            MANAGER.FRIEND.clear();
            sendMessage("Friends got reset.");

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("player", StringArgumentType.word()).executes(context -> {
            String nickname = context.getArgument("player", String.class);

            MANAGER.FRIEND.addFriend(nickname);
            sendMessage(nickname + " has been friended");
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(arg("player", FriendArgumentType.create()).executes(context -> {
            String nickname = context.getArgument("player", String.class);

            MANAGER.FRIEND.removeFriend(nickname);
            sendMessage(nickname + " has been unfriended");
            return SINGLE_SUCCESS;
        })));

        builder.then(arg("player", StringArgumentType.word()).executes(context -> {
            String nickname = context.getArgument("player", String.class);
            sendMessage(nickname + (MANAGER.FRIEND.isFriend(nickname) ? " is friended." : " isn't friended."));

            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            if (MANAGER.FRIEND.getFriends().isEmpty()) {
                sendMessage("Friend list empty D:");
            } else {
                StringBuilder f = new StringBuilder("Friends: ");
                for (String friend : MANAGER.FRIEND.getFriends()) {
                    try {
                        f.append(friend).append(", ");
                    } catch (Exception ignored) {
                    }
                }
                sendMessage(f.toString());
            }
            return SINGLE_SUCCESS;
        });
    }
}

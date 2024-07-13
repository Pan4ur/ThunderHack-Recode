package thunder.hack.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.FriendArgumentType;
import thunder.hack.cmd.args.PlayerArgumentType;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend", "friends");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            ThunderHack.friendManager.clear();
            sendMessage("Friends got reset.");

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("add").then(arg("player", PlayerArgumentType.create()).executes(context -> {
            PlayerListEntry player = context.getArgument("player", PlayerListEntry.class);

            ThunderHack.friendManager.addFriend(player.getProfile().getName());
            sendMessage(player.getProfile().getName() + " has been friended");
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(arg("player", FriendArgumentType.create()).executes(context -> {
            String nickname = context.getArgument("player", String.class);

            ThunderHack.friendManager.removeFriend(nickname);
            sendMessage(nickname + " has been unfriended");
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("is").then(arg("player", PlayerArgumentType.create()).executes(context -> {
            PlayerListEntry player = context.getArgument("player", PlayerListEntry.class);
            sendMessage(player.getProfile().getName() + (ThunderHack.friendManager.isFriend(player.getProfile().getName()) ? " is friended." : " isn't friended."));

            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            if (ThunderHack.friendManager.getFriends().isEmpty()) {
                sendMessage("Friend list empty D:");
            } else {
                StringBuilder f = new StringBuilder("Friends: ");
                for (String friend : ThunderHack.friendManager.getFriends()) {
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

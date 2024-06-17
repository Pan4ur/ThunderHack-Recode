package thunder.hack.cmd.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
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
            PlayerEntity player = context.getArgument("player", PlayerEntity.class);

            ThunderHack.friendManager.addFriend(player.getName().getString());
            sendMessage(player.getName().getString() + " has been friended");
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(arg("player", FriendArgumentType.create()).executes(context -> {
            String nickname = context.getArgument("player", String.class);

            ThunderHack.friendManager.removeFriend(nickname);
            sendMessage(nickname + " has been unfriended");
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("is").then(arg("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity player = context.getArgument("player", PlayerEntity.class);
            sendMessage(player.getName().getString() + (ThunderHack.friendManager.isFriend(player.getName().getString()) ? " is friended." : " isn't friended."));

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

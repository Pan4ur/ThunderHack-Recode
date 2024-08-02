package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.RPC;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class RpcCommand extends Command {
    public RpcCommand() {
        super("rpc");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("bigImg", StringArgumentType.greedyString()).executes(context -> {
            String bigImg = context.getArgument("bigImg", String.class);

            RPC.WriteFile(bigImg, "none");
            sendMessage("Большая картинка RPC изменена на " + bigImg);

            return SINGLE_SUCCESS;
        }).then(arg("littleImg", StringArgumentType.greedyString()).executes(context -> {
            String bigImg = context.getArgument("bigImg", String.class);
            String littleImg = context.getArgument("littleImg", String.class);

            RPC.WriteFile(bigImg, littleImg);
            sendMessage("Большая картинка RPC изменена на " + bigImg);
            sendMessage("Маленькая картинка RPC изменена на " + littleImg);

            return SINGLE_SUCCESS;
        })));

        builder.executes(context -> {
            sendMessage(".rpc url or .rpc url url");
            return SINGLE_SUCCESS;
        });
    }
}

package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.CfgArgumentType;

import java.io.File;
import java.util.Objects;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CfgCommand extends Command {
    public CfgCommand() {
        super("cfg", "config");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            StringBuilder configs = new StringBuilder("Configs: ");
            for (String str : Objects.requireNonNull(Thunderhack.configManager.getConfigList())) {
                configs.append("\n- ").append(str);
            }
            sendMessage(configs.toString());

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("dir").executes(context -> {
            try {
                net.minecraft.util.Util.getOperatingSystem().open(new File("ThunderHackRecode/configs/").toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("save").then(arg("name", StringArgumentType.word()).executes(context -> {
            Thunderhack.configManager.save(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        // я заменил CfgArgumentType.create() на StringArgumentType.word()
        builder.then(literal("load").then(arg("name", CfgArgumentType.create()).executes(context -> {
            Thunderhack.configManager.load(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));
    }
}

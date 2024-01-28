package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.CfgArgumentType;
import thunder.hack.cmd.args.ModuleArgumentType;
import thunder.hack.modules.Module;

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
            for (String str : Objects.requireNonNull(ThunderHack.configManager.getConfigList())) {
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
            ThunderHack.configManager.save(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("load").then(arg("name", CfgArgumentType.create()).executes(context -> {
            ThunderHack.configManager.load(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("set")
                .then(arg("name", CfgArgumentType.create())
                        .then(arg("module", ModuleArgumentType.create()).executes(context -> {
                            sendMessage("228");
                            ThunderHack.configManager.loadModuleOnly(context.getArgument("name", String.class), context.getArgument("module", Module.class));
                            return SINGLE_SUCCESS;
                        }))
                        .executes(context -> {
                            sendMessage("666");
                            ThunderHack.configManager.load(context.getArgument("name", String.class));
                            return SINGLE_SUCCESS;
                        })));
    }
}

package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.CfgArgumentType;
import thunder.hack.cmd.args.CfgModeType;
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

        builder.then(literal("load")
                .then(arg("mode", CfgModeType.create()))
                .then(arg("name", CfgModeType.create()).executes(context -> {
                    String name = context.getArgument("name", String.class);
                    String cat = context.getArgument("mode", String.class);
                    ThunderHack.configManager.load(name, cat);
                    return SINGLE_SUCCESS;
                }))).executes(context -> {
            ThunderHack.configManager.load(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        });

        builder.then(literal("loadcloud").then(arg("name", StringArgumentType.word()).executes(context -> {
            ThunderHack.configManager.loadCloud(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("cloudlist").executes(context -> {
            StringBuilder configs = new StringBuilder("Cloud Configs: \n");
            for (String str : Objects.requireNonNull(ThunderHack.configManager.getCloudConfigs())) {
                String[] split = str.split(";");
                configs.append("\n- " + Formatting.BOLD + split[0] + Formatting.RESET + Formatting.GRAY + " author: " + Formatting.RESET + split[1] + Formatting.GRAY + " last updated: " + Formatting.RESET + split[2]);
            }
            sendMessage(configs.toString());

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("cloud").then(arg("name", StringArgumentType.word()).executes(context -> {
            ThunderHack.configManager.loadCloud(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("set")
                .then(arg("name", CfgModeType.create())
                        .then(arg("module", ModuleArgumentType.create()).executes(context -> {
                            ThunderHack.configManager.loadModuleOnly(context.getArgument("name", String.class), context.getArgument("module", Module.class));
                            return SINGLE_SUCCESS;
                        }))
                        .executes(context -> {
                            ThunderHack.configManager.load(context.getArgument("name", String.class));
                            return SINGLE_SUCCESS;
                        })));

        builder.then(literal("load")
                .then(arg("mode", CfgModeType.create())
                        .then(arg("name", CfgArgumentType.create()).executes(context -> {
                            ThunderHack.configManager.load(context.getArgument("name", String.class), context.getArgument("mode", String.class));
                            return SINGLE_SUCCESS;
                        }))
                        .executes(context -> {
                            ThunderHack.configManager.load(context.getArgument("name", String.class));
                            return SINGLE_SUCCESS;
                        })));
    }
}

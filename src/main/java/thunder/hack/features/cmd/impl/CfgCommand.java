package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.CategoryArgumentType;
import thunder.hack.features.cmd.args.CfgArgumentType;
import thunder.hack.features.cmd.args.ModuleArgumentType;
import thunder.hack.features.modules.Module;

import java.io.File;
import java.util.Objects;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CfgCommand extends Command {
    public CfgCommand() {
        super("cfg", "config");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            StringBuilder configs = new StringBuilder("Configs: ");
            for (String str : Objects.requireNonNull(Managers.CONFIG.getConfigList())) {
                configs.append("\n- " + (str.equals(Managers.CONFIG.getCurrentConfig().getName().replace(".th", "")) ? Formatting.GREEN : "")).append(str).append(Formatting.RESET);
            }
            sendMessage(configs.toString());

            return SINGLE_SUCCESS;
        });

        builder.then(literal("list").executes(context -> {
            StringBuilder configs = new StringBuilder("Configs: ");
            for (String str : Objects.requireNonNull(Managers.CONFIG.getConfigList())) {
                configs.append("\n- " + (str.equals(Managers.CONFIG.getCurrentConfig().getName().replace(".th", "")) ? Formatting.GREEN : "")).append(str).append(Formatting.RESET);
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
            Managers.CONFIG.save(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("loadcloud").then(arg("name", StringArgumentType.word()).executes(context -> {
            Managers.CONFIG.loadCloud(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("cloudlist").executes(context -> {
            StringBuilder configs = new StringBuilder("Cloud Configs: \n");
            for (String str : Objects.requireNonNull(Managers.CONFIG.getCloudConfigs())) {
                String[] split = str.split(";");
                configs.append("\n- " + Formatting.BOLD + split[0] + Formatting.RESET + Formatting.GRAY + " author: " + Formatting.RESET + split[1] + Formatting.GRAY + " last updated: " + Formatting.RESET + split[2]);
            }
            sendMessage(configs.toString());

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("cloud").then(arg("name", StringArgumentType.word()).executes(context -> {
            Managers.CONFIG.loadCloud(context.getArgument("name", String.class));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("set")
                .then(arg("name", CfgArgumentType.create())
                        .then(arg("module", ModuleArgumentType.create()).executes(context -> {
                            Managers.CONFIG.loadModuleOnly(context.getArgument("name", String.class), context.getArgument("module", Module.class));
                            return SINGLE_SUCCESS;
                        }))
                        .executes(context -> {
                            Managers.CONFIG.load(context.getArgument("name", String.class));
                            return SINGLE_SUCCESS;
                        })));

        builder.then(literal("load")
                .then(arg("name", CfgArgumentType.create())
                        .then(arg("module", ModuleArgumentType.create()).executes(context -> {
                            Managers.CONFIG.loadModuleOnly(context.getArgument("name", String.class), context.getArgument("module", Module.class));
                            return SINGLE_SUCCESS;
                        }))
                        .executes(context -> {
                            Managers.CONFIG.load(context.getArgument("name", String.class));
                            return SINGLE_SUCCESS;
                        })));


        builder.then(literal("loadCategory")
                .then(arg("name", CfgArgumentType.create()).then(arg("category", CategoryArgumentType.create()).executes(context -> {
                    Managers.CONFIG.load(context.getArgument("name", String.class), context.getArgument("category", String.class));
                    return SINGLE_SUCCESS;
                }))));

        builder.then(literal("loadBinds")
                .then(arg("name", CfgArgumentType.create()).executes(context -> {
                    Managers.CONFIG.loadBinds(context.getArgument("name", String.class));
                    return SINGLE_SUCCESS;
                })));
    }
}

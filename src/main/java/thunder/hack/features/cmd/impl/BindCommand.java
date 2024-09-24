package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.ModuleArgumentType;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.impl.Bind;

import java.util.Objects;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.hud.impl.KeyBinds.getShortKeyName;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("module", ModuleArgumentType.create())
                .then(arg("key", StringArgumentType.word()).executes(context -> {
                    final Module module = context.getArgument("module", Module.class);
                    final String stringKey = context.getArgument("key", String.class);

                    if (stringKey == null) {
                        sendMessage(module.getName() + " is bound to " + Formatting.GRAY + module.getBind().getBind());
                        return SINGLE_SUCCESS;
                    }

                    int key;
                    if (stringKey.equalsIgnoreCase("none") || stringKey.equalsIgnoreCase("null")) {
                        key = -1;
                    } else {
                        try {
                            key = InputUtil.fromTranslationKey("key.keyboard." + stringKey.toLowerCase()).getCode();
                        } catch (NumberFormatException e) {
                            sendMessage(isRu() ? "Такой кнопки не существует!" : "There is no such button");
                            return SINGLE_SUCCESS;
                        }
                    }

                    if (key == 0) {
                        sendMessage("Unknown key '" + stringKey + "'!");
                        return SINGLE_SUCCESS;
                    }
                    module.setBind(key, !stringKey.equals("M") && stringKey.contains("M"), false);

                    sendMessage("Bind for " + Formatting.GREEN + module.getName() + Formatting.WHITE + " set to " + Formatting.GRAY + stringKey.toUpperCase());

                    return SINGLE_SUCCESS;
                }))
        );

        builder.then(literal("list").executes(context -> {
            StringBuilder binds = new StringBuilder("Binds: ");
            for (Module feature : Managers.MODULE.modules) {
                if (!Objects.equals(feature.getBind().getBind(), "None")) {
                    binds.append("\n- ").append(feature.getName()).append(" -> ").append(getShortKeyName(feature)).append(feature.getBind().isHold() ? "[hold]" : "");
                }
            }
            sendMessage(binds.toString());
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("clear").executes(context -> {
            for (Module mod : Managers.MODULE.modules) mod.setBind(new Bind(-1, false, false));
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("reset").executes(context -> {
            for (Module mod : Managers.MODULE.modules) mod.setBind(new Bind(-1, false, false));
            sendMessage("Done!");
            return SINGLE_SUCCESS;
        }));
    }
}

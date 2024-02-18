package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.MacroArgumentType;
import thunder.hack.core.impl.MacroManager;

import java.lang.reflect.Field;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.modules.client.MainSettings.isRu;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("macro", "macros");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            sendMessage(isRu() ? "Макросы:" : "Macro list:");
            sendMessage(" ");
            ThunderHack.macroManager.getMacros().forEach(macro -> sendMessage(macro.name() + (macro.bind() != -1 ? " [" + toString(macro.bind()) + "]" : "") + " {" + macro.text() + "}"));
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(arg("macro", MacroArgumentType.create()).executes(context -> {
            MacroManager.Macro macro = context.getArgument("macro", MacroManager.Macro.class);
            if (macro == null) {
                sendMessage(isRu() ? "Не существует такого макроса!" : "Wrong macro name!");
                return SINGLE_SUCCESS;
            }

            ThunderHack.macroManager.removeMacro(macro);
            sendMessage((isRu() ? "Удален макрос " : "Removed macro ") + macro.name());

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("add")
                .then(arg("name", StringArgumentType.word())
                        .then(arg("bind", StringArgumentType.word())
                                .then(arg("args", StringArgumentType.greedyString()).executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    String bind = context.getArgument("bind", String.class).toUpperCase();
                                    String args = context.getArgument("args", String.class);

                                    if (InputUtil.fromTranslationKey("key.keyboard." + bind.toLowerCase()).getCode() == -1) {
                                        sendMessage(isRu() ? "Неправильный бинд!" : "Wrong bind!");
                                        return SINGLE_SUCCESS;
                                    }

                                    MacroManager.Macro macro = new MacroManager.Macro(name, args, InputUtil.fromTranslationKey("key.keyboard." + bind.toLowerCase()).getCode());
                                    MacroManager.addMacro(macro);
                                    sendMessage(isRu() ? "Добавлен макрос " + name + " на кнопку " + toString(macro.bind()) : "Added macro " + name + " to " + toString(macro.bind()));

                                    return SINGLE_SUCCESS;
                                })))));

        builder.executes(context -> {
            sendMessage(usage());

            return SINGLE_SUCCESS;
        });
    }

    public String toString(int key) {
        String kn = key > 0 ? GLFW.glfwGetKeyName(key, GLFW.glfwGetKeyScancode(key)) : "None";

        if (kn == null) {
            try {
                for (Field declaredField : GLFW.class.getDeclaredFields()) {
                    if (declaredField.getName().startsWith("GLFW_KEY_")) {
                        int a = (int) declaredField.get(null);
                        if (a == key) {
                            String nb = declaredField.getName().substring("GLFW_KEY_".length());
                            kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                        }
                    }
                }
            } catch (Exception ignored) {
                kn = "unknown." + key;
            }
        }

        return key == -1 ? "None" : (kn + "").toUpperCase();
    }

    String usage() {
        return "macro add/remove/list (macro add name key text), (macro remove name)";
    }
}

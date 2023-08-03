package thunder.hack.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import org.lwjgl.glfw.GLFW;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.MacroArgumentType;
import thunder.hack.core.MacroManager;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.utility.Macro;

import java.lang.reflect.Field;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("macro", "macros");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("list").executes(context -> {
            if (MainSettings.language.getValue() == MainSettings.Language.RU) {
                sendMessage("Макросы:");
            } else {
                sendMessage("Macro list:");
            }


            sendMessage(" ");
            Thunderhack.macroManager.getMacros().forEach(macro -> sendMessage(macro.getName() + (macro.getBind() != -1 ? " [" + toString(macro.getBind()) + "]" : "") + " {" + macro.getText() + "}"));

            return SINGLE_SUCCESS;
        }));

        builder.then(literal("remove").then(arg("macro", MacroArgumentType.create()).executes(context -> {
            Macro macro = context.getArgument("macro", Macro.class);
            if (macro == null) {
                sendMessage("Не существует такого макроса!");
                return SINGLE_SUCCESS;
            }

            Thunderhack.macroManager.removeMacro(macro);
            sendMessage("Удален макрос " + macro.getName());

            return SINGLE_SUCCESS;
        })));

        builder.then(literal("add")
                .then(arg("name", StringArgumentType.word())
                        .then(arg("bind", StringArgumentType.word())
                                .then(arg("args", StringArgumentType.string()).executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    String bind = context.getArgument("bind", String.class).toUpperCase();
                                    String args = context.getArgument("args", String.class);

                                    if (InputUtil.fromTranslationKey("key.keyboard." + bind.toLowerCase()).getCode() == -1) {
                                        sendMessage("Неправильный бинд!");
                                        return SINGLE_SUCCESS;
                                    }

                                    Macro macro = new Macro(name, args, InputUtil.fromTranslationKey("key.keyboard." + bind.toLowerCase()).getCode());
                                    MacroManager.addMacro(macro);
                                    sendMessage("Добавлен макрос " + name + " на кнопку " + toString(macro.getBind()));

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

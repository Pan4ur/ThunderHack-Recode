package thunder.hack.cmd.impl;

import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.core.MacroManager;
import thunder.hack.utility.Macro;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Arrays;

public class MacroCommand extends Command {


        public MacroCommand() {
            super("macro");
        }

        @Override
        public void execute(String[] args) {
            if (args[0] == null) {
                Command.sendMessage(usage());
            }
            if (args[0].equals("list")) {

                sendMessage("Макросы:");


                sendMessage(" ");
                Thunderhack.macroManager.getMacros().forEach(macro -> sendMessage(macro.getName() + (macro.getBind() != -1 ? " [" + toString(macro.getBind()) + "]" : "") + " {" + macro.getText() + "}"));
            }
            if (args[0].equals("remove")) {
                if (Thunderhack.macroManager.getMacroByName(args[1]) != null) {
                    Macro macro = Thunderhack.macroManager.getMacroByName(args[1]);
                    Thunderhack.macroManager.removeMacro(macro);
                    sendMessage("Удален макрос " + macro.getName());
                } else {
                    sendMessage("Не существует макроса с именем " + args[1]);
                }
            }
            if (args.length >= 4) {
                if (args[0].equals("add")) {
                    String name = args[1];
                    String bind = args[2].toUpperCase();
                    String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length - 1));
                    if (InputUtil.fromTranslationKey("key.keyboard." + bind.toLowerCase()).getCode() == -1) {
                        sendMessage("Неправильный бинд!");
                        return;
                    }
                    Macro macro = new Macro(name, text, InputUtil.fromTranslationKey("key.keyboard." + bind.toLowerCase()).getCode());
                    MacroManager.addMacro(macro);
                    sendMessage("Добавлен макрос " + name + " на кнопку " + toString(macro.getBind() + 0));
                } else {
                    sendMessage(usage());
                }
            }
        }

    public String toString(int key) {
        String kn = key > 0 ? GLFW.glfwGetKeyName((int) (key + 0), GLFW.glfwGetKeyScancode((int) (key + 0))) : "None";
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
                kn = "unknown." + (int) (key + 0);
            }
        }

        return key == -1 ? "None" : (kn + "").toUpperCase();
    }

        String usage() {
            return "macro add/remove/list (macro add name key text), (macro remove name)";
        }
}

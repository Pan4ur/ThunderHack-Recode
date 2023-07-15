package thunder.hack.cmd.impl;

import thunder.hack.cmd.Command;
import thunder.hack.Thunderhack;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.impl.Bind;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;

public class BindCommand
        extends Command {
    public BindCommand() {
        super("bind");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if(MainSettings.language.getValue() == MainSettings.Language.RU){
                BindCommand.sendMessage("Укажи название модуля!");
            } else {
                BindCommand.sendMessage("Please specify a module!");
            }
            return;
        }
        String rkey = commands[1];
        String moduleName = commands[0];
        Module module = Thunderhack.moduleManager.get(moduleName);
        if (module == null) {
            if(MainSettings.language.getValue() == MainSettings.Language.RU){
                BindCommand.sendMessage("Неизвестный модуль '" + module + "'!");
            } else {
                BindCommand.sendMessage("Unknown module '" + module + "'!");
            }
            return;
        }
        if (rkey == null) {
            BindCommand.sendMessage(module.getName() + " is bound to " + Formatting.GRAY + module.getBind().toString());
            return;
        }
        int key = InputUtil.fromTranslationKey("key.keyboard." + rkey.toLowerCase()).getCode();
        if (rkey.equalsIgnoreCase("none")) {
            key = -1;
        }
        if (key == 0) {
            BindCommand.sendMessage("Unknown key '" + rkey + "'!");
            return;
        }
        module.bind.setValue(new Bind(key));
        BindCommand.sendMessage("Bind for " + Formatting.GREEN + module.getName() + Formatting.WHITE + " set to " + Formatting.GRAY + rkey.toUpperCase());
    }
}


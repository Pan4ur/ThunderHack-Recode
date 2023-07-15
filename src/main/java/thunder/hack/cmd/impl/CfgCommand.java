package thunder.hack.cmd.impl;

import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.core.ConfigManager;
import thunder.hack.modules.client.MainSettings;

import java.awt.*;
import java.io.File;
import java.util.Objects;

public class CfgCommand extends Command {
    public CfgCommand() {
        super("cfg");
    }

    public void execute(String[] commands) {
        if (commands.length == 1) {

            if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                sendMessage("Конфиги сохраняются в  ThunderHackRecode/configs/");
            } else {
                sendMessage("Configurations are saved in ThunderHackRecode/configs/");
            }
            return;
        }
        if (commands.length == 2)
            if ("list".equals(commands[0])) {
                StringBuilder configs = new StringBuilder("Configs: ");
                for(String str : Objects.requireNonNull(Thunderhack.configManager.getConfigList())){
                    configs.append("\n- ").append(str);
                }
                sendMessage(configs.toString());
            } else if( "dir".equals(commands[0]) ){
                try {
                    net.minecraft.util.Util.getOperatingSystem().open(new File("ThunderHackRecode/configs/").toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                    sendMessage("Нет такой команды!... Может list ?");
                } else {
                    sendMessage("Wrong command!... Maybe list?");
                }
            }
        if (commands.length >= 3) {
            switch (commands[0]) {
                case "save":
                case "create":
                    Thunderhack.configManager.save(commands[1]);
                    return;
                case "set":
                case "load":
                    Thunderhack.configManager.load(commands[1]);
                    return;
            }
            if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                sendMessage("Нет такой команды! Пример использования: <save/load>");
            } else {
                sendMessage("Wrong command! try: <save/load>");
            }
        }
    }
}

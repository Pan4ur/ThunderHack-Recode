package thunder.hack.cmd.impl;


import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.core.ConfigManager;

import java.awt.*;
import java.io.File;
import java.util.Objects;


public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config");
    }

    public void execute(String[] commands) {
        if (commands.length == 1) {
            sendMessage("Конфиги сохраняются в  ThunderHackRecode/configs/");
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
                sendMessage("Нет такой команды!... Может list ?");
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
            sendMessage("Нет такой команды! Пример использования: <save/load>");
        }
    }
}

package thunder.hack.cmd.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.modules.Module;
import net.minecraft.util.Formatting;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.*;

import java.util.Objects;

public class ModuleCommand
        extends Command {
    public ModuleCommand() {
        super("module");
    }

    @Override
    public void execute(String[] commands) {
        Setting setting;
        if (commands.length == 1) {
            ModuleCommand.sendMessage("Modules: ");
            for (Module.Category category : Thunderhack.moduleManager.getCategories()) {
                String modules = category.getName() + ": ";
                for (Module module1 : Thunderhack.moduleManager.getModulesByCategory(category)) {
                    modules = modules + (module1.isEnabled() ? Formatting.GREEN : Formatting.RED) + module1.getName() + Formatting.WHITE + ", ";
                }
                ModuleCommand.sendMessage(modules);
            }
            return;
        }
        Module module = Thunderhack.moduleManager.getModuleByDisplayName(commands[0]);
        if (module == null) {
            module = Thunderhack.moduleManager.get(commands[0]);
            if (module == null) {
                ModuleCommand.sendMessage("This module doesnt exist.");
                return;
            }
            ModuleCommand.sendMessage(" This is the original name of the module. Its current name is: " + module.getDisplayName());
            return;
        }
        if (commands.length == 2) {
            ModuleCommand.sendMessage(module.getDisplayName() + " : " + module.getDescription());
            for (Setting setting2 : module.getSettings()) {
                ModuleCommand.sendMessage(setting2.getName() + " : " + setting2.getValue());
            }
            return;
        }
        if (commands.length == 3) {
            if (commands[1].equalsIgnoreCase("set")) {
                ModuleCommand.sendMessage("Please specify a setting.");
            } else if (commands[1].equalsIgnoreCase("reset")) {
                for (Setting setting3 : module.getSettings()) {
                    setting3.setValue(setting3.getDefaultValue());
                }
            } else {
                ModuleCommand.sendMessage("This command doesnt exist.");
            }
            return;
        }
        if (commands.length == 4) {
            ModuleCommand.sendMessage("Please specify a value.");
            return;
        }
        if (commands.length == 5 && (setting = module.getSettingByName(commands[2])) != null) {
            JsonParser jp = new JsonParser();
            if (setting.getType().equalsIgnoreCase("String")) {
                setting.setValue(commands[3]);
                ModuleCommand.sendMessage(Formatting.DARK_GRAY + module.getName() + " " + setting.getName() + " has been set to " + commands[3] + ".");
                return;
            }
            try {
                if (setting.getName().equalsIgnoreCase("Enabled")) {
                    if (commands[3].equalsIgnoreCase("true")) {
                        module.enable();
                    }
                    if (commands[3].equalsIgnoreCase("false")) {
                        module.disable();
                    }
                }
              setCommandValue(module, setting, jp.parse(commands[3]));
            } catch (Exception e) {
                ModuleCommand.sendMessage("Bad Value! This setting requires a: " + setting.getType() + " value.");
                return;
            }
            ModuleCommand.sendMessage(Formatting.GRAY + module.getName() + " " + setting.getName() + " has been set to " + commands[3] + ".");
        }
    }


    public static void setCommandValue(Module feature, Setting setting, JsonElement element) {
        String str;
        for(Setting setting2 : feature.getSettings()) {
            if(Objects.equals(setting.getName(), setting2.getName())) {
                switch (setting2.getType()) {
                    case "Parent":
                        return;
                    case "Boolean":
                        setting2.setValue(Boolean.valueOf(element.getAsBoolean()));
                        return;
                    case "Double":
                        setting2.setValue(Double.valueOf(element.getAsDouble()));
                        return;
                    case "Float":
                        setting2.setValue(Float.valueOf(element.getAsFloat()));
                        return;
                    case "Integer":
                        setting2.setValue(Integer.valueOf(element.getAsInt()));
                        return;
                    case "String":
                        str = element.getAsString();
                        setting2.setValue(str.replace("_", " "));
                        return;
                    case "Bind":
                        JsonArray array4 = element.getAsJsonArray();
                        setting2.setValue((new Bind.BindConverter()).doBackward(array4.get(0)));
                        ((Bind) setting2.getValue()).setHold(array4.get(1).getAsBoolean());
                        return;
                    case "ColorSetting":
                        JsonArray array = element.getAsJsonArray();
                        ((ColorSetting) setting2.getValue()).setColor(array.get(0).getAsInt());
                        ((ColorSetting) setting2.getValue()).setCycle(array.get(1).getAsBoolean());
                        ((ColorSetting) setting2.getValue()).setGlobalOffset(array.get(2).getAsInt());
                        return;
                    case "PositionSetting":
                        JsonArray array3 = element.getAsJsonArray();
                        ((PositionSetting) setting2.getValue()).setX(array3.get(0).getAsFloat());
                        ((PositionSetting) setting2.getValue()).setY(array3.get(1).getAsFloat());
                        return;
                    case "SubBind":
                        setting2.setValue((new SubBind.SubBindConverter()).doBackward(element));
                        return;
                    case "Enum":
                        try {
                            EnumConverter converter = new EnumConverter(((Enum) setting2.getValue()).getClass());
                            Enum value = converter.doBackward(element);
                            setting2.setValue((value == null) ? setting2.getDefaultValue() : value);
                        } catch (Exception ignored) {}
                }
            }
        }
    }
}


package thunder.hack.cmd.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.args.ModuleArgumentType;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.EnumConverter;
import thunder.hack.setting.impl.PositionSetting;

import java.util.Objects;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ModuleCommand extends Command {
    public ModuleCommand() {
        super("module", "modules");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("module", ModuleArgumentType.create()).executes(context -> {
            Module module = context.getArgument("module", Module.class);
            sendMessage(module.getDisplayName() + " : " + module.getDescription());

            for (Setting setting2 : module.getSettings()) {
                sendMessage(setting2.getName() + " : " + setting2.getValue());
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(arg("module", ModuleArgumentType.create())
                .then(literal("reset").executes(context -> {
                    Module module = context.getArgument("module", Module.class);

                    for (Setting setting3 : module.getSettings()) {
                        setting3.setValue(setting3.getDefaultValue());
                    }

                    return SINGLE_SUCCESS;
                })).then(arg("setting", StringArgumentType.word())
                        .then(arg("settingValue", StringArgumentType.string()).executes(context -> {
                            Module module = context.getArgument("module", Module.class);
                            Setting setting = module.getSettingByName(context.getArgument("setting", String.class));
                            String settingValue = context.getArgument("settingValue", String.class);

                            if (setting == null) {
                                sendMessage("No such setting");
                                return SINGLE_SUCCESS;
                            }

                            JsonParser jp = new JsonParser();
                            if (setting.getType().equalsIgnoreCase("String")) {
                                setting.setValue(settingValue);
                                sendMessage(Formatting.DARK_GRAY + module.getName() + " " + setting.getName() + " has been set to " + settingValue + ".");
                                return SINGLE_SUCCESS;
                            }
                            try {
                                if (setting.getName().equalsIgnoreCase("Enabled")) {
                                    if (settingValue.equalsIgnoreCase("true")) {
                                        module.enable();
                                    }
                                    if (settingValue.equalsIgnoreCase("false")) {
                                        module.disable();
                                    }
                                }
                                setCommandValue(module, setting, jp.parse(settingValue));
                            } catch (Exception e) {
                                sendMessage("Bad Value! This setting requires a: " + setting.getType() + " value.");
                                return SINGLE_SUCCESS;
                            }
                            sendMessage(Formatting.GRAY + module.getName() + " " + setting.getName() + " has been set to " + settingValue + ".");
                            return SINGLE_SUCCESS;
                        }))));

        builder.executes(context -> {
            sendMessage("Modules: ");

            for (Module.Category category : Thunderhack.moduleManager.getCategories()) {
                StringBuilder modules = new StringBuilder(category.getName() + ": ");

                for (Module module1 : Thunderhack.moduleManager.getModulesByCategory(category)) {
                    modules.append(module1.isEnabled() ? Formatting.GREEN : Formatting.RED).append(module1.getName()).append(Formatting.WHITE).append(", ");
                }

                sendMessage(modules.toString());
            }

            return SINGLE_SUCCESS;
        });
    }

    public static void setCommandValue(Module feature, Setting setting, JsonElement element) {
        String str;
        for (Setting setting2 : feature.getSettings()) {
            if (Objects.equals(setting.getName(), setting2.getName())) {
                switch (setting2.getType()) {
                    case "Parent", "Bind":
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
                    case "Enum":
                        try {
                            EnumConverter converter = new EnumConverter(((Enum) setting2.getValue()).getClass());
                            Enum value = converter.doBackward(element);
                            setting2.setValue((value == null) ? setting2.getDefaultValue() : value);
                        } catch (Exception ignored) {
                        }
                }
            }
        }
    }
}

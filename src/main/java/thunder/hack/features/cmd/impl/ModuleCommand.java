package thunder.hack.features.cmd.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.ModuleArgumentType;
import thunder.hack.features.cmd.args.SettingArgumentType;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.EnumConverter;
import thunder.hack.setting.impl.PositionSetting;

import java.util.Objects;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class ModuleCommand extends Command {
    public ModuleCommand() {
        super("module", "modules");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("module", ModuleArgumentType.create()).executes(context -> {
            Module module = context.getArgument("module", Module.class);
            sendMessage(module.getDisplayName() + " : " + I18n.translate(module.getDescription()));

            for (Setting<?> setting2 : module.getSettings()) {
                sendMessage(setting2.getName() + " : " + setting2.getValue());
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(arg("module", ModuleArgumentType.create())
                .then(literal("reset").executes(context -> {
                    Module module = context.getArgument("module", Module.class);

                    for (Setting s : module.getSettings()) {
                        if (s.getValue() instanceof ColorSetting cs)
                            cs.setDefault();
                        else
                            s.setValue(s.getDefaultValue());
                    }

                    return SINGLE_SUCCESS;
                })).then(arg("setting", SettingArgumentType.create())
                        .then(arg("settingValue", StringArgumentType.greedyString()).executes(context -> {
                            Module module = context.getArgument("module", Module.class);
                            String settingName = context.getArgument("setting", String.class);
                            String settingValue = context.getArgument("settingValue", String.class);
                            Setting setting = null;

                            for(Setting set : module.getSettings()) {
                                if(Objects.equals(set.getName(), settingName))
                                    setting = set;
                            }

                            if (setting == null) {
                                sendMessage("No such setting");
                                return SINGLE_SUCCESS;
                            }

                            JsonParser jp = new JsonParser();
                            if (setting.getValue().getClass().getSimpleName().equalsIgnoreCase("String")) {
                                setting.setValue(settingValue);
                                sendMessage(Formatting.DARK_GRAY + module.getName() + " " + setting.getName() + (isRu() ? " был выставлен " : " has been set to ") + settingValue);
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
                                sendMessage((isRu() ? "Неверное значение! Эта настройка требует тип: " : "Bad Value! This setting requires a: ") + setting.getValue().getClass().getSimpleName());
                                return SINGLE_SUCCESS;
                            }

                            if(settingValue.contains("toggle"))
                                sendMessage(Formatting.GRAY + module.getName() + " " + setting.getName() + (isRu() ? " был переключен" : " has been toggled"));
                            else
                                sendMessage(Formatting.GRAY + module.getName() + " " + setting.getName() + (isRu() ? " был выставлен " : " has been set to ") + settingValue);

                            return SINGLE_SUCCESS;
                        }))));

        builder.executes(context -> {
            sendMessage("Modules: ");

            for (Module.Category category : Managers.MODULE.getCategories()) {
                StringBuilder modules = new StringBuilder(category.getName() + ": ");

                for (Module module1 : Managers.MODULE.getModulesByCategory(category)) {
                    modules.append(module1.isEnabled() ? Formatting.GREEN : Formatting.RED).append(module1.getName()).append(Formatting.WHITE).append(", ");
                }

                sendMessage(modules.toString());
            }

            return SINGLE_SUCCESS;
        });
    }

    public static void setCommandValue(@NotNull Module feature, Setting setting, JsonElement element) {
        String str;
        for (Setting checkSetting : feature.getSettings()) {
            if (Objects.equals(setting.getName(), checkSetting.getName())) {
                switch (checkSetting.getValue().getClass().getSimpleName()) {
                    case "SettingGroup", "Bind" -> {
                        return;
                    }
                    case "Boolean" -> {
                        if(element.getAsString().equals("toggle")) {
                            checkSetting.setValue(!(boolean) checkSetting.getValue());
                            return;
                        }
                        checkSetting.setValue(element.getAsBoolean());
                        return;
                    }
                    case "BooleanSettingGroup" -> {
                        ((BooleanSettingGroup) checkSetting.getValue()).setEnabled(element.getAsBoolean());
                    }
                    case "Double" -> {
                        checkSetting.setValue(element.getAsDouble());
                        return;
                    }
                    case "Float" -> {
                        checkSetting.setValue(element.getAsFloat());
                        return;
                    }
                    case "Integer" -> {
                        checkSetting.setValue(element.getAsInt());
                        return;
                    }
                    case "String" -> {
                        str = element.getAsString();
                        checkSetting.setValue(str.replace("_", " "));
                        return;
                    }
                    case "ColorSetting" -> {
                        JsonArray array = element.getAsJsonArray();
                        ((ColorSetting) checkSetting.getValue()).setColor(array.get(0).getAsInt());
                        ((ColorSetting) checkSetting.getValue()).setRainbow(array.get(1).getAsBoolean());
                        return;
                    }
                    case "PositionSetting" -> {
                        JsonArray array3 = element.getAsJsonArray();
                        ((PositionSetting) checkSetting.getValue()).setX(array3.get(0).getAsFloat());
                        ((PositionSetting) checkSetting.getValue()).setY(array3.get(1).getAsFloat());
                        return;
                    }
                    default -> {
                        try {
                            EnumConverter converter = new EnumConverter(((Enum) checkSetting.getValue()).getClass());
                            Enum value = converter.doBackward(element);
                            checkSetting.setValue((value == null) ? checkSetting.getDefaultValue() : value);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }
}

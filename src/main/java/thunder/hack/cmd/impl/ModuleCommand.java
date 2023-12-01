package thunder.hack.cmd.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
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

                    for (Setting setting3 : module.getSettings()) {
                        setting3.setValue(setting3.getDefaultValue());
                    }

                    return SINGLE_SUCCESS;
                })).then(arg("setting", StringArgumentType.word())
                        .then(arg("settingValue", StringArgumentType.greedyString()).executes(context -> {
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

            for (Module.Category category : ThunderHack.moduleManager.getCategories()) {
                StringBuilder modules = new StringBuilder(category.getName() + ": ");

                for (Module module1 : ThunderHack.moduleManager.getModulesByCategory(category)) {
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
                switch (checkSetting.getType()) {
                    case "Parent", "Bind" -> {
                        return;
                    }
                    case "Boolean" -> {
                        checkSetting.setValue(Boolean.valueOf(element.getAsBoolean()));
                        return;
                    }
                    case "Double" -> {
                        checkSetting.setValue(Double.valueOf(element.getAsDouble()));
                        return;
                    }
                    case "Float" -> {
                        checkSetting.setValue(Float.valueOf(element.getAsFloat()));
                        return;
                    }
                    case "Integer" -> {
                        checkSetting.setValue(Integer.valueOf(element.getAsInt()));
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
                        ((ColorSetting) checkSetting.getValue()).setGlobalOffset(array.get(2).getAsInt());
                        return;
                    }
                    case "PositionSetting" -> {
                        JsonArray array3 = element.getAsJsonArray();
                        ((PositionSetting) checkSetting.getValue()).setX(array3.get(0).getAsFloat());
                        ((PositionSetting) checkSetting.getValue()).setY(array3.get(1).getAsFloat());
                        return;
                    }
                    case "Enum" -> {
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

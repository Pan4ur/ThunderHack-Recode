package thunder.hack.core.manager.client;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.cmd.Command;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ConfigManager implements IManager {
    public static final String CONFIG_FOLDER_NAME = "ThunderHackRecode";
    public static final File MAIN_FOLDER = new File(mc.runDirectory, CONFIG_FOLDER_NAME);
    public static final File CONFIGS_FOLDER = new File(MAIN_FOLDER, "configs");
    public static final File TEMP_FOLDER = new File(MAIN_FOLDER, "temp");
    public static final File MISC_FOLDER = new File(MAIN_FOLDER, "misc");
    public static final File SOUNDS_FOLDER = new File(MISC_FOLDER, "sounds");
    public static final File IMAGES_FOLDER = new File(MISC_FOLDER, "images");
    public static final File TABPARSER_FOLDER = new File(MISC_FOLDER, "tabparser");
    public static final File STASHLOGGER_FOLDER = new File(MISC_FOLDER, "stashlogger");

    public File currentConfig = null;

    public static boolean firstLaunch = false;

    public ConfigManager() {
        firstLaunch = !MAIN_FOLDER.exists();
        createDirs(MAIN_FOLDER, CONFIGS_FOLDER, TEMP_FOLDER, MISC_FOLDER, SOUNDS_FOLDER, IMAGES_FOLDER, TABPARSER_FOLDER, STASHLOGGER_FOLDER);
    }

    private void createDirs(File... dirs) {
        for (File dir : dirs) if (!dir.exists()) dir.mkdirs();
    }

    public static @NotNull String getConfigDate(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists())
            return "none";

        return new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date(file.lastModified()));
    }

    public void load(String name, String category) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            Command.sendMessage(isRu() ? "Конфига " + name + " не существует!" : "Config " + name + " does not exist!");
            return;
        }

        if (currentConfig != null)
            save(currentConfig);

        Managers.MODULE.onUnload(category);
        load(file, category);
        Managers.MODULE.onLoad(category);
    }

    public void loadBinds(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            Command.sendMessage(isRu() ? "Конфига " + name + " не существует!" : "Config " + name + " does not exist!");
            return;
        }

        if (currentConfig != null)
            save(currentConfig);

        loadBinds(file);
    }

    private void loadBinds(@NotNull File config) {
        if (!config.exists())
            save(config);

        try (FileReader reader = new FileReader(config, StandardCharsets.UTF_8)) {
            JsonObject modulesObject = JsonParser.parseReader(reader).getAsJsonArray().get(0).getAsJsonObject();
            JsonArray modules = modulesObject.getAsJsonArray("Modules");

            if (modules != null)
                for (JsonElement element : modules)
                    parseBinds(element.getAsJsonObject());

            Command.sendMessage(isRu() ? "Загружены бинды с конфига: " + config.getName() : "Loaded bind from config: " + config.getName());
        } catch (IOException e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
        saveCurrentConfig();
    }

    public void load(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            Command.sendMessage(isRu() ? "Конфига " + name + " не существует!" : "Config " + name + " does not exist!");

            return;
        }

        if (currentConfig != null)
            save(currentConfig);

        Managers.MODULE.onUnload("none");
        load(file);
        Managers.MODULE.onLoad("none");
    }

    public void loadCloud(String name) {
        Command.sendMessage(isRu() ? "Загружаю.." : "Downloading..");
        try (BufferedInputStream in = new BufferedInputStream(new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/configs/" + name + ".th").openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(new File(CONFIGS_FOLDER, name + ".th"))) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1)
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            Command.sendMessage(isRu() ? "Загрузил!" : "Downloaded!");
            load(name);
        } catch (Exception e) {
            Command.sendMessage(isRu() ? "Произошла ошибка при загрузке! Может название неправильное?" : "There was an error downloading! Maybe the name is wrong?");
        }
    }

    public void loadModuleOnly(String name, Module module) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            Command.sendMessage(isRu() ? "Конфига " + name + " не существует!" : "Config " + name + " does not exist!");
            return;
        }

        if (module.isEnabled()) {
            ThunderHack.EVENT_BUS.unsubscribe(module);
            module.setEnabled(false);
        }

        loadModuleOnly(file, module);

        if (module.isEnabled())
            ThunderHack.EVENT_BUS.subscribe(module);
    }

    public void load(@NotNull File config) {
        load(config, "none");
    }

    private void load(@NotNull File config, String category) {
        if (!config.exists())
            save(config);

        try {
            FileReader reader = new FileReader(config, StandardCharsets.UTF_8);
            JsonObject modulesObject = JsonParser.parseReader(reader).getAsJsonArray().get(0).getAsJsonObject();
            JsonArray modules = modulesObject.getAsJsonArray("Modules");

            if (modules != null)
                for (JsonElement element : modules)
                    parseModule(element.getAsJsonObject(), category);

            Command.sendMessage(isRu() ? "Загружен конфиг " + config.getName() : "Loaded " + config.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Objects.equals(category, "none"))
            currentConfig = config;

        saveCurrentConfig();
    }

    public void loadModuleOnly(File config, Module module) {
        try (FileReader reader = new FileReader(config)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            JsonObject modulesObject = array.get(0).getAsJsonObject();
            JsonArray modules = modulesObject.getAsJsonArray("Modules");

            if (modules != null) {
                for (JsonElement element : modules) {
                    JsonObject moduleObject = element.getAsJsonObject();
                    Module loadedModule = Managers.MODULE.modules.stream().filter(m -> moduleObject.getAsJsonObject(m.getName()) != null).findFirst().orElse(null);
                    if (loadedModule != null && Objects.equals(module.getName(), loadedModule.getName()))
                        parseModule(moduleObject, "none");
                }
            }
            Command.sendMessage(isRu() ? "Загружен модуль " + module.getName() + " с конфига " + config.getName() :
                    "Loaded " + module.getName() + " from " + config.getName());

        } catch (IOException e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
    }

    public void save(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (file.exists()) {
            Command.sendMessage(isRu() ? "Перезаписываем " + name + "..." : "Overwriting " + name + "...");
            file.delete();
        } else {
            Command.sendMessage(isRu() ? "Конфиг " + name + " успешно сохранен!" : "Config " + name + " successfully saved!");
        }
        save(file);
    }

    public void save(@NotNull File config) {
        try {
            if (!config.exists())
                config.createNewFile();
            JsonArray array = new JsonArray();

            JsonObject modulesObj = new JsonObject();
            modulesObj.add("Modules", getModuleArray());
            array.add(modulesObj);

            FileWriter writer = new FileWriter(config, StandardCharsets.UTF_8);
            new GsonBuilder().setPrettyPrinting().create().toJson(array, writer);
            writer.close();
        } catch (IOException e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void parseModule(JsonObject object, String category) throws NullPointerException {
        Module module = Managers.MODULE.modules.stream()
                .filter(m -> object.getAsJsonObject(m.getName()) != null)
                .findFirst()
                .orElse(null);

        if (module == null)
            return;

        if (!Objects.equals(category, "none") && !module.getCategory().getName().equalsIgnoreCase(category))
            return;

        JsonObject mobject = object.getAsJsonObject(module.getName());

        for (Setting setting : module.getSettings()) {
            try {
                if (setting.getValue() instanceof SettingGroup) {
                } else if (setting.getValue() instanceof Boolean) {
                    setting.setValue(mobject.getAsJsonPrimitive(setting.getName()).getAsBoolean());
                } else if (setting.getValue() instanceof Float) {
                    setting.setValue(mobject.getAsJsonPrimitive(setting.getName()).getAsFloat());
                } else if (setting.getValue() instanceof Integer) {
                    setting.setValue(mobject.getAsJsonPrimitive(setting.getName()).getAsInt());
                } else if (setting.getValue() instanceof String) {
                    setting.setValue(mobject.getAsJsonPrimitive(setting.getName()).getAsString().replace("%%", " ").replace("++", "/"));
                } else if (setting.getValue() instanceof Bind) {
                    JsonArray array = mobject.getAsJsonArray(setting.getName());
                    if (array.get(0).getAsString().contains("M")) {
                        setting.setValue(new Bind(Integer.parseInt(array.get(0).getAsString().replace("M", "")), true, array.get(1).getAsBoolean()));
                    } else {
                        setting.setValue(new Bind(Integer.parseInt(array.get(0).getAsString()), false, array.get(1).getAsBoolean()));
                    }
                } else if (setting.getValue() instanceof ColorSetting colorSetting) {
                    JsonArray array = mobject.getAsJsonArray(setting.getName());
                    colorSetting.setColor(array.get(0).getAsInt());
                    colorSetting.setRainbow(array.get(1).getAsBoolean());
                } else if (setting.getValue() instanceof PositionSetting posSetting) {
                    JsonArray array = mobject.getAsJsonArray(setting.getName());
                    posSetting.setX(array.get(0).getAsFloat());
                    posSetting.setY(array.get(1).getAsFloat());
                } else if (setting.getValue() instanceof BooleanSettingGroup bGroup) {
                    bGroup.setEnabled(mobject.getAsJsonPrimitive(setting.getName()).getAsBoolean());
                } else if (setting.getValue() instanceof ItemSelectSetting iSetting) {
                    JsonArray array = mobject.getAsJsonArray(setting.getName());
                    for (int i = 0; i < array.size(); i++)
                        if (!iSetting.getItemsById().contains(array.get(i).getAsString()))
                            iSetting.getItemsById().add(array.get(i).getAsString());
                } else if (setting.getValue().getClass().isEnum()) {
                    Enum value = new EnumConverter(((Enum) setting.getValue()).getClass()).doBackward(mobject.getAsJsonPrimitive(setting.getName()));
                    setting.setValue((value == null) ? setting.getDefaultValue() : value);
                }
            } catch (Exception e) {
                LogUtils.getLogger().warn("[Thunderhack] Module: " + module.getName() + " Setting: " + setting.getName() + " Error: ");
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseBinds(JsonObject object) throws NullPointerException {
        Module module = Managers.MODULE.modules.stream()
                .filter(m -> object.getAsJsonObject(m.getName()) != null)
                .findFirst()
                .orElse(null);

        if (module == null)
            return;

        JsonObject mobject = object.getAsJsonObject(module.getName());

        for (Setting setting : module.getSettings()) {
            try {
                if (setting.getValue() instanceof Bind) {
                    JsonArray array = mobject.getAsJsonArray(setting.getName());
                    if (array.get(0).getAsString().contains("M")) {
                        setting.setValue(new Bind(Integer.parseInt(array.get(0).getAsString().replace("M", "")), true, array.get(1).getAsBoolean()));
                    } else {
                        setting.setValue(new Bind(Integer.parseInt(array.get(0).getAsString()), false, array.get(1).getAsBoolean()));
                    }
                }
            } catch (Exception e) {
                LogUtils.getLogger().warn("[Thunderhack] Module: " + module.getName() + " Setting: " + setting.getName() + " Error: ");
                e.printStackTrace();
            }
        }
    }

    private @NotNull JsonArray getModuleArray() {
        JsonArray modulesArray = new JsonArray();
        for (Module m : Managers.MODULE.modules) {
            modulesArray.add(getModuleObject(m));
        }
        return modulesArray;
    }

    public JsonObject getModuleObject(@NotNull Module m) {
        JsonObject attribs = new JsonObject();
        JsonParser jp = new JsonParser();

        for (Setting setting : m.getSettings()) {
            if (setting.getValue() instanceof ColorSetting color) {
                JsonArray array = new JsonArray();
                array.add(new JsonPrimitive(color.getRawColor()));
                array.add(new JsonPrimitive(color.isRainbow()));
                attribs.add(setting.getName(), array);
            } else if (setting.getValue() instanceof PositionSetting pos) {
                JsonArray array = new JsonArray();
                array.add(new JsonPrimitive(pos.getX()));
                array.add(new JsonPrimitive(pos.getY()));
                attribs.add(setting.getName(), array);
            } else if (setting.getValue() instanceof BooleanSettingGroup bGroup) {
                attribs.add(setting.getName(), jp.parse(String.valueOf(bGroup.isEnabled())));
            } else if (setting.getValue() instanceof Bind b) {
                JsonArray array = new JsonArray();
                if (b.isMouse())
                    array.add(jp.parse(b.getBind()));
                else
                    array.add(new JsonPrimitive(b.getKey()));
                array.add(new JsonPrimitive(b.isHold()));
                attribs.add(setting.getName(), array);
            } else if (setting.getValue() instanceof String str) {
                try {
                    attribs.add(setting.getName(), jp.parse(str.replace(" ", "%%").replace("/", "++")));
                } catch (Exception ignored) {
                }
            } else if (setting.getValue() instanceof ItemSelectSetting iSelect) {
                JsonArray array = new JsonArray();
                for (String id : iSelect.getItemsById())
                    array.add(new JsonPrimitive(id));
                attribs.add(setting.getName(), array);
            } else if (setting.isEnumSetting()) {
                attribs.add(setting.getName(), new EnumConverter(((Enum) setting.getValue()).getClass()).doForward((Enum) setting.getValue()));
            } else {
                try {
                    attribs.add(setting.getName(), jp.parse(setting.getValue().toString()));
                } catch (Exception ignored) {
                }
            }
        }

        JsonObject moduleObject = new JsonObject();
        moduleObject.add(m.getName(), attribs);
        return moduleObject;
    }

    public void delete(@NotNull File file) {
        file.delete();
    }

    public void delete(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            return;
        }
        delete(file);
    }

    public List<String> getConfigList() {
        if (!MAIN_FOLDER.exists() || MAIN_FOLDER.listFiles() == null) return null;

        List<String> list = new ArrayList<>();

        if (CONFIGS_FOLDER.listFiles() != null) {
            for (File file : Arrays.stream(Objects.requireNonNull(CONFIGS_FOLDER.listFiles())).filter(f -> f.getName().endsWith(".th")).toList()) {
                list.add(file.getName().replace(".th", ""));
            }
        }
        return list;
    }

    public List<String> getCloudConfigs() {
        List<String> list = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/cloudConfigs.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                list.add(inputLine.trim());
        } catch (Exception ignored) {
        }
        return list;
    }

    public void saveCurrentConfig() {
        File file = new File(CONFIG_FOLDER_NAME + "/misc/currentcfg.txt");
        try {
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                writer.write(currentConfig.getName().replace(".th", ""));
                writer.close();
            } else {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(currentConfig.getName().replace(".th", ""));
                writer.close();
            }
        } catch (Exception e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
    }

    public File getCurrentConfig() {
        File file = new File(CONFIG_FOLDER_NAME + "/misc/currentcfg.txt");
        String name = "config";
        try {
            if (file.exists()) {
                Scanner reader = new Scanner(file);
                while (reader.hasNextLine())
                    name = reader.nextLine();
                reader.close();
            }
        } catch (Exception e) {
            LogUtils.getLogger().warn(e.getMessage());
        }
        currentConfig = new File(CONFIGS_FOLDER, name + ".th");
        return currentConfig;
    }
}
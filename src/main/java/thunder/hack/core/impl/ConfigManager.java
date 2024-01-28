package thunder.hack.core.impl;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.util.Pair;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.cmd.impl.SearchCommand;
import thunder.hack.core.IManager;
import thunder.hack.gui.autobuy.AutoBuyItem;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.AutoBuy;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.render.Search;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.*;
import thunder.hack.utility.player.InventoryUtility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static thunder.hack.modules.client.MainSettings.isRu;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ConfigManager implements IManager {
    public static final String CONFIG_FOLDER_NAME = "ThunderHackRecode";
    public static final File MAIN_FOLDER = new File(mc.runDirectory, CONFIG_FOLDER_NAME);
    public static final File CONFIGS_FOLDER = new File(MAIN_FOLDER, "configs");
    public static final File AUTOBUY_FOLDER = new File(MAIN_FOLDER, "autobuy");
    public static final File TEMP_FOLDER = new File(MAIN_FOLDER, "temp");
    public static final File MISC_FOLDER = new File(MAIN_FOLDER, "misc");
    public static final File SOUNDS_FOLDER = new File(MISC_FOLDER, "sounds");

    public File currentConfig = null;

    public static boolean firstLaunch = false;

    public ConfigManager() {
        if (!MAIN_FOLDER.exists()) {
            MAIN_FOLDER.mkdirs();
            firstLaunch = true;
        }
        if (!CONFIGS_FOLDER.exists()) CONFIGS_FOLDER.mkdirs();
        if (!TEMP_FOLDER.exists()) TEMP_FOLDER.mkdirs();
        if (!MISC_FOLDER.exists()) MISC_FOLDER.mkdirs();
        if (!SOUNDS_FOLDER.exists()) SOUNDS_FOLDER.mkdirs();
        loadSearch();
    }

    public void loadSearch() {
        try {
            File file = new File(CONFIG_FOLDER_NAME + "/misc/search.txt");
            if (file.exists())
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready())
                        Search.defaultBlocks.add(SearchCommand.getRegisteredBlock(reader.readLine()));
                }
        } catch (Exception ignored) {
        }
    }

    public void loadDefault(String name) {
        MainSettings.Language prevLang = MainSettings.language.getValue();
        Path path = Paths.get(CONFIG_FOLDER_NAME + "/configs/" + name + ".th");
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("cfg/" + name + ".th");
             OutputStream out = Files.newOutputStream(path)) {
            if (in == null) return;
            IOUtils.copy(in, out);
            load(name);
            MainSettings.language.setValue(prevLang);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSearch() {
        File file = new File(CONFIG_FOLDER_NAME + "/misc/search.txt");
        try {
            new File(CONFIG_FOLDER_NAME).mkdirs();
            file.createNewFile();
        } catch (Exception ignored) {
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Block name : Search.defaultBlocks) {
                writer.write(name.getTranslationKey() + "\n");
            }
        } catch (Exception ignored) {
        }
    }

    public static @NotNull String getConfigDate(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            return "none";
        }
        long x = file.lastModified();
        DateFormat obj = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Date sol = new Date(x);
        return obj.format(sol);
    }

    public void load(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            if (isRu()) Command.sendMessage("Конфига " + name + " не существует!");
            else Command.sendMessage("Config " + name + " does not exist!");

            return;
        }

        if (currentConfig != null)
            save(currentConfig);

        ThunderHack.moduleManager.onUnload();
        ThunderHack.moduleManager.onUnloadPost();
        load(file);
        ThunderHack.moduleManager.onLoad();
    }

    public void loadModuleOnly(String name, Module module) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            if (isRu()) Command.sendMessage("Конфига " + name + " не существует!");
            else Command.sendMessage("Config " + name + " does not exist!");

            return;
        }

        ThunderHack.moduleManager.onUnload();
        ThunderHack.moduleManager.onUnloadPost();

        loadModuleOnly(file, module);
        ThunderHack.moduleManager.onLoad();
    }

    public void load(@NotNull File config) {
        if (!config.exists()) save(config);
        try {
            FileReader reader = new FileReader(config, StandardCharsets.UTF_8);
            JsonParser parser = new JsonParser();

            JsonArray array = null;
            try {
                array = (JsonArray) parser.parse(reader);
            } catch (ClassCastException e) {
                save(config);
            }

            JsonArray modules = null;
            try {
                JsonObject modulesObject = (JsonObject) array.get(0);
                modules = modulesObject.getAsJsonArray("Modules");
            } catch (Exception ignored) {
            }
            if (modules != null) {
                modules.forEach(m -> {
                    try {
                        parseModule(m.getAsJsonObject());
                    } catch (NullPointerException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }

            if (isRu()) Command.sendMessage("Загружен конфиг " + config.getName());
            else Command.sendMessage("Loaded " + config.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentConfig = config;
        saveCurrentConfig();
    }

    public void loadModuleOnly(File config, Module module) {
        try {
            FileReader reader = new FileReader(config);
            JsonParser parser = new JsonParser();

            JsonArray array = null;
            try {
                array = (JsonArray) parser.parse(reader);
            } catch (ClassCastException ignored) {
            }

            JsonArray modules = null;
            try {
                JsonObject modulesObject = (JsonObject) Objects.requireNonNull(array).get(0);
                modules = modulesObject.getAsJsonArray("Modules");
            } catch (Exception ignored) {
            }

            if (modules != null) {
                modules.forEach(m -> {
                    Module module1 = ThunderHack.moduleManager.modules.stream()
                            .filter(m1 -> m.getAsJsonObject().getAsJsonObject(m1.getName()) != null)
                            .findFirst().orElse(null);

                    if (module1 == null)
                        return;

                    if (Objects.equals(module.getName(), module1.getName())) {
                        try {
                            parseModule(m.getAsJsonObject());
                        } catch (NullPointerException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                });
            }
            if (isRu()) Command.sendMessage("Загружен модуль " + module.getName() + " с конфига " + config.getName());
            else Command.sendMessage("Loaded " + module.getName() + " from " + config.getName());
        } catch (IOException e) {
            e.printStackTrace();
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
        saveSearch();
        try {
            if (!config.exists()) {
                config.createNewFile();
            }
            JsonArray array = new JsonArray();

            JsonObject modulesObj = new JsonObject();
            modulesObj.add("Modules", getModuleArray());
            array.add(modulesObj);

            FileWriter writer = new FileWriter(config, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(array, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseModule(JsonObject object) throws NullPointerException {
        Module module = ThunderHack.moduleManager.modules.stream()
                .filter(m -> object.getAsJsonObject(m.getName()) != null)
                .findFirst()
                .orElse(null);

        if (module != null) {
            JsonObject mobject = object.getAsJsonObject(module.getName());

            for (Setting setting2 : module.getSettings()) {
                try {
                    switch (setting2.getType()) {
                        case "Parent":
                            continue;
                        case "Boolean":
                            setting2.setValue(mobject.getAsJsonPrimitive(setting2.getName()).getAsBoolean());
                            continue;
                        case "Double":
                            setting2.setValue(mobject.getAsJsonPrimitive(setting2.getName()).getAsDouble());
                            continue;
                        case "Float":
                            setting2.setValue(mobject.getAsJsonPrimitive(setting2.getName()).getAsFloat());
                            continue;
                        case "Integer":
                            setting2.setValue(mobject.getAsJsonPrimitive(setting2.getName()).getAsInt());
                            continue;
                        case "String":
                            setting2.setValue(mobject.getAsJsonPrimitive(setting2.getName()).getAsString().replace("_", " "));
                            continue;
                        case "Bind":
                            try {
                                JsonArray bindArray = mobject.getAsJsonArray(setting2.getName());
                                if (bindArray.get(0).getAsString().contains("M")) {
                                    setting2.setValue(new Bind(Integer.parseInt(bindArray.get(0).getAsString().replace("M", "")), true, bindArray.get(1).getAsBoolean()));
                                } else {
                                    setting2.setValue(new Bind(Integer.parseInt(bindArray.get(0).getAsString()), false, bindArray.get(1).getAsBoolean()));
                                }
                            } catch (Exception ignored) {
                            }
                            continue;
                        case "ColorSetting":
                            JsonArray array = mobject.getAsJsonArray(setting2.getName());
                            ((ColorSetting) setting2.getValue()).setColor(array.get(0).getAsInt());
                            ((ColorSetting) setting2.getValue()).setRainbow(array.get(1).getAsBoolean());
                            ((ColorSetting) setting2.getValue()).setGlobalOffset(array.get(2).getAsInt());
                            continue;
                        case "PositionSetting":
                            JsonArray array3 = mobject.getAsJsonArray(setting2.getName());
                            ((PositionSetting) setting2.getValue()).setX(array3.get(0).getAsFloat());
                            ((PositionSetting) setting2.getValue()).setY(array3.get(1).getAsFloat());
                            continue;
                        case "BooleanParent":
                            ((BooleanParent) setting2.getValue()).setEnabled(mobject.getAsJsonPrimitive(setting2.getName()).getAsBoolean());
                            continue;
                        case "Enum":
                            try {
                                EnumConverter converter = new EnumConverter(((Enum) setting2.getValue()).getClass());
                                Enum value = converter.doBackward(mobject.getAsJsonPrimitive(setting2.getName()));
                                setting2.setValue((value == null) ? setting2.getDefaultValue() : value);
                            } catch (Exception ignored) {
                            }
                    }
                } catch (Exception e) {
                    System.out.println(module.getName());
                    System.out.println(setting2);
                    e.printStackTrace();
                }
            }
        }
    }

    private @NotNull JsonArray getModuleArray() {
        JsonArray modulesArray = new JsonArray();
        for (Module m : ThunderHack.moduleManager.modules) {
            modulesArray.add(getModuleObject(m));
        }
        return modulesArray;
    }

    public JsonObject getModuleObject(@NotNull Module m) {
        JsonObject attribs = new JsonObject();
        JsonParser jp = new JsonParser();

        for (Setting setting : m.getSettings()) {
            if (setting.isColorSetting()) {
                JsonArray array = new JsonArray();
                array.add(new JsonPrimitive(((ColorSetting) setting.getValue()).getRawColor()));
                array.add(new JsonPrimitive(((ColorSetting) setting.getValue()).isRainbow()));
                array.add(new JsonPrimitive(((ColorSetting) setting.getValue()).getGlobalOffset()));
                attribs.add(setting.getName(), array);
                continue;
            }
            if (setting.isPositionSetting()) {
                JsonArray array = new JsonArray();
                float num2 = ((PositionSetting) setting.getValue()).getX();
                float num1 = ((PositionSetting) setting.getValue()).getY();
                array.add(new JsonPrimitive(num2));
                array.add(new JsonPrimitive(num1));

                attribs.add(setting.getName(), array);
                continue;
            }
            if (setting.isBooleanParent()) {
                attribs.add(setting.getName(), jp.parse(String.valueOf(((BooleanParent) setting.getValue()).isEnabled())));
                continue;
            }
            if (setting.isBindSetting()) {
                Bind b = (Bind) setting.getValue();
                JsonArray array = new JsonArray();
                boolean hold = ((Bind) setting.getValue()).isHold();
                if (b.isMouse())
                    array.add(jp.parse(b.getBind()));
                else
                    array.add(new JsonPrimitive(b.getKey()));
                array.add(new JsonPrimitive(hold));
                attribs.add(setting.getName(), array);
                continue;
            }
            if (setting.isStringSetting()) {
                String str = (String) setting.getValue();
                attribs.add(setting.getName(), jp.parse(str.replace(" ", "_")));
                continue;
            }
            if (setting.isEnumSetting()) {
                EnumConverter converter = new EnumConverter(((Enum) setting.getValue()).getClass());
                attribs.add(setting.getName(), converter.doForward((Enum) setting.getValue()));
                continue;
            }
            try {
                attribs.add(setting.getName(), jp.parse(setting.getValueAsString()));
            } catch (Exception ignored) {
            }
        }

        JsonObject moduleObject = new JsonObject();
        moduleObject.add(m.getName(), attribs);
        return moduleObject;
    }

    public boolean delete(@NotNull File file) {
        return file.delete();
    }

    public boolean delete(String name) {
        File file = new File(CONFIGS_FOLDER, name + ".th");
        if (!file.exists()) {
            return false;
        }
        return delete(file);
    }

    public List<String> getConfigList() {
        if (!MAIN_FOLDER.exists() || MAIN_FOLDER.listFiles() == null) return null;

        List<String> list = new ArrayList<>();

        if (CONFIGS_FOLDER.listFiles() != null) {
            for (File file : Arrays.stream(Objects.requireNonNull(CONFIGS_FOLDER.listFiles())).filter(f -> f.getName().endsWith(".th")).collect(Collectors.toList())) {
                list.add(file.getName().replace(".th", ""));
            }
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        currentConfig = new File(CONFIGS_FOLDER, name + ".th");
        return currentConfig;
    }

    public void loadChestStealer() {
        try {
            File file = new File(CONFIG_FOLDER_NAME + "/misc/search.txt");

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        ModuleManager.chestStealer.items.add(reader.readLine());
                    }

                }
            }
        } catch (Exception ignored) {
        }
    }

    public void saveChestStealer() {
        File file = new File(CONFIG_FOLDER_NAME + "/misc/search.txt");
        try {
            file.createNewFile();
        } catch (Exception ignored) {

        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String item : ModuleManager.chestStealer.items) {
                writer.write(item + "\n");
            }
        } catch (Exception ignored) {
        }
    }

    public void loadInvCleaner() {
        try {
            File file = new File("ThunderHackRecode/misc/invcleaner.txt");

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        ModuleManager.inventoryCleaner.items.add(reader.readLine());
                    }

                }
            }
        } catch (Exception ignored) {
        }
    }

    public void saveInvCleaner() {
        File file = new File("ThunderHackRecode/misc/invcleaner.txt");
        try {
            file.createNewFile();
        } catch (Exception ignored) {

        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String item : ModuleManager.inventoryCleaner.items) {
                writer.write(item + "\n");
            }
        } catch (Exception ignored) {
        }
    }

    public void loadAutoBuy() {
        try {
            File file = new File(ConfigManager.CONFIG_FOLDER_NAME + "/misc/autobuy.txt");

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                    while (reader.ready()) {
                        String[] nameKey = reader.readLine().split(";");
                        String name = nameKey[0];
                        String price = nameKey[1];
                        String count = nameKey[2];
                        String enchantments = nameKey[3];
                        String attributes = nameKey[4];
                        String checkForStar = nameKey[5];

                        ArrayList<Pair<String, Integer>> enchList = new ArrayList<>();
                        for (String enc : enchantments.split(" ")) {
                            Pair<String, Integer> ench = AutoBuy.parseEnchant(enc);
                            if (ench != null)
                                enchList.add(ench);
                        }

                        ArrayList<String> attribList = new ArrayList<>();
                        for (String attr : attributes.split(",")) {
                            if (!attr.isEmpty())
                                attribList.add(attr);
                        }

                        AutoBuy.items.add(new AutoBuyItem(InventoryUtility.getItem(name), enchList, attribList, Integer.parseInt(price), Integer.parseInt(count), Boolean.parseBoolean(checkForStar)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void saveAutoBuy() {
        File file = new File(ConfigManager.CONFIG_FOLDER_NAME + "/misc/autobuy.txt");
        try {
            if (new File(ConfigManager.CONFIG_FOLDER_NAME).mkdirs()) {
                file.createNewFile();
            }
        } catch (Exception ignored) {

        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (AutoBuyItem item : AutoBuy.items) {
                writer.write(item.getItem().getTranslationKey().replace("block.minecraft.", "").replace("item.minecraft.", "") + ";" + item.getPrice() + ";" + item.getCount() + ";" + ArrayToString(item.getEnchantmentsToArray()) + ";" + ArrayToStringСomma(item.getAttributesToArray()) + ";" + item.checkForStar() + "\n");
            }
        } catch (Exception ignored) {
        }
    }

    public List<String> getAutoBuyList() {
        if (!MAIN_FOLDER.exists() || MAIN_FOLDER.listFiles() == null) return null;

        List<String> list = new ArrayList<>();

        if (AUTOBUY_FOLDER.listFiles() != null) {
            for (File file : Arrays.stream(Objects.requireNonNull(AUTOBUY_FOLDER.listFiles())).filter(f -> f.getName().endsWith(".txt")).collect(Collectors.toList())) {
                list.add(file.getName().replace(".txt", ""));
            }
        }
        return list;
    }

    public static String ArrayToString(Object[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(String.valueOf(a[i]));
            if (i == iMax)
                return b.toString();
            b.append(" ");
        }
    }

    public static String ArrayToStringСomma(Object[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(String.valueOf(a[i]));
            if (i == iMax)
                return b.toString();
            b.append(",");
        }
    }
}
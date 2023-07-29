package thunder.hack.core;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;

import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.render.Search;
import thunder.hack.modules.Module;
import net.minecraft.block.Block;
import thunder.hack.cmd.impl.SearchCommand;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static thunder.hack.modules.Module.mc;


public class ConfigManager  {

        public static  File MainFolder = new File(mc.runDirectory, "ThunderHackRecode");
        public static  File ConfigsFolder = new File(MainFolder, "configs");
        public static  File CustomImages = new File(MainFolder, "images");
        public static  File TempFolder = new File(MainFolder, "temp");
            public static  File SkinsFolder = new File(TempFolder, "skins");
            public static  File CapesFolder = new File(TempFolder, "capes");
            public static  File HeadsFolder = new File(TempFolder, "heads");
            public static  File DiscordEmbeds = new File(TempFolder, "embeds");
        public static  File MiscFolder = new File(MainFolder, "misc");
            public static  File KitsFolder = new File(MiscFolder, "kits");
            //friends
            //enemies
            //webhook
            //rpc
            //autoEz
            //currentcfg
            //macro
            //search
            //alts

    public static boolean firstLaunch = false;


    public ConfigManager(){
        if (!MainFolder.exists()){
            MainFolder.mkdirs();
            firstLaunch = true;
        }
        if (!ConfigsFolder.exists()) ConfigsFolder.mkdirs();
        if (!CustomImages.exists()) CustomImages.mkdirs();
        if (!TempFolder.exists()) TempFolder.mkdirs();
        if (!SkinsFolder.exists()) SkinsFolder.mkdirs();
        if (!CapesFolder.exists()) CapesFolder.mkdirs();
        if (!HeadsFolder.exists()) HeadsFolder.mkdirs();
        if (!MiscFolder.exists()) MiscFolder.mkdirs();
        if (!KitsFolder.exists()) KitsFolder.mkdirs();
        if (!DiscordEmbeds.exists()) DiscordEmbeds.mkdirs();
        loadSearch();
    }


    public void loadSearch() {
        try {
            File file = new File("ThunderHackRecode/misc/search.txt");

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        String name = reader.readLine();
                        Search.defaultBlocks.add(SearchCommand.getRegisteredBlock(name));
                    }

                }
            }
        } catch (Exception ignored) {
        }
    }


    public void loadDefault(String name) {
        MainSettings.Language prevLang = MainSettings.language.getValue();
        Path path = Paths.get("ThunderHackRecode/configs/" + name + ".th");
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("cfg/" + name + ".th");
             OutputStream out = Files.newOutputStream(path)) {
            if (in  == null) return;
            IOUtils.copy(in, out);
            load(name);
            MainSettings.language.setValue(prevLang);
        } catch (IOException e) {e.printStackTrace();}
    }


    public void saveSearch() {
        File file = new File("ThunderHackRecode/misc/search.txt");
        try {
            new File("ThunderHackRecode").mkdirs();
            file.createNewFile();
        } catch (Exception e) {

        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Block name : Search.defaultBlocks) {
                writer.write(name.getTranslationKey() + "\n");
            }
        } catch (Exception ignored) {
        }
    }


    public static String getConfigDate(String name) {
        File file = new File(ConfigsFolder, name + ".th");
        if (!file.exists()) {
            return "none";
        }
        long x = file.lastModified();
        DateFormat obj = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Date sol = new Date(x);
        return obj.format(sol);
    }

    public File currentConfig = null;


    public void load(String name) {
        File file = new File(ConfigsFolder, name + ".th");
        if (!file.exists()) {
            if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                Command.sendMessage("Конфига " + name + " не существует!");
            } else {
                Command.sendMessage("Config " + name + " does not exist!");
            }
            return;
        }

        if(currentConfig != null){
            save(currentConfig);
        }

        Thunderhack.moduleManager.onUnload();
        Thunderhack.moduleManager.onUnloadPost();
        load(file);
        Thunderhack.moduleManager.onLoad();

    }

    public void load(File config) {
        if (!config.exists()) save(config);
        try {
            FileReader reader = new FileReader(config);
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
            } catch (Exception ignored) {}
            if (modules != null) {
                modules.forEach(m -> {
                    try {
                        parseModule(m.getAsJsonObject());
                    } catch (NullPointerException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }

            if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                Command.sendMessage("Загружен конфиг " + config.getName());
            } else {
                Command.sendMessage("Loaded " + config.getName());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        currentConfig = config;
        saveCurrentConfig();
    }


    public void save(String name) {
        File file = new File(ConfigsFolder, name + ".th");

        if (file.exists()) {
            if(MainSettings.language.getValue() == MainSettings.Language.RU) {
                Command.sendMessage("Конфиг " + name + " уже существует!");
            } else {
                Command.sendMessage("Config " + name + " already exists!");
            }
            return;
        }

        save(file);
        if(MainSettings.language.getValue() == MainSettings.Language.RU) {
            Command.sendMessage("Конфиг " + name + " успешно сохранен!");
        } else {
            Command.sendMessage("Config " + name + " successfully saved!");
        }
    }


    public void save(File config) {
        saveSearch();
        try {
            if (!config.exists()) {
                config.createNewFile();
            }
            JsonArray array = new JsonArray();

            JsonObject modulesObj = new JsonObject();
            modulesObj.add("Modules", getModuleArray());
            array.add(modulesObj);


            FileWriter writer = new FileWriter(config);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(array, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private void parseModule(JsonObject object) throws NullPointerException {

        Module module = Thunderhack.moduleManager.modules.stream()
                .filter(m -> object.getAsJsonObject(m.getName()) != null)
                .findFirst().orElse(null);

        if (module != null) {
            JsonObject mobject = object.getAsJsonObject(module.getName());

            for(Setting setting2 : module.getSettings()){
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
                            if(mobject.getAsJsonPrimitive(setting2.getName()).getAsString().contains("M"))
                                setting2.setValue( new Bind(Integer.parseInt(mobject.getAsJsonPrimitive(setting2.getName()).getAsString().replace("M","")),true));
                            else
                                setting2.setValue( new Bind(mobject.getAsJsonPrimitive(setting2.getName()).getAsInt(),false));
                            continue;
                        case "ColorSetting":
                            JsonArray array = mobject.getAsJsonArray(setting2.getName());
                            ((ColorSetting) setting2.getValue()).setColor(array.get(0).getAsInt());
                            ((ColorSetting) setting2.getValue()).setCycle(array.get(1).getAsBoolean());
                            ((ColorSetting) setting2.getValue()).setGlobalOffset(array.get(2).getAsInt());
                            continue;
                        case "PositionSetting":
                            JsonArray array3 = mobject.getAsJsonArray(setting2.getName());
                            ((PositionSetting) setting2.getValue()).setX(array3.get(0).getAsFloat());
                            ((PositionSetting) setting2.getValue()).setY(array3.get(1).getAsFloat());
                            continue;
                        case "SubBind":
                            setting2.setValue((new SubBind.SubBindConverter()).doBackward(mobject.getAsJsonPrimitive(setting2.getName())));
                            continue;
                        case "Enum":
                            try {
                                EnumConverter converter = new EnumConverter(((Enum) setting2.getValue()).getClass());
                                Enum value = converter.doBackward(mobject.getAsJsonPrimitive(setting2.getName()));
                                setting2.setValue((value == null) ? setting2.getDefaultValue() : value);
                            } catch (Exception ignored) {
                            }
                    }
                } catch (Exception e){
                    System.out.println(module.getName());
                    System.out.println(setting2);
                    e.printStackTrace();
                }
            }
        }
    }

    private JsonArray getModuleArray() {
        JsonArray modulesArray = new JsonArray();
        for (Module m : Thunderhack.moduleManager.modules) {
            modulesArray.add(getModuleObject(m));
        }
        return modulesArray;
    }

    public JsonObject getModuleObject(Module m) {
        JsonObject attribs = new JsonObject();
        JsonParser jp = new JsonParser();

            for (Setting setting : m.getSettings()) {

                if(setting.isColorSetting()){
                    JsonArray array = new JsonArray();
                    array.add(new JsonPrimitive(((ColorSetting) setting.getValue()).getRawColor()));
                    array.add(new JsonPrimitive(((ColorSetting) setting.getValue()).isCycle()));
                    array.add(new JsonPrimitive(((ColorSetting) setting.getValue()).getGlobalOffset()));
                    attribs.add(setting.getName(), array);
                    continue;
                }
                if(setting.isPositionSetting()){
                    JsonArray array = new JsonArray();
                    float num2 = ((PositionSetting) setting.getValue()).getX();
                    float num1 = ((PositionSetting) setting.getValue()).getY();
                    array.add(new JsonPrimitive(num2));
                    array.add(new JsonPrimitive(num1));

                    attribs.add(setting.getName(), array);
                    continue;
                }

                if(Objects.equals(setting.getName(), "Keybind")){

                    if(m.getBind().isMouse())
                        attribs.add("Keybind", jp.parse(m.getBind().getBind()));
                    else
                        attribs.add("Keybind",new JsonPrimitive(m.getBind().getKey()));

                    continue;
                }
                if (setting.isStringSetting()) {
                    String str = (String) setting.getValue();
                    setting.setValue(str.replace(" ", "_"));
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

    public boolean delete(File file) {
        return file.delete();
    }


    public boolean delete(String name) {
        File file = new File(ConfigsFolder, name + ".th");
        if (!file.exists()) {
            return false;
        }
        return delete(file);
    }

    public List<String> getConfigList() {
        if (!MainFolder.exists() || MainFolder.listFiles() == null) return null;

        List<String> list = new ArrayList<>();

        if (ConfigsFolder.listFiles() != null) {
            for(File file : Arrays.stream(ConfigsFolder.listFiles()).filter(f -> f.getName().endsWith(".th")).collect(Collectors.toList())){
                list.add(file.getName().replace(".th",""));
            }
        }
        return list;
    }


    public void saveCurrentConfig() {
        File file = new File("ThunderHackRecode/misc/currentcfg.txt");
        try {
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                writer.write(currentConfig.getName().replace(".th",""));
                writer.close();
            } else {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(currentConfig.getName().replace(".th",""));
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getCurrentConfig() {
        File file = new File("ThunderHackRecode/misc/currentcfg.txt");
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
        currentConfig = new File(ConfigsFolder,name + ".th");
        return currentConfig;
    }
}
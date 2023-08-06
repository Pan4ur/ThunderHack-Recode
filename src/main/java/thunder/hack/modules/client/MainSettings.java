package thunder.hack.modules.client;

import thunder.hack.setting.Setting;
import thunder.hack.modules.Module;

public class MainSettings extends Module {


    public MainSettings() {
        super("ClientSettings", "Настройки клиента", Category.CLIENT);
    }

    public static Setting<Boolean> renderRotations = new Setting<>("renderRotations", true);
    public static Setting<Language> language = new Setting("Language", Language.ENG);

    public enum Language {
        RU,
        ENG
    }

    public static boolean isRu(){
        return language.getValue() == Language.RU;
    }
}

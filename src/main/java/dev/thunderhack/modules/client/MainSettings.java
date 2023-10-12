package dev.thunderhack.modules.client;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;

public class MainSettings extends Module {
    public static Setting<Boolean> futureCompatibility = new Setting<>("FutureCompatibility", false);
    public static Setting<Boolean> customMainMenu = new Setting<>("CustomMainMenu", true);
    public static Setting<Boolean> renderRotations = new Setting<>("RenderRotations", true);
    public static Setting<Boolean> skullEmoji = new Setting<>("SkullEmoji", true);
    public static Setting<Language> language = new Setting<>("Language", Language.ENG);
    public static Setting<String> prefix = new Setting<>("Prefix", "@");

    public enum Language {
        RU,
        ENG
    }

    public MainSettings() {
        super("ClientSettings", Category.CLIENT);
    }

    public static boolean isRu() {
        return language.getValue() == Language.RU;
    }

    @Override
    public void onUpdate() {
    }
}

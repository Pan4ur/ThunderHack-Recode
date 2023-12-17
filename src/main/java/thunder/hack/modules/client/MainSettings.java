package thunder.hack.modules.client;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public final class MainSettings extends Module {
    public static Setting<Boolean> futureCompatibility = new Setting<>("FutureCompatibility", false);
    public static Setting<Boolean> customMainMenu = new Setting<>("CustomMainMenu", true);
    public static Setting<Boolean> scaleFactorFix = new Setting<>("ScaleFactorFix", false);
    public static Setting<Float> scaleFactorFixValue = new Setting<>("ScaleFactorFixValue", 2f, 0f, 4f);
    public static Setting<Boolean> renderRotations = new Setting<>("RenderRotations", true);
    public static Setting<Boolean> skullEmoji = new Setting<>("SkullEmoji", true);
    public static Setting<Boolean> debug = new Setting<>("Debug", false);
    public static Setting<Boolean> customBob = new Setting<>("CustomBob", true);
    public static Setting<Language> language = new Setting<>("Language", Language.ENG);
    public static Setting<String> prefix = new Setting<>("Prefix", "@");

    private static MainSettings instance;

    public enum Language {
        RU,
        ENG
    }

    public MainSettings() {
        super("ClientSettings", Category.CLIENT);
        instance = this;
    }

    public static boolean isRu() {
        return language.getValue() == Language.RU;
    }

    public static MainSettings getInstance() {
        return instance;
    }
}

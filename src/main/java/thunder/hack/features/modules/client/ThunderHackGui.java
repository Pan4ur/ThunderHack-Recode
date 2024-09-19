package thunder.hack.features.modules.client;

import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

import java.awt.*;

public final class ThunderHackGui extends Module {
    public static final Setting<ColorSetting> onColor1 = new Setting<>("OnColor1", new ColorSetting(new Color(71, 0, 117, 255).getRGB()));
    public static final Setting<ColorSetting> onColor2 = new Setting<>("OnColor2", new ColorSetting(new Color(32, 1, 96, 255).getRGB()));
    public static final Setting<Float> scrollSpeed = new Setting<>("ScrollSpeed", 1f, 0.1F, 2.0F);

    public ThunderHackGui() {
        super("ThunderGui", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        mc.setScreen(ThunderGui.getThunderGui());
        disable();
    }

    public static Color getColorByTheme(int id) {
        return switch (id) {
            case 0 -> new Color(37, 27, 41, 250); // Основная плита
            case 1 -> new Color(50, 35, 60, 250); // плита лого
            case 2 -> new Color(-1); // надпись THUNDERHACK+, белые иконки
            case 3, 8 -> new Color(0x656565); // версия под надписью
            case 4 -> new Color(50, 35, 60, 178); // плита под категориями, выбор режима гуи (выкл)
            case 5 -> new Color(133, 93, 162, 178); // выбор режима гуи (вкл)
            case 6 -> new Color(88, 64, 107, 178); // цвет разделителя качели выбора режима
            case 7 -> new Color(25, 20, 30, 255); // цвет плиты настроек
            case 9 -> new Color(50, 35, 60, 178);
            default -> new Color(37, 27, 41, 250); // плита под категориями
        };
    }
}

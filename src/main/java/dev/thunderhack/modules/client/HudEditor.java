package dev.thunderhack.modules.client;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.render.Render2DEngine;
import dev.thunderhack.gui.hud.HudEditorGui;

import java.awt.*;

public class HudEditor extends Module {
    public HudEditor() {
        super("HudEditor", Module.Category.CLIENT);
    }

    public static final Setting<ClickGui.colorModeEn> colorMode = new Setting<>("ColorMode", ClickGui.colorModeEn.Static);
    public static final Setting<Integer> colorSpeed = new Setting<>("ColorSpeed", 18, 2, 54);
    public static final Setting<Boolean> glow = new Setting<>("Glow", true);
    public static final Setting<ColorSetting> hcolor1 = new Setting<>("Color", new ColorSetting(-6974059));
    public static final Setting<ColorSetting> acolor = new Setting<>("Color2", new ColorSetting(-8365735));
    public static final Setting<ColorSetting> plateColor = new Setting<>("PlateColor", new ColorSetting(new Color(0xE7000000, true).getRGB()));
    public static final Setting<ColorSetting> textColor = new Setting<>("TextColorColor", new ColorSetting(new Color(0xFFFFFFFF, true).getRGB()));
    public static final Setting<Float> hudRound = new Setting<>("HudRound", 6f, 1f, 10f);

    public static Color getColor(int count) {
        int index = count;
        return switch (colorMode.getValue()) {
            case Sky -> Render2DEngine.skyRainbow(colorSpeed.getValue(), index);
            case LightRainbow -> Render2DEngine.rainbow(colorSpeed.getValue(), index, .6f, 1, 1);
            case Rainbow -> Render2DEngine.rainbow(colorSpeed.getValue(), index, 1f, 1, 1);
            case Fade -> Render2DEngine.fade(colorSpeed.getValue(), index, hcolor1.getValue().getColorObject(), 1);
            case DoubleColor -> Render2DEngine.TwoColoreffect(hcolor1.getValue().getColorObject(), acolor.getValue().getColorObject(), Math.abs(System.currentTimeMillis() / 10) / 100.0 + (count));
            case Analogous -> Render2DEngine.interpolateColorsBackAndForth(colorSpeed.getValue(), index, hcolor1.getValue().getColorObject(), Render2DEngine.getAnalogousColor(acolor.getValue().getColorObject()), true);
            default -> hcolor1.getValue().getColorObject();
        };
    }

    @Override
    public void onEnable() {
        mc.setScreen(HudEditorGui.getHudGui());
        disable();
    }
}

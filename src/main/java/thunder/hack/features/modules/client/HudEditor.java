package thunder.hack.features.modules.client;

import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public final class HudEditor extends Module {
    public static final Setting<Boolean> sticky = new Setting<>("Sticky", true);
    public static final Setting<HudStyle> hudStyle = new Setting<>("HudStyle", HudStyle.Blurry);
    public static final Setting<ArrowsStyle> arrowsStyle = new Setting<>("ArrowsStyle", ArrowsStyle.Default);
    public static final Setting<ClickGui.colorModeEn> colorMode = new Setting<>("ColorMode", ClickGui.colorModeEn.Static);
    public static final Setting<Integer> colorSpeed = new Setting<>("ColorSpeed", 18, 2, 54);
    public static final Setting<Boolean> glow = new Setting<>("Light", true);
    public static final Setting<ColorSetting> hcolor1 = new Setting<>("Color", new ColorSetting(-6974059));
    public static final Setting<ColorSetting> acolor = new Setting<>("Color2", new ColorSetting(-8365735));
    public static final Setting<ColorSetting> plateColor = new Setting<>("PlateColor", new ColorSetting(new Color(0xE7000000, true).getRGB()));
    public static final Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(0xFFFFFFFF, true).getRGB()));
    public static final Setting<ColorSetting> textColor2 = new Setting<>("TextColor2", new ColorSetting(new Color(0xFFFFFFFF, true).getRGB()));
    public static final Setting<ColorSetting> blurColor = new Setting<>("BlurColor", new ColorSetting(new Color(0xFF000E25, true).getRGB()));
    public static final Setting<Float> hudRound = new Setting<>("HudRound", 4f, 1f, 7f);
    public static final Setting<Float> alpha = new Setting<>("Alpha", 0.9f, 0f, 1f);
    public static final Setting<Float> blend = new Setting<>("Blend", 10f, 1f, 15f);
    public static final Setting<Float> outline = new Setting<>("Outline", 0.5f, 0f, 2.5f);
    public static final Setting<Float> glow1 = new Setting<>("Glow", 0.5f, 0f, 1f);
    public static final Setting<Float> blurOpacity = new Setting<>("BlurOpacity", 0.55f, 0f, 1f);
    public static final Setting<Float> blurStrength = new Setting<>("BlurStrength", 20f, 5f, 50f);

    public HudEditor() {
        super("HudEditor", Module.Category.CLIENT);
    }

    public static Color getColor(int count) {
        return switch (colorMode.getValue()) {
            case Sky -> Render2DEngine.skyRainbow(colorSpeed.getValue(), count);
            case LightRainbow -> Render2DEngine.rainbow(colorSpeed.getValue(), count, .6f, 1, 1);
            case Rainbow -> Render2DEngine.rainbow(colorSpeed.getValue(), count, 1f, 1, 1);
            case Fade -> Render2DEngine.fade(colorSpeed.getValue(), count, hcolor1.getValue().getColorObject(), 1);
            case DoubleColor ->
                    Render2DEngine.TwoColoreffect(hcolor1.getValue().getColorObject(), acolor.getValue().getColorObject(), colorSpeed.getValue(), count);
            case Analogous ->
                    Render2DEngine.interpolateColorsBackAndForth(colorSpeed.getValue(), count, hcolor1.getValue().getColorObject(), Render2DEngine.getAnalogousColor(acolor.getValue().getColorObject()), true);
            default -> hcolor1.getValue().getColorObject();
        };
    }

    @Override
    public void onEnable() {
        mc.setScreen(HudEditorGui.getHudGui());
        disable();
    }

    public enum ArrowsStyle {
        Default, New
    }

    public enum HudStyle {
        Blurry, Glowing
    }
}

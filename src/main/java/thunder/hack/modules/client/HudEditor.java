package thunder.hack.modules.client;

import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.modules.Module;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class HudEditor extends Module{

    public HudEditor() {
        super("HudEditor", "худ изменять да", Module.Category.CLIENT);
    }


    public static final Setting<ClickGui.colorModeEn> colorMode = new Setting<>("ColorMode", ClickGui.colorModeEn.Static);
    public static final Setting<Integer> colorSpeed = new Setting<>("ColorSpeed", 18, 2, 54);
    public static final Setting <Boolean> glow =  new Setting <>( "Glow" , true );
    public static final Setting<ColorSetting> hcolor1 = new Setting<>("Color", new ColorSetting(-6974059));
    public static final Setting<ColorSetting> acolor = new Setting<>("Color2", new ColorSetting(-8365735));
    public static final Setting<ColorSetting> plateColor = new Setting<>("PlateColor", new ColorSetting(new Color(0xE7000000, true).getRGB()));
    public static final Setting<ColorSetting> textColor = new Setting<>("TextColorColor", new ColorSetting(new Color(0xFFFFFFFF, true).getRGB()));
    public static final Setting<Float> hudRound = new Setting<>("HudRound", 6f, 1f, 10f);
    public static final Setting <Boolean> fpsEater =  new Setting <>( "fpsEater3000" , true );


    public static Color getColor(int count) {
        int index = count;
        switch (colorMode.getValue()) {
            case Sky -> {
                return Render2DEngine.skyRainbow(colorSpeed.getValue(), index);
            }
            case LightRainbow -> {
                return Render2DEngine.rainbow((int) colorSpeed.getValue(), index, .6f, 1, 1);
            }
            case Rainbow -> {
                return Render2DEngine.rainbow((int) colorSpeed.getValue(), index, 1f, 1, 1);
            }
            case Fade -> {
                return Render2DEngine.fade((int) colorSpeed.getValue(), index, hcolor1.getValue().getColorObject(), 1);
            }
            case DoubleColor -> {
                return Render2DEngine.TwoColoreffect(hcolor1.getValue().getColorObject(), acolor.getValue().getColorObject(), Math.abs(System.currentTimeMillis() / 10) / 100.0 + 3.0F * (count * 2.55) / 60);
            }
            case Analogous -> {
                Color analogous = Render2DEngine.getAnalogousColor(acolor.getValue().getColorObject());
                return Render2DEngine.interpolateColorsBackAndForth((int) colorSpeed.getValue(), index, hcolor1.getValue().getColorObject(), analogous, true);
            }
            default -> {
                return hcolor1.getValue().getColorObject();
            }
        }
    }

    @Override
    public void onEnable(){
        mc.setScreen(HudEditorGui.getHudGui());
        toggle();
    }
}

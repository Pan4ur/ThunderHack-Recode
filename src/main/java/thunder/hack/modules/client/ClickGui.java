package thunder.hack.modules.client;

import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

public class ClickGui extends Module {
    public Setting<Gradient> gradientMode = new Setting<>("Gradient", Gradient.LeftToRight);
    public Setting<TextSide> textSide = new Setting<>("TextSide", TextSide.Left);
    public Setting<scrollModeEn> scrollMode = new Setting<>("ScrollMode", scrollModeEn.Old);
    public Setting<Integer> catHeight = new Setting<>("CategoryHeight", 300, 100, 720);
    public final Setting<Boolean> descriptions = new Setting<>("Descriptions", true);
    public final Setting<Boolean> tips = new Setting<>("Tips", true);


    public ClickGui() {
        super("ClickGui", Module.Category.CLIENT);
    }

    public static ClickGui getInstance() {
        return ModuleManager.clickGui;
    }

    @Override
    public void onEnable() {
        setGui();
    }

    public void setGui() {
        mc.setScreen(ClickGUI.getClickGui());
    }

    @Override
    public void onUpdate() {
        if (!(ClickGui.mc.currentScreen instanceof ClickGUI))
            disable();
    }

    public enum colorModeEn {
        Static,
        Sky,
        LightRainbow,
        Rainbow,
        Fade,
        DoubleColor,
        Analogous
    }

    public enum scrollModeEn {
        New,
        Old
    }

    public enum TextSide {
        Left,
        Center
    }

    public enum Gradient {
        UpsideDown,
        LeftToRight,
        both
    }
}


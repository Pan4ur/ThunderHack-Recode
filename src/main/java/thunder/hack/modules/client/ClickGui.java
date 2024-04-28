package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.setting.impl.ColorSetting;

public class ClickGui extends Module {
    public final Setting<Gradient> gradientMode = new Setting<>("Gradient", Gradient.LeftToRight);
    public final Setting<TextSide> textSide = new Setting<>("TextSide", TextSide.Left);
    public final Setting<scrollModeEn> scrollMode = new Setting<>("ScrollMode", scrollModeEn.Old);
    public final Setting<Integer> catHeight = new Setting<>("CategoryHeight", 300, 100, 720, v -> scrollMode.is(scrollModeEn.New));
    public final Setting<Boolean> descriptions = new Setting<>("Descriptions", true);
    public final Setting<Boolean> tips = new Setting<>("Tips", true);
    public final Setting<Integer> moduleWidth = new Setting<>("ModuleWidth", 100, 50, 200);
    public final Setting<Integer> moduleHeight = new Setting<>("ModuleHeight", 14, 8, 25);
    public final Setting<Integer> settingFontScale = new Setting<>("SettingFontScale", 12, 6, 24);
    public final Setting<Integer> modulesFontScale = new Setting<>("ModulesFontScale", 14, 6, 24);
    public final Setting<BooleanParent> gear = new Setting<>("Gear", new BooleanParent(true));
    public final Setting<Integer> gearScale = new Setting<>("GearScale", 60, 6, 300).withParent(gear);

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

    @EventHandler
    public void onSetting(EventSetting e) {
        try {
            if (e.getSetting() == settingFontScale)
                FontRenderers.sf_medium_mini = FontRenderers.create(settingFontScale.getValue(), "sf_medium");

            if (e.getSetting() == modulesFontScale)
                FontRenderers.sf_medium_modules = FontRenderers.create(modulesFontScale.getValue(), "sf_medium");
        } catch (Exception ex) {
        }
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


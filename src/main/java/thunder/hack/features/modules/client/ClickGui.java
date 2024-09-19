package thunder.hack.features.modules.client;

import baritone.api.BaritoneAPI;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Identifier;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;

public class ClickGui extends Module {
    public final Setting<Gradient> gradientMode = new Setting<>("Gradient", Gradient.LeftToRight);
    public final Setting<TextSide> textSide = new Setting<>("TextSide", TextSide.Left);
    public final Setting<scrollModeEn> scrollMode = new Setting<>("ScrollMode", scrollModeEn.Old);
    public final Setting<Integer> catHeight = new Setting<>("CategoryHeight", 300, 100, 720, v -> scrollMode.is(scrollModeEn.New));
    public final Setting<Boolean> descriptions = new Setting<>("Descriptions", true);
    public final Setting<Boolean> blur = new Setting<>("Blur", true);
    public final Setting<Boolean> tips = new Setting<>("Tips", true);
    public final Setting<Boolean> pauseBaritone = new Setting<>("PauseBaritone", false);
    public final Setting<Image> image = new Setting<>("Image", Image.None);
    public final Setting<Integer> moduleWidth = new Setting<>("ModuleWidth", 100, 50, 200);
    public final Setting<Integer> moduleHeight = new Setting<>("ModuleHeight", 14, 8, 25);
    public final Setting<Integer> settingFontScale = new Setting<>("SettingFontScale", 12, 6, 24);
    public final Setting<Integer> modulesFontScale = new Setting<>("ModulesFontScale", 14, 6, 24);
    public static final Setting<BooleanSettingGroup> gear = new Setting<>("Gear", new BooleanSettingGroup(true));
    public final Setting<Integer> gearScale = new Setting<>("GearScale", 60, 6, 300).addToGroup(gear);
    public static Setting<Float> gearDuration = new Setting<>("GearDuration", 0.5f, 0.1f, 2f).addToGroup(gear);
    public static Setting<Integer> gearStop = new Setting<>("GearStop", 25, 10, 45).addToGroup(gear);
    public final Setting<Boolean> closeAnimation = new Setting<>("CloseAnimation", true);

    public ClickGui() {
        super("ClickGui", Module.Category.CLIENT);
    }

    @Override
    public void onEnable() {
        if(pauseBaritone.getValue() && !fullNullCheck() && ThunderHack.baritone){
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
        }
        setGui();
    }

    @Override
    public void onDisable() {
        if(pauseBaritone.getValue() && !fullNullCheck() && ThunderHack.baritone){
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
        }
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

            if (e.getSetting() == image)
                ClickGUI.getClickGui().imageAnimation.reset();
        } catch (Exception ignored) {
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

    public enum Image {
        None("", 0, 0, new int[]{0, 0}, 0),
        Bdsm("bdsm.png", 1414, 823, new int[]{-100, 90}, 900),
        Cutie("cutie.png", 476, 490, new int[]{500, 60}, 500),
        Cutie2("cutie2.png", 540, 540, new int[]{500, 60}, 500),
        Nya("nya.png", 246, 238, new int[]{500, 60}, 500),
        Smile("smile.png", 324, 406, new int[]{500, 40}, 500),
        Bat("bat.png", 352, 503, new int[]{500, 40}, 500),
        Gacha("gacha.png", 592, 592, new int[]{500, 60}, 500),
        NFT("nft.png", 562, 494, new int[]{400, 110}, 500),
        ThousandSeven("thousand_seven.png", 380, 336, new int[]{400, 130}, 500);

        public final int fileWidth;
        public final int fileHeight;
        public final Identifier file;
        public final int[] pos;
        public final int size;

        Image(String file, int fileWidth, int fileHeight, int[] pos, int size) {
            this.fileHeight = fileHeight;
            this.fileWidth = fileWidth;
            this.file = Identifier.of("thunderhack", "textures/gui/images/" + file);
            this.pos = pos;
            this.size = size;
        }
    }
}


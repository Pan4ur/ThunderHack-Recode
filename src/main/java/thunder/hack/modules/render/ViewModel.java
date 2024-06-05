package thunder.hack.modules.render;

import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;

public class ViewModel extends Module {
    public ViewModel() {
        super("ViewModel", Category.RENDER);
    }

    public final Setting<SettingGroup> mainHand = new Setting<>("MainHand", new SettingGroup(false, 0));
    public final Setting<Float> positionMainX = new Setting<>("positionMainX", 0f, -3.0f, 3f);
    public final Setting<Float> positionMainY = new Setting<>("positionMainY", 0f, -3.0f, 3f);
    public final Setting<Float> positionMainZ = new Setting<>("positionMainZ", 0f, -3.0f, 3f);

    public final Setting<SettingGroup> rotationMain = new Setting<>("RotationMain", new SettingGroup(false, 1)).addToGroup(mainHand);
    public final Setting<Float> rotationMainX = new Setting<>("rotationMainX", 0f, -180.0f, 180f).addToGroup(rotationMain);
    public final Setting<Float> rotationMainY = new Setting<>("rotationMainY", 0f, -180.0f, 180f).addToGroup(rotationMain);
    public final Setting<Float> rotationMainZ = new Setting<>("rotationMainZ", 0f, -180.0f, 180f).addToGroup(rotationMain);

    public final Setting<SettingGroup> animateMain = new Setting<>("AnimateMain", new SettingGroup(false, 1)).addToGroup(mainHand);
    public final Setting<Boolean> animateMainX = new Setting<>("animateMainX", false).addToGroup(animateMain);
    public final Setting<Boolean> animateMainY = new Setting<>("animateMainY", false).addToGroup(animateMain);
    public final Setting<Boolean> animateMainZ = new Setting<>("animateMainZ", false).addToGroup(animateMain);
    public final Setting<Float> speedAnimateMain = new Setting<>("speedAnimateMain", 1f, 1f, 5f).addToGroup(rotationMain);

    public final Setting<SettingGroup> offHand = new Setting<>("OffHand", new SettingGroup(false, 0));
    public final Setting<Float> positionOffX = new Setting<>("positionOffX", 0f, -3.0f, 3f).addToGroup(offHand);
    public final Setting<Float> positionOffY = new Setting<>("positionOffY", 0f, -3.0f, 3f).addToGroup(offHand);
    public final Setting<Float> positionOffZ = new Setting<>("positionOffZ", 0f, -3.0f, 3f).addToGroup(offHand);

    public final Setting<SettingGroup> rotationOff = new Setting<>("RotationOff", new SettingGroup(false, 1)).addToGroup(offHand);
    public final Setting<Float> rotationOffX = new Setting<>("rotationOffX", 0f, -180.0f, 180f).addToGroup(rotationOff);
    public final Setting<Float> rotationOffY = new Setting<>("rotationOffY", 0f, -180.0f, 180f).addToGroup(rotationOff);
    public final Setting<Float> rotationOffZ = new Setting<>("rotationOffZ", 0f, -180.0f, 180f).addToGroup(rotationOff);

    public final Setting<SettingGroup> animateOff = new Setting<>("AnimateOff", new SettingGroup(false, 1)).addToGroup(offHand);
    public final Setting<Boolean> animateOffX = new Setting<>("animateOffX", false).addToGroup(animateOff);
    public final Setting<Boolean> animateOffY = new Setting<>("animateOffY", false).addToGroup(animateOff);
    public final Setting<Boolean> animateOffZ = new Setting<>("animateOffZ", false).addToGroup(animateOff);
    public final Setting<Float> speedAnimateOff = new Setting<>("speedAnimateOff", 1f, 1f, 5f).addToGroup(rotationOff);
    }

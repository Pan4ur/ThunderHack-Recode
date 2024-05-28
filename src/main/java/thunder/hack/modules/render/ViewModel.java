package thunder.hack.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;

public class ViewModel extends Module {
    public ViewModel() {
        super("ViewModel", Category.RENDER);
    }

    public final Setting<Float> scale = new Setting<>("Scale", 1f, 0.1f, 1.5f);
    public final Setting<Float> positionMainX = new Setting<>("positionMainX", 0f, -3.0f, 3f);
    public final Setting<Float> positionMainY = new Setting<>("positionMainY", 0f, -3.0f, 3f);
    public final Setting<Float> positionMainZ = new Setting<>("positionMainZ", 0f, -3.0f, 3f);

    public final Setting<SettingGroup> mainHand = new Setting<>("MainHand", new SettingGroup(false, 0));
    public final Setting<SettingGroup> rotationMain = new Setting<>("Rotation", new SettingGroup(false, 1)).addToGroup(mainHand);
    public final Setting<Float> rotationMainX = new Setting<>("rotationMainX", 0f, -180.0f, 180f).addToGroup(rotationMain);
    public final Setting<Float> rotationMainY = new Setting<>("rotationMainY", 0f, -180.0f, 180f).addToGroup(rotationMain);
    public final Setting<Float> rotationMainZ = new Setting<>("rotationMainZ", 0f, -180.0f, 180f).addToGroup(rotationMain);
    public final Setting<SettingGroup> animateMain = new Setting<>("Animate", new SettingGroup(false, 1)).addToGroup(mainHand);
    public Setting<Boolean> animateMainX = new Setting<>("animateMainX", false).addToGroup(animateMain);
    public Setting<Boolean> animateMainY = new Setting<>("animateMainY", false).addToGroup(animateMain);
    public Setting<Boolean> animateMainZ = new Setting<>("animateMainZ", false).addToGroup(animateMain);
    public final Setting<Float> speedAnimateMain = new Setting<>("speedAnimateMain", 1f, 1f, 5f).addToGroup(rotationMain);

    public final Setting<SettingGroup> offHand = new Setting<>("OffHand", new SettingGroup(false, 0));
    public final Setting<SettingGroup> rotationOff = new Setting<>("RotationOff", new SettingGroup(false, 1)).addToGroup(offHand);
    public final Setting<Float> rotationOffX = new Setting<>("rotationOffX", 0f, -180.0f, 180f).addToGroup(rotationOff);
    public final Setting<Float> rotationOffY = new Setting<>("rotationOffY", 0f, -180.0f, 180f).addToGroup(rotationOff);
    public final Setting<Float> rotationOffZ = new Setting<>("rotationOffZ", 0f, -180.0f, 180f).addToGroup(rotationOff);
    public final Setting<SettingGroup> animateOff = new Setting<>("AnimateOff", new SettingGroup(false, 1)).addToGroup(offHand);
    public Setting<Boolean> animateOffX = new Setting<>("animateOffX", false).addToGroup(animateOff);
    public Setting<Boolean> animateOffY = new Setting<>("animateOffY", false).addToGroup(animateOff);
    public Setting<Boolean> animateOffZ = new Setting<>("animateOffZ", false).addToGroup(animateOff);
    public final Setting<Float> speedAnimateOff = new Setting<>("speedAnimateOff", 1f, 1f, 5f).addToGroup(rotationOff);
    public final Setting<SettingGroup> eatMod = new Setting<>("Eat", new SettingGroup(false, 0));
    public final Setting<Float> eatX = new Setting<>("EatX", 1f, -1f, 2f).addToGroup(eatMod);
    public final Setting<Float> eatY = new Setting<>("EatY", 1f, -1f, 2f).addToGroup(eatMod);

    private double changeRotate(double value, double speed) {
        return value - speed <= 180 && value - speed > -180 ? value - speed : 180;
    }

    @EventHandler
    private void onHeldItemRender(EventHeldItemRenderer event) {
        if (event.getHand() == Hand.MAIN_HAND) {
            if (animateMainX.getValue())
                rotationMainX.setValue((float) changeRotate(rotationMainX.getValue(), speedAnimateMain.getValue()));
            if (animateMainY.getValue())
                rotationMainY.setValue((float) changeRotate(rotationMainY.getValue(), speedAnimateMain.getValue()));
            if (animateMainZ.getValue())
                rotationMainZ.setValue((float) changeRotate(rotationMainZ.getValue(), speedAnimateMain.getValue()));
            event.getStack().translate(positionMainX.getValue(), positionMainY.getValue(), positionMainZ.getValue());
            event.getStack().scale(scale.getValue(), scale.getValue(), scale.getValue());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationMainX.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationMainY.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationMainZ.getValue()));
        } else {
            if (animateOffX.getValue())
                rotationOffX.setValue((float) changeRotate(rotationOffX.getValue(), speedAnimateOff.getValue()));
            if (animateOffY.getValue())
                rotationOffY.setValue((float) changeRotate(rotationOffY.getValue(), speedAnimateOff.getValue()));
            if (animateOffZ.getValue())
                rotationOffZ.setValue((float) changeRotate(rotationOffZ.getValue(), speedAnimateOff.getValue()));
            event.getStack().translate(-positionMainX.getValue(), positionMainY.getValue(), positionMainZ.getValue());
            event.getStack().scale(scale.getValue(), scale.getValue(), scale.getValue());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationOffX.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationOffY.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationOffZ.getValue()));
        }
    }
}

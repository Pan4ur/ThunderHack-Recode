package thunder.hack.features.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class ViewModel extends Module {
    public ViewModel() {
        super("ViewModel", Category.RENDER);
    }

    public final Setting<Boolean> syncHands = new Setting<>("SyncHands", true);

    public final Setting<SettingGroup> mainHand = new Setting<>("MainHand", new SettingGroup(false, 0));
    public final Setting<Float> positionMainX = new Setting<>("positionMainX", 0f, -3.0f, 3f).addToGroup(mainHand);
    public final Setting<Float> positionMainY = new Setting<>("positionMainY", 0f, -3.0f, 3f).addToGroup(mainHand);
    public final Setting<Float> positionMainZ = new Setting<>("positionMainZ", 0f, -3.0f, 3f).addToGroup(mainHand);
    public final Setting<Float> scaleMain = new Setting<>("ScaleMain", 1f, 0.1f, 1.5f).addToGroup(mainHand);

    public final Setting<SettingGroup> rotationMain = new Setting<>("Rotation", new SettingGroup(false, 1)).addToGroup(mainHand);
    public final Setting<Float> rotationMainX = new Setting<>("rotationMainX", 0f, -180.0f, 180f).addToGroup(rotationMain);
    public final Setting<Float> rotationMainY = new Setting<>("rotationMainY", 0f, -180.0f, 180f).addToGroup(rotationMain);
    public final Setting<Float> rotationMainZ = new Setting<>("rotationMainZ", 0f, -180.0f, 180f).addToGroup(rotationMain);
    public final Setting<SettingGroup> animateMain = new Setting<>("Animate", new SettingGroup(false, 1)).addToGroup(mainHand);
    public final Setting<Boolean> animateMainX = new Setting<>("animateMainX", false).addToGroup(animateMain);
    public final Setting<Boolean> animateMainY = new Setting<>("animateMainY", false).addToGroup(animateMain);
    public final Setting<Boolean> animateMainZ = new Setting<>("animateMainZ", false).addToGroup(animateMain);
    public final Setting<Float> speedAnimateMain = new Setting<>("speedAnimateMain", 1f, 1f, 5f).addToGroup(rotationMain);

    public final Setting<SettingGroup> offHand = new Setting<>("OffHand", new SettingGroup(false, 0));
    public final Setting<Float> positionOffX = new Setting<>("positionOffX", 0f, -3.0f, 3f).addToGroup(offHand);
    public final Setting<Float> positionOffY = new Setting<>("positionOffY", 0f, -3.0f, 3f).addToGroup(offHand);
    public final Setting<Float> positionOffZ = new Setting<>("positionOffZ", 0f, -3.0f, 3f).addToGroup(offHand);
    public final Setting<Float> scaleOff = new Setting<>("ScaleOff", 1f, 0.1f, 1.5f).addToGroup(offHand);

    public final Setting<SettingGroup> rotationOff = new Setting<>("RotationOff", new SettingGroup(false, 1)).addToGroup(offHand);
    public final Setting<Float> rotationOffX = new Setting<>("rotationOffX", 0f, -180.0f, 180f).addToGroup(rotationOff);
    public final Setting<Float> rotationOffY = new Setting<>("rotationOffY", 0f, -180.0f, 180f).addToGroup(rotationOff);
    public final Setting<Float> rotationOffZ = new Setting<>("rotationOffZ", 0f, -180.0f, 180f).addToGroup(rotationOff);
    public final Setting<SettingGroup> animateOff = new Setting<>("AnimateOff", new SettingGroup(false, 1)).addToGroup(offHand);
    public final Setting<Boolean> animateOffX = new Setting<>("animateOffX", false).addToGroup(animateOff);
    public final Setting<Boolean> animateOffY = new Setting<>("animateOffY", false).addToGroup(animateOff);
    public final Setting<Boolean> animateOffZ = new Setting<>("animateOffZ", false).addToGroup(animateOff);
    public final Setting<Float> speedAnimateOff = new Setting<>("speedAnimateOff", 1f, 1f, 5f).addToGroup(rotationOff);
    public final Setting<SettingGroup> eatMod = new Setting<>("Eat", new SettingGroup(false, 0));
    public final Setting<Float> eatX = new Setting<>("EatX", 1f, -1f, 2f).addToGroup(eatMod);
    public final Setting<Float> eatY = new Setting<>("EatY", 1f, -1f, 2f).addToGroup(eatMod);

    private float prevMainX, prevMainY, prevMainZ, prevOffX, prevOffY, prevOffZ;

    private float rotate(float value, float speed) {
        return value - speed <= 180 && value - speed > -180 ? value - speed : 180;
    }

    @EventHandler
    public void onSettingChange(EventSetting e) {
        if (!syncHands.getValue())
            return;

        if (e.getSetting() == positionMainX)
            positionOffX.setValueSilent(positionMainX.getValue());

        if (e.getSetting() == positionMainY)
            positionOffY.setValueSilent(positionMainY.getValue());

        if (e.getSetting() == positionMainZ)
            positionOffZ.setValueSilent(positionMainZ.getValue());

        if (e.getSetting() == positionOffX)
            positionMainX.setValueSilent(positionOffX.getValue());

        if (e.getSetting() == positionOffY)
            positionMainY.setValueSilent(positionOffY.getValue());

        if (e.getSetting() == positionOffZ)
            positionMainZ.setValueSilent(positionOffZ.getValue());

        if (e.getSetting() == scaleMain)
            scaleOff.setValueSilent(scaleMain.getValue());

        if (e.getSetting() == scaleOff)
            scaleMain.setValueSilent(scaleOff.getValue());
    }

    @Override
    public void onUpdate() {
        prevMainX = rotationMainX.getValue();
        prevMainY = rotationMainY.getValue();
        prevMainZ = rotationMainZ.getValue();
        prevOffX = rotationOffX.getValue();
        prevOffY = rotationOffY.getValue();
        prevOffZ = rotationOffZ.getValue();

        if (animateMainX.getValue())
            rotationMainX.setValue(rotate(rotationMainX.getValue(), speedAnimateMain.getValue()));

        if (animateMainY.getValue())
            rotationMainY.setValue(rotate(rotationMainY.getValue(), speedAnimateMain.getValue()));

        if (animateMainZ.getValue())
            rotationMainZ.setValue(rotate(rotationMainZ.getValue(), speedAnimateMain.getValue()));

        if (animateOffX.getValue())
            rotationOffX.setValue(rotate(rotationOffX.getValue(), speedAnimateOff.getValue()));

        if (animateOffY.getValue())
            rotationOffY.setValue(rotate(rotationOffY.getValue(), speedAnimateOff.getValue()));

        if (animateOffZ.getValue())
            rotationOffZ.setValue(rotate(rotationOffZ.getValue(), speedAnimateOff.getValue()));
    }

    @EventHandler
    private void onHeldItemRender(EventHeldItemRenderer event) {
        if (event.getHand() == Hand.MAIN_HAND) {
            event.getStack().translate(positionMainX.getValue(), positionMainY.getValue(), positionMainZ.getValue());
            event.getStack().scale(scaleMain.getValue(), scaleMain.getValue(), scaleMain.getValue());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(Render2DEngine.interpolateFloat(prevMainX, rotationMainX.getValue(), Render3DEngine.getTickDelta())));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(Render2DEngine.interpolateFloat(prevMainY, rotationMainY.getValue(), Render3DEngine.getTickDelta())));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(Render2DEngine.interpolateFloat(prevMainZ, rotationMainZ.getValue(), Render3DEngine.getTickDelta())));
        } else {
            event.getStack().translate(-positionOffX.getValue(), positionOffY.getValue(), positionOffZ.getValue());
            event.getStack().scale(scaleOff.getValue(), scaleOff.getValue(), scaleOff.getValue());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(Render2DEngine.interpolateFloat(prevOffX, rotationOffX.getValue(), Render3DEngine.getTickDelta())));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(Render2DEngine.interpolateFloat(prevOffY, rotationOffY.getValue(), Render3DEngine.getTickDelta())));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(Render2DEngine.interpolateFloat(prevOffZ, rotationOffZ.getValue(), Render3DEngine.getTickDelta())));
        }
    }
}

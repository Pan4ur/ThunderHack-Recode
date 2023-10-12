package dev.thunderhack.modules.render;

import dev.thunderhack.event.events.EventHeldItemRenderer;
import dev.thunderhack.mixins.accesors.IHeldItemRenderer;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.Parent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class ViewModel extends Module {
    public ViewModel() {
        super("ViewModel", Category.RENDER);
    }

    public Setting<Boolean> oldAnimationsM = new Setting<>("DisableSwapMain", false);
    public Setting<Boolean> oldAnimationsOff = new Setting<>("DisableSwapOff", false);

    public final Setting<Parent> mainHand = new Setting<>("MainHand", new Parent(false, 0));
    public final Setting<Parent> scaleMain = new Setting<>("Scale", new Parent(false, 1)).withParent(mainHand);
    public final Setting<Float> scaleMainX = new Setting<>("scaleMainX", 1f, 0.1f, 5f).withParent(scaleMain);
    public final Setting<Float> scaleMainY = new Setting<>("scaleMainY", 1f, 0.1f, 5f).withParent(scaleMain);
    public final Setting<Float> scaleMainZ = new Setting<>("scaleMainZ", 1f, 0.1f, 5f).withParent(scaleMain);
    public final Setting<Parent> positionMain = new Setting<>("Position", new Parent(false, 1)).withParent(mainHand);
    public final Setting<Float> positionMainX = new Setting<>("positionMainX", 0f, -3.0f, 3f).withParent(positionMain);
    public final Setting<Float> positionMainY = new Setting<>("positionMainY", 0f, -3.0f, 3f).withParent(positionMain);
    public final Setting<Float> positionMainZ = new Setting<>("positionMainZ", 0f, -3.0f, 3f).withParent(positionMain);
    public final Setting<Parent> rotationMain = new Setting<>("Rotation", new Parent(false, 1)).withParent(mainHand);
    public final Setting<Float> rotationMainX = new Setting<>("rotationMainX", 0f, -180.0f, 180f).withParent(rotationMain);
    public final Setting<Float> rotationMainY = new Setting<>("rotationMainY", 0f, -180.0f, 180f).withParent(rotationMain);
    public final Setting<Float> rotationMainZ = new Setting<>("rotationMainZ", 0f, -180.0f, 180f).withParent(rotationMain);
    public final Setting<Parent> animateMain = new Setting<>("Animate", new Parent(false, 1)).withParent(mainHand);
    public Setting<Boolean> animateMainX = new Setting<>("animateMainX", false).withParent(animateMain);
    public Setting<Boolean> animateMainY = new Setting<>("animateMainY", false).withParent(animateMain);
    public Setting<Boolean> animateMainZ = new Setting<>("animateMainZ", false).withParent(animateMain);
    public final Setting<Float> speedAnimateMain = new Setting<>("speedAnimateMain", 1f, 1f, 5f).withParent(rotationMain);

    public final Setting<Parent> offHand = new Setting<>("OffHand", new Parent(false, 0));
    public final Setting<Parent> scaleOff = new Setting<>("ScaleOff", new Parent(false, 1)).withParent(offHand);
    public final Setting<Float> scaleOffX = new Setting<>("scaleOffX", 1f, 0.1f, 5f).withParent(scaleOff);
    public final Setting<Float> scaleOffY = new Setting<>("scaleOffY", 1f, 0.1f, 5f).withParent(scaleOff);
    public final Setting<Float> scaleOffZ = new Setting<>("scaleOffZ", 1f, 0.1f, 5f).withParent(scaleOff);
    public final Setting<Parent> positionOff = new Setting<>("PositionOff", new Parent(false, 1)).withParent(offHand);
    public final Setting<Float> positionOffX = new Setting<>("positionOffX", 0f, -3.0f, 3f).withParent(positionOff);
    public final Setting<Float> positionOffY = new Setting<>("positionOffY", 0f, -3.0f, 3f).withParent(positionOff);
    public final Setting<Float> positionOffZ = new Setting<>("positionOffZ", 0f, -3.0f, 3f).withParent(positionOff);
    public final Setting<Parent> rotationOff = new Setting<>("RotationOff", new Parent(false, 1)).withParent(offHand);
    public final Setting<Float> rotationOffX = new Setting<>("rotationOffX", 0f, -180.0f, 180f).withParent(rotationOff);
    public final Setting<Float> rotationOffY = new Setting<>("rotationOffY", 0f, -180.0f, 180f).withParent(rotationOff);
    public final Setting<Float> rotationOffZ = new Setting<>("rotationOffZ", 0f, -180.0f, 180f).withParent(rotationOff);
    public final Setting<Parent> animateOff = new Setting<>("AnimateOff", new Parent(false, 1)).withParent(offHand);
    public Setting<Boolean> animateOffX = new Setting<>("animateOffX", false).withParent(animateOff);
    public Setting<Boolean> animateOffY = new Setting<>("animateOffY", false).withParent(animateOff);
    public Setting<Boolean> animateOffZ = new Setting<>("animateOffZ", false).withParent(animateOff);
    public final Setting<Float> speedAnimateOff = new Setting<>("speedAnimateOff", 1f, 1f, 5f).withParent(rotationOff);
    public static Setting<Boolean> slowAnimation = new Setting<>("SlowAnimation", true);
    public static Setting<Integer> slowAnimationVal = new Setting<>("SlowValue", 6, 1, 50);
    public final Setting<Parent> eatMod = new Setting<>("Eat", new Parent(false, 0));
    public final Setting<Float> eatX = new Setting<>("EatX", 1f, -1f, 2f).withParent(eatMod);
    public final Setting<Float> eatY = new Setting<>("EatY", 1f, -1f, 2f).withParent(eatMod);

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
            event.getStack().scale(scaleMainX.getValue(), scaleMainY.getValue(), scaleMainZ.getValue());
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
            event.getStack().translate(positionOffX.getValue(), positionOffY.getValue(), positionOffZ.getValue());
            event.getStack().scale(scaleOffX.getValue(), scaleOffY.getValue(), scaleOffZ.getValue());
            event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationOffX.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationOffY.getValue()));
            event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationOffZ.getValue()));
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;
        if (oldAnimationsM.getValue() && ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressMainHand() <= 1f) {
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(1f);
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(mc.player.getMainHandStack());
        }

        if (oldAnimationsOff.getValue() && ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressOffHand() <= 1f) {
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(1f);
            ((IHeldItemRenderer) mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(mc.player.getOffHandStack());
        }
    }
}

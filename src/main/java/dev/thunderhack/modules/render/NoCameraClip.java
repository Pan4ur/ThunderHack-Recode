package dev.thunderhack.modules.render;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.render.animation.BetterDynamicAnimation;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;

public class NoCameraClip extends Module {
    public NoCameraClip() {
        super("NoCameraClip", Category.RENDER);
    }

    public Setting<Boolean> antiFront = new Setting<>("AntiFront", false);
    public Setting<Float> distance = new Setting<>("Distance", 3f, 1f, 20f);
    private final BetterDynamicAnimation animation = new BetterDynamicAnimation();

    @Override
    public void onUpdate() {
        animation.update();
    }

    public void onRender3D(MatrixStack matrix) {
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) animation.setValue(0f);
        else animation.setValue(1f);

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && antiFront.getValue())
            mc.options.setPerspective(Perspective.FIRST_PERSON);
    }

    public double getDistance() {
        return 1d + ((distance.getValue() - 1d) * (float) animation.getAnimationD());
    }
}

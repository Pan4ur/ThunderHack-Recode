package thunder.hack.features.modules.render;

import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.animation.AnimationUtility;

public class NoCameraClip extends Module {
    public NoCameraClip() {
        super("NoCameraClip", Category.RENDER);
    }

    public Setting<Boolean> antiFront = new Setting<>("AntiFront", false);
    public Setting<Float> distance = new Setting<>("Distance", 3f, 1f, 20f);
    private float animation;

    public void onRender3D(MatrixStack matrix) {
        if (mc.options.getPerspective() == Perspective.FIRST_PERSON) animation = AnimationUtility.fast(animation, 0f, 10);
        else animation = AnimationUtility.fast(animation, 1f, 10);

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && antiFront.getValue())
            mc.options.setPerspective(Perspective.FIRST_PERSON);
    }

    public float getDistance() {
        return 1f + ((distance.getValue() - 1f) * animation);
    }
}

package thunder.hack.utility.render.animation;

import net.minecraft.util.math.MathHelper;

import static thunder.hack.utility.Util.mc;
import static thunder.hack.utility.render.animation.DynamicAnimation.createAnimation;

public class BetterDynamicAnimation {
    private final int maxTicks;
    private double value, dstValue;
    private int prevStep, step;

    public BetterDynamicAnimation(int maxTicks) {
        this.maxTicks = maxTicks;
    }

    public BetterDynamicAnimation() {
        this(5);
    }

    public void update() {
        prevStep = step;
        step = MathHelper.clamp(step + 1, 0, maxTicks);
    }

    public void setValue(double value) {
        if (value != this.dstValue) {
            this.prevStep = 0;
            this.step = 0;
            this.value = dstValue;
            this.dstValue = value;
        }
    }

    public double getAnimationD() {
        float pt = mc.getTickDelta();
        double delta = dstValue - value;
        double animation = createAnimation((prevStep + (step - prevStep) * pt) / (double) maxTicks);
        return value + delta * animation;
    }
}
package thunder.hack.utility.render.animation;

import net.minecraft.util.math.MathHelper;
import thunder.hack.utility.render.Render3DEngine;

public class EaseOutCirc {
    private final int maxTicks;
    private double value, dstValue;
    private int prevStep, step;

    public EaseOutCirc(int maxTicks) {
        this.maxTicks = maxTicks;
    }

    public EaseOutCirc() {
        this(5);
    }

    public void update() {
        prevStep = step;
        step = MathHelper.clamp(step + 1, 0, maxTicks);
    }

    public static double createAnimation(double value) {
        return Math.sqrt(1 - Math.pow(value - 1, 2));
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
        double delta = dstValue - value;
        double animation = createAnimation((prevStep + (step - prevStep) * Render3DEngine.getTickDelta()) / (double) maxTicks);
        return value + delta * animation;
    }
}
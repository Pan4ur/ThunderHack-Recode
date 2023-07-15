package thunder.hack.utility.render.animation;

import net.minecraft.util.math.MathHelper;

import static thunder.hack.utility.Util.mc;

public class DynamicAnimation {
    private final double speed;
    private double startValue;
    private double targetValue;
    private double outValue;
    private double step, prevStep;
    private double delta;

    public DynamicAnimation(double speed) {
        this.speed = 0.15f + speed;
    }

    public DynamicAnimation() {
        this(0);
    }

    public static double createAnimation(double value) {
        return Math.sqrt(1 - Math.pow(value - 1, 2));
    }

    public void update() {
        this.prevStep = step;
        this.step = MathHelper.clamp(this.step + speed, 0, 1);
        this.outValue = this.startValue + this.delta * createAnimation(this.step);
    }

    public double getValue() {
        return this.startValue + this.delta * createAnimation(
                this.prevStep + (this.step - this.prevStep) * mc.getTickDelta());
    }

    public void setValue(double value) {
        if (value == targetValue)
            return;
        this.targetValue = value;
        this.startValue = this.outValue;
        this.prevStep = 0;
        this.step = 0;
        this.delta = this.targetValue - this.startValue;
    }

}

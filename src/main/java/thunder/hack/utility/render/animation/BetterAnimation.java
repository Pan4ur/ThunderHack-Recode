package thunder.hack.utility.render.animation;

import net.minecraft.util.math.MathHelper;

import static thunder.hack.modules.Module.mc;


public class BetterAnimation {
    private int prevTick;
    private int tick;
    private final int maxTick;

    public BetterAnimation(int maxTick) {
        this.maxTick = maxTick;
    }

    public BetterAnimation() {
        this(10);
    }

    public static double dropAnimation(double value) {
        double c1 = 1.70158;
        double c3 = 2.70158;
        return 1 + c3 * Math.pow(value - 1, 3) + c1 * Math.pow(value - 1, 2);
    }

    public void update(boolean update) {
        prevTick = tick;
        tick = MathHelper.clamp(tick + (update ? 1 : -1), 0, maxTick);
    }

    public double getAnimationd() {
        return dropAnimation((this.prevTick + (this.tick - this.prevTick) * mc.getTickDelta()) / maxTick);
    }
}

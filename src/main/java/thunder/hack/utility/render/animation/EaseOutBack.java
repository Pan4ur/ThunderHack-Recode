package thunder.hack.utility.render.animation;

import net.minecraft.util.math.MathHelper;
import thunder.hack.utility.render.Render3DEngine;

public class EaseOutBack {
    private int prevTick;
    private int tick;
    private final int maxTick;

    public EaseOutBack(int maxTick) {
        this.maxTick = maxTick;
    }

    public EaseOutBack() {
        this(10);
    }

    public static double dropAnimation(double value) {
        return 1 + 2.70158 * Math.pow(value - 1, 3) + 1.70158 * Math.pow(value - 1, 2);
    }

    public void update(boolean update) {
        prevTick = tick;
        tick = MathHelper.clamp(tick + (update ? 1 : -1), 0, maxTick);
    }

    public double getAnimationd() {
        return dropAnimation((prevTick + (tick - prevTick) * Render3DEngine.getTickDelta()) / maxTick);
    }

    public void reset() {
        prevTick = 0;
        tick = 0;
    }
}

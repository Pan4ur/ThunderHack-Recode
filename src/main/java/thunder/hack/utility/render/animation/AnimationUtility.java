package thunder.hack.utility.render.animation;

import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.math.MathUtility;

public class AnimationUtility {
    public static float deltaTime() {
        return FrameRateCounter.INSTANCE.getFps() > 5 ? (1f / FrameRateCounter.INSTANCE.getFps()) : 0.016f;
    }

    public static float fast(float end, float start, float multiple) {
        float clampedDelta = MathUtility.clamp(deltaTime() * multiple, 0f, 1f);
        return (1f - clampedDelta) * end + clampedDelta * start;
    }
}

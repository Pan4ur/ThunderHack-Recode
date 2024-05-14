package thunder.hack.utility.render.animation;

import net.minecraft.client.MinecraftClient;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.math.MathUtility;

public class AnimationUtility {
    public static double deltaTime() {
        return MinecraftClient.getInstance().getCurrentFps() > 0 ? (1f / FrameRateCounter.INSTANCE.getFps()) : 0.016;
    }

    public static float fast(float end, float start, float multiple) {
        return (1 - MathUtility.clamp((float) (deltaTime() * multiple), 0, 1)) * end + MathUtility.clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }
}

package thunder.hack.events.impl;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.events.Event;

public class RenderBlurEvent extends Event {
    private float partialTicks;
    private MatrixStack matrixStack;

    public RenderBlurEvent(float partialTicks, MatrixStack matrixStack) {
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;

    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}

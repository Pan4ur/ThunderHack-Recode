package thunder.hack.events.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.events.Event;

public class RenderBlurEvent extends Event {
    private float partialTicks;
    private DrawContext context;

    public RenderBlurEvent(float partialTicks, DrawContext context) {
        this.partialTicks = partialTicks;
        this.context = context;

    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public DrawContext getDrawContext() {
        return context;
    }

    public MatrixStack getMatrixStack() {
        return context.getMatrices();
    }
}

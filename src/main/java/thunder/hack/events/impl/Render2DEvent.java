package thunder.hack.events.impl;


import net.minecraft.client.gui.DrawContext;
import thunder.hack.events.Event;
import net.minecraft.client.util.math.MatrixStack;

public class Render2DEvent extends Event {
    private  MatrixStack matrixStack;
    private  DrawContext context;

    public Render2DEvent(MatrixStack matrixStack, DrawContext context) {
        this.matrixStack = matrixStack;
        this.context = context;
    }

    public DrawContext getContext() {
        return context;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}



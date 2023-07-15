package thunder.hack.events.impl;


import thunder.hack.events.Event;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent extends Event {

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
    private final MatrixStack matrixStack;
    public Render3DEvent(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}


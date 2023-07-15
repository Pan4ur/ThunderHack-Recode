package thunder.hack.events.impl;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.events.Event;

public class PreRender3DEvent extends Event {

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
    private final MatrixStack matrixStack;
    public PreRender3DEvent(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}

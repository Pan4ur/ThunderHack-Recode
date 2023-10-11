package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.math.FrameRateCounter;

public class FpsCounter extends HudElement {
    public FpsCounter() {
        super("Fps", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), "FPS " + Formatting.WHITE + FrameRateCounter.INSTANCE.getFps(), getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }
}

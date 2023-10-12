package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.utils.math.FrameRateCounter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import dev.thunderhack.gui.font.FontRenderers;

public class FpsCounter extends HudElement {
    public FpsCounter() {
        super("Fps", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), "FPS " + Formatting.WHITE + FrameRateCounter.INSTANCE.getFps(), getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }
}

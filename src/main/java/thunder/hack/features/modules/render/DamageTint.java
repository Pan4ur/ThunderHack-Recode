package thunder.hack.features.modules.render;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class DamageTint extends Module {
    public DamageTint() {
        super("DamageTint", Category.RENDER);
    }

    public void onRender2D(DrawContext context) {
        float factor = 1f - MathUtility.clamp(mc.player.getHealth(), 0f, 12f) / 12f;
        Color red = new Color(0xFF0000, true);

        if (factor < 1f)
            Render2DEngine.draw2DGradientRect(context.getMatrices(), 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(),
                    Render2DEngine.injectAlpha(red, (int) (factor * 170f)), red,
                    Render2DEngine.injectAlpha(red, (int) (factor * 170f)), red
            );
    }
}

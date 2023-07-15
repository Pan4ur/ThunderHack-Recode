package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.MathHelper;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.math.FrameRateCounter;
import net.minecraft.util.Formatting;

public class FpsCounter extends HudElement {


    public FpsCounter() {
        super("Fps", "fps", 50,10);
    }

    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);
        FontRenderers.getRenderer2().drawString(e.getMatrixStack(),"FPS " + Formatting.WHITE + FrameRateCounter.INSTANCE.getFps(), getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);

    }
}

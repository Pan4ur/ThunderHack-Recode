package thunder.hack.gui.hud.impl;


import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import net.minecraft.util.Formatting;

public class TPSCounter extends HudElement {
    public TPSCounter() {
        super("TPS", "trps", 50,10);
    }

    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);
        String str = "TPS " + Formatting.WHITE + (Thunderhack.serverManager.getTPS());
       FontRenderers.getRenderer2().drawString(e.getMatrixStack(),str, getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }
}

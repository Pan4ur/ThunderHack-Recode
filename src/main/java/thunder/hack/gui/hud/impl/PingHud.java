package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Formatting;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;

import static thunder.hack.core.impl.ServerManager.getPing;

public class PingHud extends HudElement {

    public PingHud() {
        super("Ping", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), "Ping " + Formatting.WHITE + getPing(), getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }
}

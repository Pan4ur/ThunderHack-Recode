package thunder.hack.gui.hud.impl;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Formatting;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;

public class PingHud extends HudElement {

    public PingHud() {
        super("Ping", "PingHud", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        FontRenderers.getRenderer2().drawString(context.getMatrices(), "Ping " + Formatting.WHITE + getPing(), getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }

    public static int getPing() {
        if (mc.getNetworkHandler() == null) return 0;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }
}

package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Formatting;

public class PingHud extends HudElement {

    public PingHud() {
        super("Ping", "PingHud", 50,10);
    }

    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);
        FontRenderers.getRenderer2().drawString(e.getMatrixStack(),"Ping " + Formatting.WHITE + getPing(), getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }

    public static int getPing() {
        if (mc.getNetworkHandler() == null) return 0;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }
}

package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.HudEditor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Formatting;
import dev.thunderhack.gui.font.FontRenderers;

public class PingHud extends HudElement {

    public PingHud() {
        super("Ping", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), "Ping " + Formatting.WHITE + getPing(), getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }

    public static int getPing() {
        if (Module.mc.getNetworkHandler() == null) return 0;
        PlayerListEntry playerListEntry = Module.mc.getNetworkHandler().getPlayerListEntry(Module.mc.player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }
}

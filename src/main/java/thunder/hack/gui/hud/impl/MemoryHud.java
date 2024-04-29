package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;

public class MemoryHud extends HudElement {
    public MemoryHud() {
        super("MemoryHud", 100, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        long m = Runtime.getRuntime().maxMemory();
        long t = Runtime.getRuntime().totalMemory();
        long f = Runtime.getRuntime().freeMemory();
        long o = t - f;
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), "Mem: " + Formatting.WHITE + toMiB(o) + "/" + toMiB(m) + "MB [" + (o * 100L / m) + "%]", getPosX(), getPosY() + 3, HudEditor.getColor(1).getRGB());
    }

    private long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }
}

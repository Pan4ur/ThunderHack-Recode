package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;

public class TPSCounter extends HudElement {
    public TPSCounter() {
        super("TPS", 50, 10);
    }

    private final Setting<Boolean> extraTps = new Setting<>("ExtraTPS", true);

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        String str = "TPS " + Formatting.WHITE + ThunderHack.serverManager.getTPS() + (extraTps.getValue() ? " [" + ThunderHack.serverManager.getTPS2() + "]" : "");
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), str, getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }
}

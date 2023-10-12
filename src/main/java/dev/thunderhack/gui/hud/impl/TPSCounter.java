package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.ThunderHack;
import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import dev.thunderhack.gui.font.FontRenderers;

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

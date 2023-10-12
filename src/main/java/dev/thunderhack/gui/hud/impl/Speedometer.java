package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.ThunderHack;
import dev.thunderhack.gui.font.FontRenderers;
import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.math.MathUtility;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

public class Speedometer extends HudElement {
    public float speed = 0f;
    private final Setting<Boolean> bps = new Setting<>("BPS", false);

    public Speedometer() {
        super("Speedometer", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        String str = "Speed " + Formatting.WHITE;
        if (!bps.getValue()) {
            str += MathUtility.round(getSpeedKpH() * ThunderHack.TICK_TIMER) + " km/h";
        } else {
            str += MathUtility.round(getSpeedMpS() * ThunderHack.TICK_TIMER) + " b/s";
        }
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), str, getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), true);
    }

    public float getSpeedKpH() {
        return (float) (ThunderHack.playerManager.currentPlayerSpeed * 72f);
    }

    public float getSpeedMpS() {
        return (float) (ThunderHack.playerManager.currentPlayerSpeed * 20f);
    }
}
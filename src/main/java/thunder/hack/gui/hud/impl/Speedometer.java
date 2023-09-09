package thunder.hack.gui.hud.impl;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;


public class Speedometer extends HudElement {
    public float speed = 0f;
    private final Setting<Boolean> bps = new Setting<>("BPS", false);

    public Speedometer() {
        super("Speedometer", "Speedometer", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        String str = "";
        if (!bps.getValue()) {
            str = "Speed " + Formatting.WHITE + MathUtility.round(getSpeedKpH() * ThunderHack.TICK_TIMER) + " km/h";
        } else {
            str = "Speed " + Formatting.WHITE + MathUtility.round(getSpeedMpS() * ThunderHack.TICK_TIMER) + " b/s";
        }
        FontRenderers.getRenderer2().drawString(context.getMatrices(), str, getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), true);
    }

    @EventHandler
    public void updateValues(EventSync e) {
        float deltaX = (float) (mc.player.getX() - mc.player.prevX);
        float deltaZ = (float) (mc.player.getZ() - mc.player.prevZ);
        speed = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    public float getSpeedKpH() {
        return speed * 72f;
    }

    public float getSpeedMpS() {
        return speed * 20f;
    }
}
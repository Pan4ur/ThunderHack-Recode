package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class Speedometer extends HudElement {
    public float speed = 0f;
    private final Setting<Boolean> bps = new Setting<>("BPS", false);
    private final Setting<Boolean> average = new Setting<>("Average", false);

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

        float pX = getPosX() > mc.getWindow().getScaledWidth() / 2f ? getPosX() - FontRenderers.getModulesRenderer().getStringWidth(str) : getPosX();

        if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRoundedBlur(context.getMatrices(), pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(str) + 21, 13f, 3, HudEditor.blurColor.getValue().getColorObject());
            Render2DEngine.drawRect(context.getMatrices(), pX + 14, getPosY() + 2, 0.5f, 8, new Color(0x44FFFFFF, true));

            Render2DEngine.setupRender();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.setShaderTexture(0, TextureStorage.speedometerIcon);
            Render2DEngine.renderGradientTexture(context.getMatrices(), pX + 2, getPosY() + 1, 10, 10, 0, 0, 512, 512, 512, 512,
                    HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.endRender();
        }

        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), str, pX + 18, getPosY() + 5, HudEditor.getColor(1).getRGB());
        setBounds(pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(str) + 21, 13f);
    }

    public float getSpeedKpH() {
        return (average.getValue() ? Managers.PLAYER.averagePlayerSpeed : Managers.PLAYER.currentPlayerSpeed) * 72f;
    }

    public float getSpeedMpS() {
        return (average.getValue() ? Managers.PLAYER.averagePlayerSpeed : Managers.PLAYER.currentPlayerSpeed) * 20f;
    }
}
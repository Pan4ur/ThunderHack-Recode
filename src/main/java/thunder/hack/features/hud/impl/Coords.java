package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class Coords extends HudElement {
    public Coords() {
        super("Coords", 100, 10);
    }

    private final Setting<NetherCoords> netherCoords = new Setting<>("NetherCoords", NetherCoords.On);

    private enum NetherCoords {
        Off, On, OnlyNether
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        int posX = (int) mc.player.getX();
        int posY = (int) mc.player.getY();
        int posZ = (int) mc.player.getZ();

        float nether = !PlayerUtility.isInHell() ? 0.125F : 8.0F;

        int hposX = (int) (mc.player.getX() * nether);
        int hposZ = (int) (mc.player.getZ() * nether);

        String coordinates = "XYZ " + Formatting.WHITE +
                (posX + " " + posY + " " + posZ + Formatting.WHITE + (netherCoords.is(NetherCoords.On) || (netherCoords.is(NetherCoords.OnlyNether) && !PlayerUtility.isInHell()) ? " [" + Formatting.RESET + hposX + " " + hposZ + Formatting.WHITE + "]" : ""));

        float pX = getPosX() > mc.getWindow().getScaledWidth() / 2f ? getPosX() - FontRenderers.getModulesRenderer().getStringWidth(coordinates) : getPosX();

        if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRoundedBlur(context.getMatrices(), pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(coordinates) + 21, 13f, 3, HudEditor.blurColor.getValue().getColorObject());
            Render2DEngine.drawRect(context.getMatrices(), pX + 14, getPosY() + 2, 0.5f, 8, new Color(0x44FFFFFF, true));

            Render2DEngine.setupRender();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.setShaderTexture(0, TextureStorage.coordsIcon);
            Render2DEngine.renderGradientTexture(context.getMatrices(), pX + 2, getPosY() + 1, 10, 10, 0, 0, 512, 512, 512, 512,
                    HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.endRender();
        }

        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), coordinates, pX + 18, getPosY() + 5, HudEditor.getColor(1).getRGB());
        setBounds(pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(coordinates) + 21, 13f);
    }
}
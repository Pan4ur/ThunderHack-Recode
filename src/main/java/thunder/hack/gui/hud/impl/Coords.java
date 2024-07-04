package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.Objects;

import static thunder.hack.utility.render.HUDUtils.drawCoordinatesBackground;
import static thunder.hack.utility.render.HUDUtils.drawIcon;

public class Coords extends HudElement {
    public Coords() {
        super("Coords", 100, 10);
    }

    private final Identifier icon = Identifier.of("thunderhack", "textures/hud/icons/coords.png");
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
            drawCoordinatesBackground(context, pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(coordinates) + 21, 13f, HudEditor.blurColor.getValue().getColorObject());
            drawIcon(context, pX + 2, getPosY() + 1, 10, 10, icon);
        }

        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), coordinates, pX + 18, getPosY() + 5, HudEditor.getColor(1).getRGB());
        setBounds(pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(coordinates) + 21, 13f);
    }
}
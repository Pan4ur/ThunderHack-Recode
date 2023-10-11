package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;

import java.util.Objects;

public class Coords extends HudElement {
    public Coords() {
        super("Coords", 100, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        int posX = (int) mc.player.getX();
        int posY = (int) mc.player.getY();
        int posZ = (int) mc.player.getZ();
        float nether = !isHell() ? 0.125F : 8.0F;
        int hposX = (int) (mc.player.getX() * nether);
        int hposZ = (int) (mc.player.getZ() * nether);
        String coordinates = "XYZ " + Formatting.RESET + (isHell() ? (posX + ", " + posY + ", " + posZ + Formatting.WHITE + " [" + Formatting.RESET + hposX + ", " + hposZ + Formatting.WHITE + "]" + Formatting.RESET) : (posX + ", " + posY + ", " + posZ + Formatting.WHITE + " [" + Formatting.RESET + hposX + ", " + hposZ + Formatting.WHITE + "]"));
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), coordinates, getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }

    public boolean isHell() {
        if (mc.world == null) return false;
        return Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "the_nether");
    }
}
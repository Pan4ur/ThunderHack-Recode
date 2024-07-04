package thunder.hack.utility.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import thunder.hack.modules.client.HudEditor;

import java.awt.*;

public class HUDUtils {
    public static void setupBlend() {
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
    }

    public static void bindTexture(Identifier icon) {
        RenderSystem.setShaderTexture(0, icon);
    }

    public static void drawIcon(DrawContext context, float x, float y, int width, int height, Identifier icon) {
        setupBlend();
        bindTexture(icon);
        Render2DEngine.renderGradientTexture(context.getMatrices(), x, y, width, height, 0, 0, 512, 512, 512, 512,
                HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
        Render2DEngine.endRender();
    }

    public static void drawCoordinatesBackground(DrawContext context, float x, float y, float width, float height, Color color) {
        Render2DEngine.drawRoundedBlur(context.getMatrices(), x, y, width, height, 3, color);
        Render2DEngine.drawRect(context.getMatrices(), x + 14, y + 2, 0.5f, 8, new Color(0x44FFFFFF, true));
    }
}

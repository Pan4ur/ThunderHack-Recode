package thunder.hack.gui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import thunder.hack.core.ShaderManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.math.FrameRateCounter;

import java.awt.*;

public class FpsCounter extends HudElement {


    public FpsCounter() {
        super("Fps", "fps", 50, 10);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        FontRenderers.getRenderer2().drawString(context.getMatrices(), "FPS " + Formatting.WHITE + FrameRateCounter.INSTANCE.getFps(), getPosX(), getPosY(), HudEditor.getColor(1).getRGB(), false);
    }
}

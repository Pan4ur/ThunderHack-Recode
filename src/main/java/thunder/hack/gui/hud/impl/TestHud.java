package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.events.impl.RenderBlurEvent;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class TestHud extends HudElement {


    public TestHud() {
        super("TestHud", "TestHud", 100, 100);

    }


    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);

    }

    @Subscribe
    public void onRenderBlur(RenderBlurEvent e) {
       // Render2DEngine.initiateBlur(e.getMatrixStack(),10, (int) (getPosX()), (int) getPosY() + 80,120,120);
        // Render2DEngine.drawBlur(e.getMatrixStack(),10, (int) (getPosX()), (int) getPosY() + 80,120,120);

       //  Render2DEngine.drawBlur( 9, e.getMatrixStack(), () -> Render2DEngine.drawRound(e.getMatrixStack(), (int) (getPosX()), (int) getPosY() + 80,120,120,8,new Color(0x7900FFA2, true)));
          //  Render2DEngine.drawNewGlow(e.getMatrixStack(),100,100,100,100,8,8,Color.GREEN,Color.GREEN,Color.RED,Color.RED);


    }


}

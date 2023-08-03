package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.events.impl.RenderBlurEvent;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

public class TestHud extends HudElement {


    public TestHud() {
        super("TestHud", "TestHud", 100, 100);

    }

    private final Setting<Float> r = new Setting<>("r", 15f, 1f, 360f);
    private final Setting<Float> s = new Setting<>("s", 15f, 1f, 360f);



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

        Render2DEngine.drawGradientGlow(e.getMatrixStack(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90),getPosX(),getPosY(),100,100,r.getValue(),s.getValue());


    }


}

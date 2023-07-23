package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.events.impl.RenderBlurEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.movement.Timer;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.BetterDynamicAnimation;

import java.awt.*;

public class TimerIndicator extends HudElement {
    public TimerIndicator() {
        super("TimerIndicator", "TimerIndicator", 65,15);
    }

    public static BetterDynamicAnimation timerAnimation = new BetterDynamicAnimation();


    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);
        int status;
        float f4 = 100 / Thunderhack.moduleManager.get(Timer.class).speed.getValue();
        float f5 = Math.min(Timer.violation, f4);
        status = (int) (((f4 - f5) / f4) * 100);
        status = MathUtil.clamp(status, 0, 100);
        FontRenderers.sf_bold_mini.drawCenteredString(e.getMatrixStack(),status >= 99 ? "100%" : status + "%", getPosX() + 31, getPosY() + 2, new Color(200, 200, 200, 255).getRGB());
    }

    @Subscribe
    public void onRenderShader(RenderBlurEvent e){

        Render2DEngine.drawBlurredShadow(e.getMatrixStack(),getPosX(), getPosY(), 62, 12, 6, HudEditor.getColor(270));

        int status;
        float timerStatus;


        float f4 = 100 / Thunderhack.moduleManager.get(Timer.class).speed.getValue();
        float f5 = Math.min(Timer.violation, f4);
        timerAnimation.setValue(((f4 - f5) / f4) * 61);
        timerStatus = (float) timerAnimation.getAnimationD();
        status = (int) (((f4 - f5) / f4) * 100);
        status = MathUtil.clamp(status, 0, 100);
        timerStatus = MathUtil.clamp(timerStatus, 5, 61);



        Render2DEngine.drawRoundShader(e.getMatrixStack(),getPosX(), getPosY(), 62, 12, HudEditor.hudRound.getValue() - 3f, new Color(1));
        Render2DEngine.drawGradientRoundShader( e.getMatrixStack(),HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180),  HudEditor.getColor(90),getPosX() + 0.5f, getPosY() + 0.5f, (int) timerStatus, 11, HudEditor.hudRound.getValue() - 3f);
        FontRenderers.sf_bold_mini.drawCenteredString(e.getMatrixStack(),status >= 99 ? "100%" : status + "%", getPosX() + 31, getPosY() + 2, new Color(200, 200, 200, 255).getRGB());

    }

    @Override
    public void onUpdate(){
        timerAnimation.update();
    }
}

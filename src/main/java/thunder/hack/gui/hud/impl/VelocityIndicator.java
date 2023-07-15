package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.player.Velocity;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.BetterDynamicAnimation;

import java.awt.*;

public class VelocityIndicator extends HudElement {
    public VelocityIndicator() {
        super("VelocityIndicator", "VelocityIndicator", 100,100);
    }

    public static BetterDynamicAnimation timerAnimation = new BetterDynamicAnimation();


    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);
        Render2DEngine.drawBlurredShadow(e.getMatrixStack(),getPosX() - 32, getPosY(), 64, 12, 10, HudEditor.getColor(270));
        float timerStatus = Velocity.flags / 90f;
        timerAnimation.setValue(timerStatus);
        timerStatus = (float) timerAnimation.getAnimationD();
        int status = (int) (timerStatus * 100);
        status = MathUtil.clamp(status,0,100);
        timerStatus = MathUtil.clamp(timerStatus * 61,5,61);
        Render2DEngine.drawRound(e.getMatrixStack(),getPosX() - 31f, getPosY(), 62, 12, HudEditor.hudRound.getValue() - 3, new Color(1));
        Render2DEngine.renderRoundedGradientRect( e.getMatrixStack(),HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180),  HudEditor.getColor(90),getPosX() - 30.5f, getPosY() + 0.5f, (int) timerStatus, 11, HudEditor.hudRound.getValue() - 3f);
        FontRenderers.sf_bold_mini.drawCenteredString( e.getMatrixStack(),"kick chance: " + (status >= 99 ? "100%" : status + "%"), getPosX(), getPosY() + 2, new Color(200, 200, 200, 255).getRGB());
    }

    @Override
    public void onUpdate(){
        timerAnimation.update();
    }
}

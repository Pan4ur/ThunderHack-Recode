package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.ShaderManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.movement.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.BetterDynamicAnimation;

import java.awt.*;

public class TimerIndicator extends HudElement {
    private final BetterDynamicAnimation timerAnimation = new BetterDynamicAnimation();

    public TimerIndicator() {
        super("TimerIndicator", "TimerIndicator", 65, 15);
    }

    @Override
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        int status;
        float f4 = 100 / Timer.speed.getValue();
        float f5 = Math.min(Timer.violation, f4);
        status = (int) (((f4 - f5) / f4) * 100);
        status = MathUtility.clamp(status, 0, 100);
        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), status >= 99 ? "100%" : status + "%", getPosX() + 31, getPosY() + 2, new Color(200, 200, 200, 255).getRGB());
    }

    @Override
    public void onRenderShaders(@NotNull DrawContext context) {
        Render2DEngine.drawGradientGlow(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() + 0.5f, getPosY() + 0.5f, 61, 11, 3, 10);

        int status;
        float timerStatus;


        float f4 = 100 / Timer.speed.getValue();
        float f5 = Math.min(Timer.violation, f4);
        timerAnimation.setValue(((f4 - f5) / f4) * 61);
        timerStatus = (float) timerAnimation.getAnimationD();
        status = (int) (((f4 - f5) / f4) * 100);
        status = MathUtility.clamp(status, 0, 100);
        timerStatus = MathUtility.clamp(timerStatus, 5, 61);


        Render2DEngine.drawRoundShader(context.getMatrices(), getPosX(), getPosY(), 62, 12, HudEditor.hudRound.getValue() - 3f, new Color(1));
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() + 0.5f, getPosY() + 0.5f, (int) timerStatus, 11, HudEditor.hudRound.getValue() - 3f);
        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), status >= 99 ? "100%" : status + "%", getPosX() + 31, getPosY() + 2, new Color(200, 200, 200, 255).getRGB());
    }

    @Override
    public void onUpdate() {
        timerAnimation.update();
    }
}

package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
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
        super("TimerIndicator", 60, 10);
    }

    @Override
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        float f4 = 100 / Timer.speed.getValue();
        float f5 = Math.min(Timer.violation, f4);
        timerAnimation.setValue(((f4 - f5) / f4) * 58);
        int status = (int) (((f4 - f5) / f4) * 100);
        status = MathUtility.clamp(status, 0, 100);
        Render2DEngine.drawGradientBlurredShadow(context.getMatrices(), getPosX() - 1, getPosY() - 1, 62, 12,6, HudEditor.getColor(90), HudEditor.getColor(180), HudEditor.getColor(0), HudEditor.getColor(270));
        Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY(), 60, 10, new Color(0x9E000000, true));
        Render2DEngine.draw2DGradientRect(context.getMatrices(), getPosX(), getPosY(), getPosX() + 60 * ((float) status / 100f), getPosY() + 10, HudEditor.getColor(90), HudEditor.getColor(180), HudEditor.getColor(0), HudEditor.getColor(270));
        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() + 20, getPosY(), 22, 10,6, new Color(0x47000000, true));
        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), status >= 99 ? "100%" : status + "%", getPosX() + 31, getPosY() + 3.5f, new Color(200, 200, 200, 255).getRGB());
    }

    @Override
    public void onUpdate() {
        timerAnimation.update();
    }
}

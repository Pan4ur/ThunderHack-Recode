package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.movement.Timer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.EaseOutCirc;

import java.awt.*;

public class TimerIndicator extends HudElement {
    private final EaseOutCirc timerAnimation = new EaseOutCirc();

    public TimerIndicator() {
        super("TimerIndicator", 60, 10);
    }

    @Override
    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRoundedBlur(context.getMatrices(), getPosX(), getPosY(), 65, 15f, 3, HudEditor.blurColor.getValue().getColorObject());
            Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY(), 65 * Timer.energy, 15f, 3f, 0.4f);
            FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), Timer.energy >= 0.99f ? "100%" : (int) Math.ceil(Timer.energy * 100) + "%", getPosX() + 32, getPosY() + 5.5f, new Color(200, 200, 200, 255).getRGB());
            setBounds(getPosX(), getPosY(), 65, 15);
        } else {
            Render2DEngine.drawGradientBlurredShadow(context.getMatrices(), getPosX() - 1, getPosY() - 1, 62, 12, 6, HudEditor.getColor(90), HudEditor.getColor(180), HudEditor.getColor(0), HudEditor.getColor(270));
            Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY(), 60, 10, new Color(0x9E000000, true));
            Render2DEngine.draw2DGradientRect(context.getMatrices(), getPosX(), getPosY(), getPosX() + 60 * Timer.energy, getPosY() + 10, HudEditor.getColor(90), HudEditor.getColor(180), HudEditor.getColor(0), HudEditor.getColor(270));
            Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() + 20, getPosY(), 22, 10, 6, new Color(0x47000000, true));
            FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), Timer.energy >= 0.99f ? "100%" : (int) Math.ceil(Timer.energy * 100) + "%", getPosX() + 31, getPosY() + 3.5f, new Color(200, 200, 200, 255).getRGB());
            setBounds(getPosX(), getPosY(), 60, 10);
        }
    }

    @Override
    public void onUpdate() {
        timerAnimation.update();
    }
}

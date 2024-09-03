package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

public class Cooldowns extends HudElement {
    public Cooldowns() {
        super("Cooldowns", 100, 100);
    }

    private float animation1, animation2;

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), 100, 40, HudEditor.hudRound.getValue());

        animation1 = AnimationUtility.fast(animation1,mc.player.getAttackCooldownProgress(0.5f), 50f);
        animation2 = AnimationUtility.fast(animation2,(1f - ((float) mc.player.hurtTime / 10f)), 50f);

        Render2DEngine.drawGradientRound(context.getMatrices(),
                getPosX() + 30.f, getPosY() + 20.f, 65, 5, 1.5f , HudEditor.getColor(90).darker().darker().darker(), HudEditor.getColor(180).darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker(), HudEditor.getColor(270).darker().darker().darker());
        Render2DEngine.drawGradientRound(context.getMatrices(),
                getPosX() + 30.f, getPosY() + 30.f, 65, 5, 1.5f, HudEditor.getColor(90).darker().darker().darker(), HudEditor.getColor(180).darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker(), HudEditor.getColor(270).darker().darker().darker());

        Render2DEngine.drawRect(context.getMatrices(),
                getPosX() + 30.f, getPosY() + 20.f, (65 * animation1), 5, 1.5f,1f);

        Render2DEngine.drawRect(context.getMatrices(),
                getPosX() + 30.f, getPosY() + 30.f, (65 * animation2), 5, 1.5f,1f);

        if(HudEditor.hudStyle.is(HudEditor.HudStyle.Glowing)) {
            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Cooldowns", getPosX() + 50, getPosY() + 4, HudEditor.textColor.getValue().getColorObject());
        } else {
            FontRenderers.sf_bold.drawGradientCenteredString(context.getMatrices(), "Cooldowns", getPosX() + 50, getPosY() + 4, 10);
        }

        if(HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRectDumbWay(context.getMatrices(), getPosX() + 4, getPosY() + 13, getPosX() + getWidth() - 4, getPosY() + 13.5f, new Color(0x54FFFFFF, true));
        } else {
            Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + 100 / 2f - 2, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
            Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + 100 / 2f - 2, getPosY() + 13.7f, getPosX() + 2 + 100 - 4, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
        }

        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Attack", getPosX() + 5, getPosY() + 20, HudEditor.textColor.getValue().getColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Hurt", getPosX() + 5, getPosY() + 30, HudEditor.textColor.getValue().getColor());

        setBounds(getPosX(), getPosY(), 100, 40);
    }
}

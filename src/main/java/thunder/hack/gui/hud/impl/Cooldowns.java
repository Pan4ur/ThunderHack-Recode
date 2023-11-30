package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;
import thunder.hack.utility.render.animation.BetterDynamicAnimation;

public class Cooldowns extends HudElement {
    public Cooldowns() {
        super("Cooldowns", 100, 100);
    }

    private float animation1, animation2;

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Cooldowns", getPosX() + 100 / 2, getPosY() + 2, -1);
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + 100 / 2f - 2, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + 100 / 2f - 2, getPosY() + 13.7f, getPosX() + 2 + 100 - 4, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Attack", getPosX() + 5, getPosY() + 20, HudEditor.textColor.getValue().getColor());
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "Hurt", getPosX() + 5, getPosY() + 30, HudEditor.textColor.getValue().getColor());

    }

    public void onRenderShaders(DrawContext context) {
        Render2DEngine.drawGradientGlow(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), 100, 40, HudEditor.hudRound.getValue(), 10);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() - 0.5f, getPosY() - 0.5f, 100 + 1, 1 + 40, HudEditor.hudRound.getValue());
        Render2DEngine.drawRoundShader(context.getMatrices(), getPosX(), getPosY(), 100, 40, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());


        animation1 = AnimationUtility.fast(animation1,mc.player.getAttackCooldownProgress(0.5f), 50f);
        animation2 = AnimationUtility.fast(animation2,(1f - ((float) mc.player.hurtTime / 10f)), 50f);

        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270).darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker(), HudEditor.getColor(180).darker().darker().darker(), HudEditor.getColor(90).darker().darker().darker(),
                getPosX() + 30.f, getPosY() + 20.f, 65, 5, 1.5f);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270).darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker(), HudEditor.getColor(180).darker().darker().darker(), HudEditor.getColor(90).darker().darker().darker(),
                getPosX() + 30.f, getPosY() + 30.f, 65, 5, 1.5f);

        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90),
                getPosX() + 30.f, getPosY() + 20.f, 65 * animation1, 5, 1.5f);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90),
                getPosX() + 30.f, getPosY() + 30.f, 65 * animation2, 5, 1.5f);

    }
}

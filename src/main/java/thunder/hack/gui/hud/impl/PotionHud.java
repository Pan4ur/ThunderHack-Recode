package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.util.ArrayList;

public class PotionHud extends HudElement {
    public PotionHud() {
        super("Potions", 100, 100);
    }

    private float vAnimation, hAnimation;

    public static String getDuration(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "*:*";
        } else {
            int var1 = pe.getDuration();
            int mins = var1 / 1200;
            int sec = (var1 % 1200) / 20;

            return mins + ":" + sec;
        }
    }

    public void onRenderShaders(DrawContext context) {
        int y_offset1 = 0;
        float max_width = 40;

        ArrayList<StatusEffectInstance> effects = new ArrayList<>();

        for (StatusEffectInstance potionEffect : mc.player.getStatusEffects()) {
            if (potionEffect.getDuration() != 0) {
                effects.add(potionEffect);

                y_offset1 += 10;
                StatusEffect potion = potionEffect.getEffectType();
                String power = "";
                switch (potionEffect.getAmplifier()) {
                    case 0 -> power = "I";
                    case 1 -> power = "II";
                    case 2 -> power = "III";
                    case 3 -> power = "IV";
                    case 4 -> power = "V";
                }

                String s = potion.getName().getString() + " " + power;
                String s2 = getDuration(potionEffect) + "";

                float a = FontRenderers.sf_bold_mini.getStringWidth(s + "  " + s2) * 1.2f;
                if (a > max_width) {
                    max_width = a;
                }
            }
        }

        vAnimation = AnimationUtility.fast(vAnimation, 20 + y_offset1, 15);
        hAnimation = AnimationUtility.fast(hAnimation, max_width, 15);

        Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), hAnimation, vAnimation, HudEditor.hudRound.getValue());
        setBounds((int) max_width, 20 + y_offset1);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        int y_offset = 0;
        float max_width = 40;

        ArrayList<StatusEffectInstance> effects = new ArrayList<>();

        for (StatusEffectInstance potionEffect : mc.player.getStatusEffects()) {
            if (potionEffect.getDuration() != 0) {
                effects.add(potionEffect);
                StatusEffect potion = potionEffect.getEffectType();
                String power = "";
                switch (potionEffect.getAmplifier()) {
                    case 0 -> power = "I";
                    case 1 -> power = "II";
                    case 2 -> power = "III";
                    case 3 -> power = "IV";
                    case 4 -> power = "V";
                }
                String s = potion.getName().getString() + " " + power;
                String s2 = getDuration(potionEffect) + "";

                float a = FontRenderers.sf_bold_mini.getStringWidth(s + "  " + s2) * 1.2f;
                if (a > max_width)
                    max_width = a;
            }
        }

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Potions", getPosX() + max_width / 2, getPosY() + 2, HudEditor.textColor.getValue().getColor());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + hAnimation / 2, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + hAnimation / 2, getPosY() + 13.7f, getPosX() + hAnimation - 2, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        Render2DEngine.addWindow(context.getMatrices(), getPosX(), getPosY(), getPosX() + hAnimation, getPosY() + vAnimation, 1f);
        for (StatusEffectInstance potionEffect : effects) {
            StatusEffect potion = potionEffect.getEffectType();
            String power = "";
            switch (potionEffect.getAmplifier()) {
                case 0 -> power = "I";
                case 1 -> power = "II";
                case 2 -> power = "III";
                case 3 -> power = "IV";
                case 4 -> power = "V";
            }

            String s = potion.getName().getString() + " " + power;
            String s2 = getDuration(potionEffect) + "";

            FontRenderers.sf_bold_mini.drawString(context.getMatrices(), s + "  " + s2, getPosX() + 5, getPosY() + 20 + y_offset, HudEditor.textColor.getValue().getColor(), false);
            y_offset += 10;
        }
        Render2DEngine.popWindow();
    }
}
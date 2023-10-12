package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import dev.thunderhack.gui.font.FontRenderers;

import java.util.ArrayList;

public class PotionHud extends HudElement {
    public PotionHud() {
        super("Potions", 100, 100);
    }

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

        for (StatusEffectInstance potionEffect : Module.mc.player.getStatusEffects()) {
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

        //Render2DEngine.drawGradientBlurredShadow(e.getMatrixStack(), getPosX() + 1, getPosY() + 1, max_width - 2, 18 + y_offset1, 10, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));

        Render2DEngine.drawGradientGlow(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), max_width, 20 + y_offset1, HudEditor.hudRound.getValue(), 10);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() - 0.5f, getPosY() - 0.5f, max_width + 1, 21 + y_offset1, HudEditor.hudRound.getValue());
        Render2DEngine.drawRoundShader(context.getMatrices(), getPosX(), getPosY(), max_width, 20 + y_offset1, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());

        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + max_width / 2, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + max_width / 2, getPosY() + 13.7f, getPosX() + max_width - 2, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        int y_offset = 0;
        float max_width = 40;

        ArrayList<StatusEffectInstance> effects = new ArrayList<>();

        for (StatusEffectInstance potionEffect : Module.mc.player.getStatusEffects()) {
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
                if (a > max_width) {
                    max_width = a;
                }
            }
        }

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Potions", getPosX() + max_width / 2, getPosY() + 3, HudEditor.textColor.getValue().getColor());

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
    }
}
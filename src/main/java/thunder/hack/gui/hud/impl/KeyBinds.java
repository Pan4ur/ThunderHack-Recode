package thunder.hack.gui.hud.impl;


import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.Thunderhack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.util.Objects;

public class KeyBinds extends HudElement {
    public final Setting<ColorSetting> oncolor = new Setting<>("OnColor", new ColorSetting(0xBEBEBE));
    public final Setting<ColorSetting> offcolor = new Setting<>("OffColor", new ColorSetting(0x646464));
    public final Setting<Boolean> onlyEnabled = new Setting<>("OnlyEnabled", false);

    public KeyBinds() {
        super("KeyBinds", "KeyBinds", 100, 100);
    }


    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        float max_width = 40;
        for (Module feature : Thunderhack.moduleManager.modules) {
            if (feature.isDisabled() && onlyEnabled.getValue()) continue;
            if (!Objects.equals(feature.getBind().getBind(), "None") && !feature.getName().equalsIgnoreCase("clickgui") && !feature.getName().equalsIgnoreCase("thundergui")) {
                String sbind = feature.getBind().getBind();
                if (sbind.equals("LEFT_CONTROL")) {
                    sbind = "LCtrl";
                }
                if (sbind.equals("RIGHT_CONTROL")) {
                    sbind = "RCtrl";
                }
                if (sbind.equals("LEFT_SHIFT")) {
                    sbind = "LShift";
                }
                if (sbind.equals("RIGHT_SHIFT")) {
                    sbind = "RShift";
                }
                if (sbind.equals("LEFT_ALT")) {
                    sbind = "LAlt";
                }
                if (sbind.equals("RIGHT_ALT")) {
                    sbind = "RAlt";
                }
                float a = FontRenderers.sf_bold_mini.getStringWidth("[" + sbind + "]  " + feature.getName()) * 1.2f;
                if (a > max_width) {
                    max_width = a;
                }
            }
        }
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "KeyBinds", getPosX() + max_width / 2, getPosY() + 3, HudEditor.textColor.getValue().getColor());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7, getPosX() + 2 + max_width / 2f - 2, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0).getRGB(), HudEditor.textColor.getValue().getColorObject().getRGB());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + max_width / 2f - 2, getPosY() + 13.7, getPosX() + 2 + max_width - 4, getPosY() + 14, HudEditor.textColor.getValue().getColorObject().getRGB(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0).getRGB());

        int y_offset = 2;
        for (Module feature : Thunderhack.moduleManager.modules) {
            if (feature.isDisabled() && onlyEnabled.getValue()) continue;
            if (!Objects.equals(feature.getBind().getBind(), "None") && !feature.getName().equalsIgnoreCase("clickgui") && !feature.getName().equalsIgnoreCase("thundergui")) {
                String sbind = feature.getBind().getBind();
                if (sbind.equals("LEFT_CONTROL")) {
                    sbind = "LCtrl";
                }
                if (sbind.equals("RIGHT_CONTROL")) {
                    sbind = "RCtrl";
                }
                if (sbind.equals("LEFT_SHIFT")) {
                    sbind = "LShift";
                }
                if (sbind.equals("RIGHT_SHIFT")) {
                    sbind = "RShift";
                }
                if (sbind.equals("LEFT_ALT")) {
                    sbind = "LAlt";
                }
                if (sbind.equals("RIGHT_ALT")) {
                    sbind = "RAlt";
                }

                FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "[" + sbind + "]  " + feature.getName(), getPosX() + 5, getPosY() + 18 + y_offset, feature.isOn() ? oncolor.getValue().getColor() : offcolor.getValue().getColor(), false);
                y_offset += 10;
            }
        }
    }


    public void onRenderShaders(DrawContext context) {
        int y_offset1 = 0;
        float max_width = 40;
        for (Module feature : Thunderhack.moduleManager.modules) {
            if (feature.isDisabled() && onlyEnabled.getValue()) continue;
            if (!Objects.equals(feature.getBind().getBind(), "None") && !feature.getName().equalsIgnoreCase("clickgui") && !feature.getName().equalsIgnoreCase("thundergui")) {
                y_offset1 += 10;
                String sbind = feature.getBind().getBind();
                if (sbind.equals("LEFT_CONTROL")) {
                    sbind = "LCtrl";
                }
                if (sbind.equals("RIGHT_CONTROL")) {
                    sbind = "RCtrl";
                }
                if (sbind.equals("LEFT_SHIFT")) {
                    sbind = "LShift";
                }
                if (sbind.equals("RIGHT_SHIFT")) {
                    sbind = "RShift";
                }
                if (sbind.equals("LEFT_ALT")) {
                    sbind = "LAlt";
                }
                if (sbind.equals("RIGHT_ALT")) {
                    sbind = "RAlt";
                }
                float a = FontRenderers.sf_bold_mini.getStringWidth("[" + sbind + "]  " + feature.getName()) * 1.2f;
                if (a > max_width) {
                    max_width = a;
                }
            }
        }
        Render2DEngine.drawGradientGlow(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), max_width, 20 + y_offset1, HudEditor.hudRound.getValue(), 10);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() - 0.5f, getPosY() - 0.5f, max_width + 1, 21 + y_offset1, HudEditor.hudRound.getValue());
        Render2DEngine.drawRoundShader(context.getMatrices(), getPosX(), getPosY(), max_width, 20 + y_offset1, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());

    }
}
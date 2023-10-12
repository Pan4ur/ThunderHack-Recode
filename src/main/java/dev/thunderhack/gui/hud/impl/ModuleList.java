package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.ThunderHack;
import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import dev.thunderhack.gui.font.FontRenderers;

import java.awt.*;

public class ModuleList extends HudElement {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.ColorText);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Float> rainbowSpeed = new Setting<>("Speed", 10.0f, 1.0f, 20.0f);
    private final Setting<Float> saturation = new Setting<>("Saturation", 0.5f, 0.1f, 1.0f);
    private final Setting<Integer> gste = new Setting<>("GS", 30, 1, 50);
    private final Setting<Boolean> glow = new Setting<>("glow", false);
    private final Setting<cMode> cmode = new Setting<>("ColorMode", cMode.Rainbow);
    private final Setting<Boolean> hrender = new Setting<>("HideHud", true);
    private final Setting<Boolean> hhud = new Setting<>("HideRender", true);
    private final Setting<ColorSetting> color2 = new Setting<>("Color2", new ColorSetting(237176633));
    private final Setting<ColorSetting> color3 = new Setting<>("RectColor", new ColorSetting(-16777216));
    private final Setting<ColorSetting> color4 = new Setting<>("SideRectColor", new ColorSetting(-16777216));

    public ModuleList() {
        super("ArrayList", 50, 30);
    }

    boolean reverse;

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        int stringWidth;
        reverse = getPosX() > (float) (Module.mc.getWindow().getScaledWidth() / 2);
        int offset = 0;
        int offset2 = 0;

        int yTotal = 0;
        for (int i = 0; i < ThunderHack.moduleManager.sortedModules.size(); ++i) {
            yTotal += FontRenderers.modules.getFontHeight() + 3;
        }
        setHeight(yTotal);

        //Если режим - ЦветнойТекст, то мы рендерим сначала эффект свечения, а затем плитки
        if (mode.getValue() == Mode.ColorText) {
            for (int k = 0; k < ThunderHack.moduleManager.sortedModules.size(); k++) {
                Module module = ThunderHack.moduleManager.sortedModules.get(k);
                if (!module.isDrawn()) {
                    continue;
                }
                if (hrender.getValue() && module.getCategory() == Module.Category.RENDER) {
                    continue;
                }
                if (hhud.getValue() && module.getCategory() == Module.Category.HUD) {
                    continue;
                }
                Color color1 = null;
                if (cmode.getValue() == cMode.Rainbow) {
                    color1 = Render2DEngine.astolfo(offset2, yTotal, saturation.getValue(), rainbowSpeed.getValue());
                } else if (cmode.getValue() == cMode.DoubleColor) {
                    color1 = Render2DEngine.TwoColoreffect(color.getValue().getColorObject(), color2.getValue().getColorObject(), Math.abs(System.currentTimeMillis() / 10) / 100.0 + offset2 * ((20f - rainbowSpeed.getValue()) / 200));
                } else {
                    color1 = new Color(color.getValue().getColor()).darker();
                }
                if (!reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    if (glow.getValue())
                        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() - 3, getPosY() + (float) offset2 - 1, (float) stringWidth + 4.0f, 9.0f, gste.getValue(), color1);
                }
                if (reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    if (glow.getValue())
                        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() - (float) stringWidth - 3, getPosY() + (float) offset2 - 1, stringWidth + 4, 9f, gste.getValue(), color1);
                }
                offset2 += 8;
            }
        }
        //

        for (int k = 0; k < ThunderHack.moduleManager.sortedModules.size(); k++) {
            Module module = ThunderHack.moduleManager.sortedModules.get(k);
            if (!module.isDrawn()) {
                continue;
            }
            if (hrender.getValue() && module.getCategory() == Module.Category.RENDER) {
                continue;
            }
            if (hhud.getValue() && module.getCategory() == Module.Category.HUD) {
                continue;
            }
            Color color1 = null;

            if (cmode.getValue() == cMode.Rainbow) {
                color1 = Render2DEngine.astolfo(offset, yTotal, saturation.getValue(), rainbowSpeed.getValue());
            } else if (cmode.getValue() == cMode.DoubleColor) {
                color1 = Render2DEngine.TwoColoreffect(color.getValue().getColorObject(), color2.getValue().getColorObject(), Math.abs(System.currentTimeMillis() / 10) / 100.0 + offset * ((20f - rainbowSpeed.getValue()) / 200));
            } else {
                color1 = new Color(color.getValue().getColor()).darker();
            }

            if (mode.getValue() == Mode.ColorRect) {
                if (!reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    if (glow.getValue()) {
                        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() - 3, getPosY() + (float) offset - 1, (float) stringWidth + 4.0f, 9.0f, gste.getValue(), color1);
                    }
                    Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY() + (float) offset, (float) stringWidth + 1.0f, 9f, color1);
                    Render2DEngine.drawRect(context.getMatrices(), getPosX() - 2.0f, getPosY() + (float) offset, 1.0f, 9f, color4.getValue().getColorObject());
                    FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() + 3.0f, getPosY() + 2.0f + (float) offset, -1, false);
                }
                if (reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    if (glow.getValue()) {
                        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() - (float) stringWidth - 3, getPosY() + (float) offset - 1, stringWidth + 4, 9f, gste.getValue(), color1);
                    }
                    Render2DEngine.drawRect(context.getMatrices(), getPosX() - (float) stringWidth, getPosY() + (float) offset, 1.0f + stringWidth, 9f, color1);
                    Render2DEngine.drawRect(context.getMatrices(), getPosX() + 1f, getPosY() + (float) offset, 4.0f, 9f, color4.getValue().getColorObject());
                    FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() - stringWidth + 2.0f, getPosY() + 2.0f + (float) offset, -1, false);
                }
            } else {
                if (!reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY() + (float) offset - 1f, (float) stringWidth + 1.0f, 9.0f, color3.getValue().getColorObject());
                    Render2DEngine.drawRect(context.getMatrices(), getPosX() - 2.0f, getPosY() + (float) offset, 2.0f, 8.0f, color1);
                    FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() + 3.0f, getPosY() + 2.0f + (float) offset, color1.getRGB(), false);
                }
                if (reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    Render2DEngine.drawRect(context.getMatrices(), getPosX() - (float) stringWidth, getPosY() + (float) offset - 1f, stringWidth + 1f, 9.0f, color3.getValue().getColorObject());
                    Render2DEngine.drawRect(context.getMatrices(), getPosX() + 1f, getPosY() + (float) offset, 2.0f, 9.0f, color1);
                    FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() - stringWidth + 2.0f, getPosY() + 2.0f + (float) offset, color1.getRGB(), false);
                }
            }
            offset += 9;
        }
    }

    private enum cMode {
        Rainbow, Custom, DoubleColor
    }

    private enum Mode {
        ColorText, ColorRect
    }
}

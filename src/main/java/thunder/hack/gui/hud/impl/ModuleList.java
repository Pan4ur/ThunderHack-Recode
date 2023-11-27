package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;

public class ModuleList extends HudElement {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.ColorText);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Float> rainbowSpeed = new Setting<>("Speed", 10.0f, 1.0f, 20.0f);
    private final Setting<Float> offset1 = new Setting<>("Offset", 10.0f, 1.0f, 20.0f);
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


    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        int stringWidth;
        boolean reverse = getPosX() > (float) (mc.getWindow().getScaledWidth() / 2);
        int offset = 0;
        int offset2 = 0;
        float yTotal = 0;
        float maxWidth = 0;

        for (Module module : ThunderHack.moduleManager.sortedModules) {
            if (shouldRender(module))
                continue;
            stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
            if (stringWidth > maxWidth)
                maxWidth = stringWidth;
            yTotal += FontRenderers.modules.getFontHeight() + 3;
        }

        float reversedX = getPosX() + 50;

        if (mode.getValue() == Mode.ColorText) {
            for (Module module : ThunderHack.moduleManager.sortedModules) {
                if (!shouldRender(module))
                    continue;

                Color color1 = getColor(offset2, yTotal);

                stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);

                if (!reverse) {
                    if (glow.getValue())
                        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX(), getPosY() + (float) offset2 - 1, (float) stringWidth + 4.0f, 9.0f, gste.getValue(), color1);
                }
                if (reverse) {
                    if (glow.getValue())
                        Render2DEngine.drawBlurredShadow(context.getMatrices(), reversedX - (float) stringWidth - 3, getPosY() + (float) offset2 - 1, stringWidth + 4, 9f, gste.getValue(), color1);
                }
                offset2 += 9;
            }
        }

        for (Module module : ThunderHack.moduleManager.sortedModules) {
            if (!shouldRender(module))
                continue;

            Color color1 = getColor(offset, yTotal);

            if (mode.getValue() == Mode.ColorRect) {
                stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                if (reverse) {
                    if (glow.getValue())
                        Render2DEngine.drawBlurredShadow(context.getMatrices(), reversedX - (float) stringWidth - 3, getPosY() + (float) offset - 1, stringWidth + 4, 9f, gste.getValue(), color1);
                    Render2DEngine.drawRect(context.getMatrices(), reversedX - (float) stringWidth, getPosY() + (float) offset, 1.0f + stringWidth, 9f, color1);
                    Render2DEngine.drawRect(context.getMatrices(), reversedX + 1f, getPosY() + (float) offset, 4.0f, 9f, color4.getValue().getColorObject());
                    FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), reversedX - stringWidth + 2.0f, getPosY() + 2.0f + (float) offset, -1, false);
                } else {
                    if (glow.getValue())
                        Render2DEngine.drawBlurredShadow(context.getMatrices(), getPosX() - 3, getPosY() + (float) offset - 1, (float) stringWidth + 4.0f, 9.0f, gste.getValue(), color1);
                    Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY() + (float) offset, (float) stringWidth + 1.0f, 9f, color1);
                    Render2DEngine.drawRect(context.getMatrices(), getPosX() - 2.0f, getPosY() + (float) offset, 1.0f, 9f, color4.getValue().getColorObject());
                    FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() + 3.0f, getPosY() + 2.0f + (float) offset, -1, false);
                }
            } else {
                stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                if (reverse) {
                    Render2DEngine.drawRect(context.getMatrices(), reversedX - (float) stringWidth, getPosY() + (float) offset - 1f, stringWidth + 1f, 9.0f, color3.getValue().getColorObject());
                    Render2DEngine.drawRect(context.getMatrices(), reversedX + 1f, getPosY() + (float) offset, 2.0f, 9.0f, color1);
                    FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), reversedX - stringWidth + 2.0f, getPosY() + 2.0f + (float) offset, color1.getRGB(), false);
                } else {
                    Render2DEngine.drawRect(context.getMatrices(), getPosX(), getPosY() + (float) offset - 1f, (float) stringWidth + 1.0f, 9.0f, color3.getValue().getColorObject());
                    Render2DEngine.drawRect(context.getMatrices(), getPosX() - 2.0f, getPosY() + (float) offset, 2.0f, 8.0f, color1);
                    FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() + 3.0f, getPosY() + 2.0f + (float) offset, color1.getRGB(), false);
                }
            }
            offset += 9;
        }
        setBounds((int) maxWidth, (int) yTotal);
    }

    private boolean shouldRender(Module m) {
        return m.isDrawn() && (!hrender.getValue() || m.getCategory() != Category.RENDER) && (!hhud.getValue() || m.getCategory() != Category.HUD);
    }

    private Color getColor(int offset, float yTotal){
        if (cmode.getValue() == cMode.Rainbow) {
            return Render2DEngine.astolfo(offset, yTotal, saturation.getValue(), rainbowSpeed.getValue());
        } else if (cmode.getValue() == cMode.DoubleColor) {
            return Render2DEngine.TwoColoreffect(color.getValue().getColorObject(), color2.getValue().getColorObject(), rainbowSpeed.getValue(), offset * offset1.getValue());
        } else {
            return new Color(color.getValue().getColor()).darker();
        }
    }

    private enum cMode {
        Rainbow, Custom, DoubleColor
    }

    private enum Mode {
        ColorText, ColorRect
    }
}

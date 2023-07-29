package thunder.hack.gui.hud.impl;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.Render2DEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.Module;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import net.minecraft.util.Formatting;

import java.awt.*;



public class ModuleList extends HudElement {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.ColorText);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Float> rainbowSpeed = new Setting("Speed", 10.0f, 1.0f, 20.0f);
    private final Setting<Float> saturation = new Setting("Saturation", 0.5f, 0.1f, 1.0f);
    private final Setting<Integer> gste = new Setting("GS", 30, 1, 50);
    private final Setting<Boolean> glow = new Setting<>("glow", false);
    private final Setting<cMode> cmode = new Setting<>("ColorMode", cMode.Rainbow);
    private final Setting<Boolean> hrender = new Setting<>("HideHud", true);
    private final Setting<Boolean> hhud = new Setting<>("HideRender", true);
    private final Setting<ColorSetting> color2 = new Setting<>("Color2", new ColorSetting(237176633));
    private final Setting<ColorSetting> color3 = new Setting<>("RectColor", new ColorSetting(-16777216));
    private final Setting<ColorSetting> color4 = new Setting<>("SideRectColor", new ColorSetting(-16777216));


    public ModuleList() {super("ArrayList", "arraylist",50,30);}

    boolean reverse;


    @Subscribe
    public void onRender2D(Render2DEvent e) {
        super.onRender2D(e);

        int stringWidth;
        reverse = getPosX() > (float) (mc.getWindow().getScaledWidth() / 2);
        int offset = 0;
        int offset2 = 0;

        int yTotal = 0;
        for (int i = 0; i < Thunderhack.moduleManager.sortedModules.size(); ++i) {
            yTotal += FontRenderers.modules.getFontHeight() + 3;
        }
        setHeight(yTotal);

        // Если режим - ЦветнойТекст, то мы рендерим сначала эффект свечения, а затем плитки
        if(mode.getValue() == Mode.ColorText) {
            for (int k = 0; k < Thunderhack.moduleManager.sortedModules.size(); k++) {
                Module module = Thunderhack.moduleManager.sortedModules.get(k);
                if (!module.isDrawn()) {continue;}
                if (hrender.getValue() && module.getCategory() == Category.RENDER) {continue;}
                if (hhud.getValue() && module.getCategory() == Category.HUD) {continue;}
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
                    if (glow.getValue()) Render2DEngine.drawBlurredShadow(e.getMatrixStack(),getPosX() - 3, getPosY() + (float) offset2 - 1, (float) stringWidth + 4.0f, 9.0f, gste.getValue(), color1);
                }
                if (reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    if (glow.getValue()) Render2DEngine.drawBlurredShadow(e.getMatrixStack(),getPosX() - (float) stringWidth - 3, getPosY() + (float) offset2 - 1, stringWidth + 4, 9f, gste.getValue(), color1);
                }
                offset2 += 8;
            }
        }
        //

        for (int k = 0; k < Thunderhack.moduleManager.sortedModules.size(); k++) {
            Module module = Thunderhack.moduleManager.sortedModules.get(k);
            if (!module.isDrawn()) {
                continue;
            }
            if(hrender.getValue() && module.getCategory() == Category.RENDER){
                continue;
            }
            if(hhud.getValue() && module.getCategory() == Category.HUD){
                continue;
            }
            Color color1 = null;

            if(cmode.getValue() == cMode.Rainbow){
                color1 = Render2DEngine.astolfo(offset, yTotal, saturation.getValue(), rainbowSpeed.getValue());
            } else if(cmode.getValue() == cMode.DoubleColor){
                color1 = Render2DEngine.TwoColoreffect(color.getValue().getColorObject(), color2.getValue().getColorObject(), Math.abs(System.currentTimeMillis() / 10) / 100.0 + offset * ((20f - rainbowSpeed.getValue()) / 200) );
            } else {
                color1 = new Color(color.getValue().getColor()).darker();
            }

            if(mode.getValue() == Mode.ColorRect) {
                if (!reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    if (glow.getValue()) {
                        Render2DEngine.drawBlurredShadow(e.getMatrixStack(),getPosX() - 3, getPosY() + (float) offset - 1, (float) stringWidth + 4.0f, 9.0f, gste.getValue(), color1);
                    }
                    Render2DEngine.drawRect(e.getMatrixStack(),getPosX(), getPosY() + (float) offset, (float) stringWidth + 1.0f,   9f, color1);
                    Render2DEngine.drawRect(e.getMatrixStack(),getPosX() - 2.0f, getPosY() + (float) offset,  1.0f,  9f, color4.getValue().getColorObject());
                    FontRenderers.modules.drawString(e.getMatrixStack(),module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() + 3.0f, getPosY() + 2.0f + (float) offset, -1, false);
                }
                if (reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    if (glow.getValue()) {
                        Render2DEngine.drawBlurredShadow(e.getMatrixStack(),getPosX() - (float) stringWidth - 3, getPosY() + (float) offset - 1, stringWidth + 4, 9f, gste.getValue(), color1);
                    }
                    Render2DEngine.drawRect(e.getMatrixStack(),getPosX() - (float) stringWidth, getPosY() + (float) offset,  1.0f + stringWidth,   9f, color1);
                    Render2DEngine.drawRect(e.getMatrixStack(),getPosX() + 1f, getPosY() + (float) offset,  4.0f,  9f, color4.getValue().getColorObject());
                    FontRenderers.modules.drawString(e.getMatrixStack(),module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() - stringWidth + 2.0f, getPosY() + 2.0f + (float) offset, -1, false);
                }
            } else {
                if (!reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    Render2DEngine.drawRect(e.getMatrixStack(),getPosX(), getPosY() + (float) offset - 1f,  (float) stringWidth + 1.0f,   9.0f, color3.getValue().getColorObject());
                    Render2DEngine.drawRect(e.getMatrixStack(),getPosX() - 2.0f, getPosY() + (float) offset,  2.0f,  8.0f, color1);
                    FontRenderers.modules.drawString(e.getMatrixStack(),module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() + 3.0f, getPosY() + 2.0f + (float) offset, color1.getRGB(), false);
                }
                if (reverse) {
                    stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
                    Render2DEngine.drawRect(e.getMatrixStack(),getPosX() - (float) stringWidth, getPosY() + (float) offset - 1f,  stringWidth + 1f,   9.0f, color3.getValue().getColorObject());
                    Render2DEngine.drawRect(e.getMatrixStack(),getPosX() + 1f, getPosY() + (float) offset,  2.0f,  9.0f, color1);
                    FontRenderers.modules.drawString(e.getMatrixStack(),module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : ""), getPosX() - stringWidth + 2.0f, getPosY() + 2.0f + (float) offset, color1.getRGB(), false);
                }
            }
            offset += 9;
        }
    }

    private enum cMode {
        Rainbow, Custom,DoubleColor
    }

    private enum Mode {
        ColorText, ColorRect
    }
}

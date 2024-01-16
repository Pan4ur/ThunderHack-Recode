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
import java.util.Comparator;
import java.util.List;

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
        float maxWidth = 0;
        float reversedX = getPosX() + 50;

        List<Module> list = ThunderHack.moduleManager.getEnabledModules().stream().sorted(Comparator.comparing(module -> FontRenderers.modules.getStringWidth(module.getFullArrayString()) * -1)).toList();

        for (Module module : list) {
            if (!shouldRender(module))
                continue;

            Color color1 = getColor(offset, list.size() * 7);

            stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);

            if (glow.getValue())
                Render2DEngine.drawBlurredShadow(context.getMatrices(), reverse ? reversedX - stringWidth - 3 : getPosX(), getPosY() + offset - 1, stringWidth + 4, 9f, gste.getValue(), color1);

            if (stringWidth > maxWidth)
                maxWidth = stringWidth;

            offset += 9;
        }

        offset = 0;

        for (Module module : list) {
            if (!shouldRender(module))
                continue;

            stringWidth = (int) (FontRenderers.modules.getStringWidth(module.getDisplayName() + Formatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]") : "")) + 3);
            Color color1 = getColor(offset, list.size() * 7);

            Render2DEngine.drawRect(context.getMatrices(), reverse ? reversedX - stringWidth : getPosX(), getPosY() + offset, stringWidth + 1.0f, 9.0f, mode.getValue() == Mode.ColorRect ? color1 : color3.getValue().getColorObject());
            Render2DEngine.drawRect(context.getMatrices(), reverse ? reversedX + 1f : getPosX() - 2.0f, getPosY() + offset, 2.0f, 9f, mode.getValue() == Mode.ColorRect ? color4.getValue().getColorObject() : color1);
            FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + (module.getDisplayInfo() != null ? " [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]" : ""), reverse ? reversedX - stringWidth + 2.0f : getPosX() + 3.0f, getPosY() + 3.0f + offset, mode.getValue() == Mode.ColorRect ? -1 : color1.getRGB(), false);

            offset += 9;
        }
        setBounds((int) maxWidth, list.size() * 7);
    }

    private boolean shouldRender(Module m) {
        return m.isDrawn() && (!hrender.getValue() || m.getCategory() != Category.RENDER) && (!hhud.getValue() || m.getCategory() != Category.HUD);
    }

    private Color getColor(int offset, float yTotal) {
        return switch (cmode.getValue()) {
            case Rainbow -> Render2DEngine.astolfo(offset, yTotal, saturation.getValue(), rainbowSpeed.getValue());
            case DoubleColor ->
                    Render2DEngine.TwoColoreffect(color.getValue().getColorObject(), color2.getValue().getColorObject(), rainbowSpeed.getValue(), offset * offset1.getValue());
            case Custom -> new Color(color.getValue().getColor()).darker();
        };
    }

    private enum cMode {
        Rainbow, Custom, DoubleColor
    }

    private enum Mode {
        ColorText, ColorRect
    }
}

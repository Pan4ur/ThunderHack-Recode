package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import thunder.hack.core.Managers;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class ModuleList extends HudElement {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.ColorText);
    private final Setting<Integer> gste = new Setting<>("GS", 30, 1, 50);
    private final Setting<Boolean> glow = new Setting<>("glow", false);
    private final Setting<Boolean> hrender = new Setting<>("HideHud", true);
    private final Setting<Boolean> hhud = new Setting<>("HideRender", true);
    private final Setting<ColorSetting> color3 = new Setting<>("RectColor", new ColorSetting(-16777216));
    private final Setting<ColorSetting> color4 = new Setting<>("SideRectColor", new ColorSetting(-16777216));

    public ModuleList() {
        super("ArrayList", 50, 30);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        int stringWidth;
        boolean reverse = getPosX() > (mc.getWindow().getScaledWidth() / 2f);
        int offset = 0;
        float maxWidth = 0;
        float reversedX = getPosX();

        List<Module> list;

        try {
            list = Managers.MODULE.getEnabledModules().stream().sorted(Comparator.comparing(module -> FontRenderers.modules.getStringWidth(module.getFullArrayString()) * -1)).toList();
        } catch (IllegalArgumentException ex) {
            return;
        }

        for (Module module : list) {
            if (!shouldRender(module))
                continue;

            Color color1 = HudEditor.getColor(offset);

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
            Color color1 = HudEditor.getColor(offset);

            if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
                Render2DEngine.drawRoundedBlur(context.getMatrices(), reverse ? reversedX - stringWidth : getPosX(), getPosY() + offset, stringWidth + 1.0f, 9.0f, 2, HudEditor.blurColor.getValue().getColorObject());
            } else {
                Render2DEngine.drawRect(context.getMatrices(), reverse ? reversedX - stringWidth : getPosX(), getPosY() + offset, stringWidth + 1.0f, 9.0f, mode.getValue() == Mode.ColorRect ? color1 : color3.getValue().getColorObject());
                Render2DEngine.drawRect(context.getMatrices(), reverse ? reversedX + 1f : getPosX() - 2.0f, getPosY() + offset, 2.0f, 9f, mode.getValue() == Mode.ColorRect ? color4.getValue().getColorObject() : color1);
            }

            FontRenderers.modules.drawString(context.getMatrices(), module.getDisplayName() + Formatting.GRAY + (module.getDisplayInfo() != null ? " [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]" : ""), reverse ? reversedX - stringWidth + 2.0f : getPosX() + 3.0f, getPosY() + 3.0f + offset, mode.getValue() == Mode.ColorRect ? -1 : color1.getRGB());

            offset += 9;
        }
        setBounds(getPosX(), getPosY(),(int) maxWidth * (reverse ? -1 : 1), offset);
    }

    private boolean shouldRender(Module m) {
        return m.isDrawn() && (!hrender.getValue() || m.getCategory() != Category.RENDER) && (!hhud.getValue() || m.getCategory() != Category.HUD);
    }

    private enum Mode {
        ColorText, ColorRect
    }
}

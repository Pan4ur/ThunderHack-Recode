package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.ThunderHack;
import dev.thunderhack.gui.hud.HudElement;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.client.HudEditor;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.gui.font.FontRenderers;

import java.util.Objects;

public class KeyBinds extends HudElement {
    public final Setting<ColorSetting> oncolor = new Setting<>("OnColor", new ColorSetting(0xBEBEBE));
    public final Setting<ColorSetting> offcolor = new Setting<>("OffColor", new ColorSetting(0x646464));
    public final Setting<Boolean> onlyEnabled = new Setting<>("OnlyEnabled", false);

    public KeyBinds() {
        super("KeyBinds", 100, 100);
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        float max_width = 40;
        for (Module feature : ThunderHack.moduleManager.modules) {
            if (feature.isDisabled() && onlyEnabled.getValue()) continue;
            if (!Objects.equals(feature.getBind().getBind(), "None") && feature != ModuleManager.clickGui && feature != ModuleManager.thunderHackGui) {
                float width = FontRenderers.sf_bold_mini.getStringWidth("[" + getShortKeyName(feature) + "]  " + feature.getName()) * 1.2f;
                if (width > max_width) {
                    max_width = width;
                }
            }
        }

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "KeyBinds", getPosX() + max_width / 2, getPosY() + 3, HudEditor.textColor.getValue().getColor());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + max_width / 2f - 2, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + max_width / 2f - 2, getPosY() + 13.7f, getPosX() + 2 + max_width - 4, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        int y_offset = 2;
        for (Module feature : ThunderHack.moduleManager.modules) {
            if (feature.isDisabled() && onlyEnabled.getValue())
                continue;
            if (!Objects.equals(feature.getBind().getBind(), "None") && feature != ModuleManager.clickGui && feature != ModuleManager.thunderHackGui) {
                FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "[" + getShortKeyName(feature) + "]  " + feature.getName(), getPosX() + 5, getPosY() + 18 + y_offset, feature.isOn() ? oncolor.getValue().getColor() : offcolor.getValue().getColor(), false);
                y_offset += 10;
            }
        }
    }

    public void onRenderShaders(DrawContext context) {
        int y_offset1 = 0;
        float max_width = 40;
        for (Module feature : ThunderHack.moduleManager.modules) {
            if (feature.isDisabled() && onlyEnabled.getValue()) continue;
            if (!Objects.equals(feature.getBind().getBind(), "None") && feature != ModuleManager.clickGui && feature != ModuleManager.thunderHackGui) {
                y_offset1 += 10;
                float width = FontRenderers.sf_bold_mini.getStringWidth("[" + getShortKeyName(feature) + "]  " + feature.getName()) * 1.2f;
                if (width > max_width)
                    max_width = width;
            }
        }
        Render2DEngine.drawGradientGlow(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX(), getPosY(), max_width, 20 + y_offset1, HudEditor.hudRound.getValue(), 10);
        Render2DEngine.drawGradientRoundShader(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), getPosX() - 0.5f, getPosY() - 0.5f, max_width + 1, 21 + y_offset1, HudEditor.hudRound.getValue());
        Render2DEngine.drawRoundShader(context.getMatrices(), getPosX(), getPosY(), max_width, 20 + y_offset1, HudEditor.hudRound.getValue(), HudEditor.plateColor.getValue().getColorObject());
    }

    @NotNull
    private static String getShortKeyName(Module feature) {
        String sbind = feature.getBind().getBind();
        return switch (feature.getBind().getBind()) {
            case "LEFT_CONTROL" -> "LCtrl";
            case "RIGHT_CONTROL" -> "RCtrl";
            case "LEFT_SHIFT" -> "LShift";
            case "RIGHT_SHIFT" -> "RShift";
            case "LEFT_ALT" -> "LAlt";
            case "RIGHT_ALT" -> "RAlt";
            default -> sbind.toUpperCase();
        };
    }
}
package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudElement;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.util.Objects;

public class KeyBinds extends HudElement {
    public final Setting<ColorSetting> oncolor = new Setting<>("OnColor", new ColorSetting(0xBEBEBE));
    public final Setting<ColorSetting> offcolor = new Setting<>("OffColor", new ColorSetting(0x646464));
    public final Setting<Boolean> onlyEnabled = new Setting<>("OnlyEnabled", false);

    public KeyBinds() {
        super("KeyBinds", 100, 100);
    }

    private float vAnimation, hAnimation;

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "KeyBinds", getPosX() + hAnimation / 2, getPosY() + 2, HudEditor.textColor.getValue().getColor());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2, getPosY() + 13.7f, getPosX() + 2 + hAnimation / 2f - 2, getPosY() + 14, Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0), HudEditor.textColor.getValue().getColorObject());
        Render2DEngine.horizontalGradient(context.getMatrices(), getPosX() + 2 + hAnimation / 2f - 2, getPosY() + 13.7f, getPosX() + 2 + hAnimation - 4, getPosY() + 14, HudEditor.textColor.getValue().getColorObject(), Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0));

        Render2DEngine.addWindow(context.getMatrices(), getPosX(), getPosY(), getPosX() + hAnimation, getPosY() + vAnimation, 1f);
        int y_offset = 2;
        for (Module feature : ThunderHack.moduleManager.modules) {
            if (feature.isDisabled() && onlyEnabled.getValue())
                continue;
            if (!Objects.equals(feature.getBind().getBind(), "None") && feature != ModuleManager.clickGui && feature != ModuleManager.thunderHackGui) {
                FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "[" + getShortKeyName(feature) + "]  " + feature.getName(), getPosX() + 5, getPosY() + 18 + y_offset, feature.isOn() ? oncolor.getValue().getColor() : offcolor.getValue().getColor(), false);
                y_offset += 10;
            }
        }
        Render2DEngine.popWindow();
    }

    public void onRenderShaders(DrawContext context) {
        int y_offset1 = 0;
        float max_width = 50;
        for (Module feature : ThunderHack.moduleManager.modules) {
            if (feature.isDisabled() && onlyEnabled.getValue()) continue;
            if (!Objects.equals(feature.getBind().getBind(), "None") && feature != ModuleManager.clickGui && feature != ModuleManager.thunderHackGui) {
                y_offset1 += 10;
                float width = FontRenderers.sf_bold_mini.getStringWidth("[" + getShortKeyName(feature) + "]  " + feature.getName()) * 1.2f;
                if (width > max_width)
                    max_width = width;
            }
        }

        vAnimation = AnimationUtility.fast(vAnimation, 20 + y_offset1, 15);
        hAnimation = AnimationUtility.fast(hAnimation, max_width, 15);

        Render2DEngine.drawHudBase(context.getMatrices(), getPosX(), getPosY(), hAnimation, vAnimation, HudEditor.hudRound.getValue());
        setBounds((int) max_width, 20 + y_offset1);
    }

    @NotNull
    public static String getShortKeyName(Module feature) {
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
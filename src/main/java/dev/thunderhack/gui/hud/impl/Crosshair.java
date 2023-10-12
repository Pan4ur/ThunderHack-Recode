package dev.thunderhack.gui.hud.impl;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.render.Render2DEngine;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class Crosshair extends Module {

    public Crosshair() {
        super("Crosshair", Category.HUD);
    }

    private final Setting<Mode> mode = new Setting<>("ColorMode", Mode.Sync);

    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4));

    private enum Mode {
        Custom, Sync
    }

    public void onRender2D(DrawContext context) {
        if (!mc.options.getPerspective().isFirstPerson()) return;


        int progress = (int) (360 * (mc.player.handSwingProgress));
        progress = progress == 0 ? 360 : progress;

        Render2DEngine.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 0, 360, 4f, Color.BLACK);
        Render2DEngine.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 0, 360, 3.5f, Color.BLACK);

        if (mode.getValue() == Mode.Custom) {
            Render2DEngine.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 4f, color.getValue().getColorObject());
            Render2DEngine.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 3.5f, color.getValue().getColorObject());
        } else {
            Render2DEngine.drawElipseSync(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 4f, color.getValue().getColorObject());
            Render2DEngine.drawElipseSync(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 3.5f, color.getValue().getColorObject());
        }
    }
}
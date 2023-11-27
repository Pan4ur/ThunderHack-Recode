package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

public class Crosshair extends Module {

    public Crosshair() {
        super("Crosshair", Category.HUD);
    }

    private final Setting<Mode> mode = new Setting<>("ColorMode", Mode.Sync);

    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4));
    private final Setting<Boolean> dynamic = new Setting<>("Dynamic", true);

    private enum Mode {
        Custom, Sync
    }

    private float xAnim, yAnim, prevPitch;

    public void onRender2D(DrawContext context) {
        if (!mc.options.getPerspective().isFirstPerson()) return;

        float midX = mc.getWindow().getScaledWidth() / 2f;
        float midY = mc.getWindow().getScaledHeight() / 2f;

        if(mc.player.prevHeadYaw - mc.player.getHeadYaw() > 0) {
            xAnim = AnimationUtility.fast(xAnim, midX - 30f, 3);
        } else if(mc.player.prevHeadYaw - mc.player.getHeadYaw() < 0) {
            xAnim = AnimationUtility.fast(xAnim, midX + 30f, 3);
        } else  {
            xAnim = AnimationUtility.fast(xAnim, midX, 5);
        }

        if(prevPitch - mc.player.getPitch() > 0) {
            yAnim = AnimationUtility.fast(yAnim, midY - 30f, 3);
        } else if(prevPitch - mc.player.getPitch() < 0) {
            yAnim = AnimationUtility.fast(yAnim, midY + 30f, 3);
        } else  {
            yAnim = AnimationUtility.fast(yAnim, midY, 5);
        }

        prevPitch = mc.player.getPitch();

        if(!dynamic.getValue()) {
            xAnim = midX;
            yAnim = midY;
        }

        int progress = (int) (360 * (mc.player.handSwingProgress));
        progress = progress == 0 ? 360 : progress;

        Render2DEngine.drawElipse(xAnim, yAnim, 1f, 1f, 0, 360, 4f, Color.BLACK);
        Render2DEngine.drawElipse(xAnim, yAnim, 1f, 1f, 0, 360, 3.5f, Color.BLACK);

        if (mode.getValue() == Mode.Custom) {
            Render2DEngine.drawElipse(xAnim, yAnim, 1f, 1f, 270, progress + 270, 4f, color.getValue().getColorObject());
            Render2DEngine.drawElipse(xAnim, yAnim, 1f, 1f, 270, progress + 270, 3.5f, color.getValue().getColorObject());
        } else {
            Render2DEngine.drawElipseSync(xAnim, yAnim, 1f, 1f, 270, progress + 270, 4f, color.getValue().getColorObject());
            Render2DEngine.drawElipseSync(xAnim, yAnim, 1f, 1f, 270, progress + 270, 3.5f, color.getValue().getColorObject());
        }
    }
}
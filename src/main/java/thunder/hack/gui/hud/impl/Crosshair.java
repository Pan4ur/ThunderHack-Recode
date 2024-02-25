package thunder.hack.gui.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.RotationAxis;
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

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Circle);


    private final Setting<ColorMode> colorMode = new Setting<>("ColorMode", ColorMode.Sync);

    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4));
    private final Setting<Boolean> dynamic = new Setting<>("Dynamic", true);
    private final Setting<Float> range = new Setting<>("Range", 30.0f, 0.1f, 120f);
    private final Setting<Float> speed = new Setting<>("Speed", 3.0f, 0.1f, 20f);
    private final Setting<Float> backSpeed = new Setting<>("BackSpeed", 5.0f, 0.1f, 20f);

    private enum ColorMode {
        Custom, Sync
    }

    private enum Mode {
        Circle, WiseTree
    }

    private float xAnim, yAnim, prevPitch;

    public void onRender2D(DrawContext context) {
        if (!mc.options.getPerspective().isFirstPerson()) return;

        float midX = mc.getWindow().getScaledWidth() / 2f;
        float midY = mc.getWindow().getScaledHeight() / 2f;

        float yawDelta = mc.player.prevHeadYaw - mc.player.getHeadYaw();
        float pitchDelta = prevPitch - mc.player.getPitch();


        if(yawDelta > 0) xAnim = AnimationUtility.fast(xAnim, midX - range.getValue(), speed.getValue());
        else if(yawDelta < 0) xAnim = AnimationUtility.fast(xAnim, midX + range.getValue(), speed.getValue());
        else xAnim = AnimationUtility.fast(xAnim, midX, backSpeed.getValue());


        if(pitchDelta > 0) yAnim = AnimationUtility.fast(yAnim, midY - range.getValue(), speed.getValue());
        else if(pitchDelta < 0) yAnim = AnimationUtility.fast(yAnim, midY + range.getValue(), speed.getValue());
        else yAnim = AnimationUtility.fast(yAnim, midY, backSpeed.getValue());

        prevPitch = mc.player.getPitch();

        if(!dynamic.getValue()) {
            xAnim = midX;
            yAnim = midY;
        }

        int progress = (int) (360 * (mc.player.handSwingProgress));
        progress = progress == 0 ? 360 : progress;

        if(mode.is(Mode.Circle)) {
            Render2DEngine.drawElipse(xAnim, yAnim, 1f, 1f, 0, 360, 4f, Color.BLACK);
            Render2DEngine.drawElipse(xAnim, yAnim, 1f, 1f, 0, 360, 3.5f, Color.BLACK);

            if (colorMode.is(ColorMode.Custom)) {
                Render2DEngine.drawElipse(xAnim, yAnim, 1f, 1f, 270, progress + 270, 4f, color.getValue().getColorObject());
                Render2DEngine.drawElipse(xAnim, yAnim, 1f, 1f, 270, progress + 270, 3.5f, color.getValue().getColorObject());
            } else {
                Render2DEngine.drawElipseSync(xAnim, yAnim, 1f, 1f, 270, progress + 270, 4f, color.getValue().getColorObject());
                Render2DEngine.drawElipseSync(xAnim, yAnim, 1f, 1f, 270, progress + 270, 3.5f, color.getValue().getColorObject());
            }
        } else {
            Color color = this.color.getValue().getColorObject();
            context.getMatrices().push();
            context.getMatrices().translate(xAnim, yAnim, 0);
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation((System.currentTimeMillis() % 70000) / 70000f * 360f));
            context.getMatrices().translate(-xAnim, -yAnim, 0);
            Render2DEngine.drawRect(context.getMatrices(),xAnim - 0.75f, yAnim - 5, 1.5f, 10, color);
            Render2DEngine.drawRect(context.getMatrices(),xAnim - 5, yAnim - 0.75f, 10, 1.5f, color);
            Render2DEngine.drawRect(context.getMatrices(),xAnim, yAnim - 5, 5, 1.5f, color);
            Render2DEngine.drawRect(context.getMatrices(),xAnim - 5, yAnim + 4, 5.25f, 1.5f, color);
            Render2DEngine.drawRect(context.getMatrices(),xAnim - 5f, yAnim - 5, 1.5f, 4.25f, color);
            Render2DEngine.drawRect(context.getMatrices(),xAnim + 3.5f, yAnim, 1.5f, 5.5f, color);
            context.getMatrices().pop();
        }
    }

    public float getAnimatedPosX() {
        if(xAnim == 0)
            return mc.getWindow().getScaledWidth() / 2f;
        return xAnim;
    }

    public float getAnimatedPosY() {
        if(yAnim == 0)
            return mc.getWindow().getScaledHeight() / 2f;
        return yAnim;
    }
}
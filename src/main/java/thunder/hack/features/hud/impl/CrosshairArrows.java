package thunder.hack.features.hud.impl;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

import static thunder.hack.features.hud.impl.RadarRewrite.getRotations;

public class CrosshairArrows extends HudElement {
    public CrosshairArrows() {
        super("CrosshairArrows", 0, 0);
    }

    public static Setting<Boolean> glow = new Setting<>("Glow", false);
    private final Setting<Float> width = new Setting<>("Height", 2.28f, 0.1f, 5f);
    private final Setting<BooleanSettingGroup> down = new Setting<>("Down", new BooleanSettingGroup(false));
    private final Setting<Float> downHeight = new Setting<>("DownHeight", 3.63f, 0.1F, 20.0F).addToGroup(down);
    private final Setting<Float> tracerWidth = new Setting<>("Width", 0.44F, 0.0F, 8.0F);
    private final Setting<Integer> xOffset = new Setting<>("TracerRadius", 68, 20, 100);
    private final Setting<Integer> pitchLock = new Setting<>("PitchLock", 42, 0, 90);
    private final Setting<triangleModeEn> triangleMode = new Setting<>("TracerCMode", triangleModeEn.Astolfo);
    private final Setting<ColorSetting> colorf = new Setting<>("Friend", new ColorSetting(new Color(0x00E800)));
    private final Setting<ColorSetting> colors = new Setting<>("Tracer", new ColorSetting(new Color(0xFFFF00)));

    private float smoothYaw = 0;

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);
        if (fullNullCheck()) return;

        float middleW = ModuleManager.crosshair.getAnimatedPosX();
        float middleH = ModuleManager.crosshair.getAnimatedPosY();

        int color = 0;

        context.getMatrices().push();
        context.getMatrices().translate(middleW, middleH, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f / Math.abs(90f / MathUtility.clamp(mc.player.getPitch(), pitchLock.getValue(), 90f)) - 102));
        context.getMatrices().translate(-middleW, -middleH, 0);

        smoothYaw = AnimationUtility.fast(smoothYaw, mc.player.getYaw(), 13);

        for (PlayerEntity e : Lists.newArrayList(mc.world.getPlayers())) {
            if (e != mc.player){
                context.getMatrices().push();

                float yaw = getRotations(e) - smoothYaw;
                context.getMatrices().translate(middleW, middleH, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                context.getMatrices().translate(-middleW, -middleH, 0.0F);

                if (Managers.FRIEND.isFriend(e))
                    color = colorf.getValue().getColor();
                else color = switch (triangleMode.getValue()) {
                    case Custom -> colors.getValue().getColor();
                    case Astolfo -> Render2DEngine.astolfo(false, 1).getRGB();
                };

                Render2DEngine.drawTracerPointer(context.getMatrices(), middleW, middleH - xOffset.getValue(), width.getValue() * 5F,tracerWidth.getValue(), downHeight.getValue(), down.getValue().isEnabled(), glow.getValue(), color);

                context.getMatrices().translate(middleW, middleH, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                context.getMatrices().translate(-middleW, -middleH, 0.0F);
                context.getMatrices().pop();
            }
        }
        context.getMatrices().pop();
    }

    public enum triangleModeEn {
        Custom, Astolfo
    }
}

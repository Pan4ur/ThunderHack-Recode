package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.injection.accesors.IWorldRenderer;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class BreakHighLight extends Module {
    public BreakHighLight() {
        super("BreakHighLight", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Shrink);

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(0x90FD0000, true)));
    private final Setting<ColorSetting> color2 = new Setting<>("Color2", new ColorSetting(new Color(0xFFFD0000, true)));
    private final Setting<ColorSetting> ocolor = new Setting<>("OutlineColor", new ColorSetting(new Color(0x903FFD00, true)));
    private final Setting<ColorSetting> ocolor2 = new Setting<>("OutlineColor2", new ColorSetting(new Color(0xFF2EFD00, true)));
    private final Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(0xFFFFFFFF));

    private final Setting<Float> lineWidth = new Setting<>("LineWidth", 2F, 0f, 5F);
    private final Setting<Boolean> otherPlayer = new Setting<>("OtherPlayer", true);

    private float prevProgress;

    public void onRender3D(MatrixStack stack) {
        if (mc.interactionManager.isBreakingBlock() && mc.crosshairTarget != null && mc.crosshairTarget instanceof BlockHitResult bhr && !mc.world.isAir(bhr.getBlockPos())) {
            Box shrunkMineBox = new Box(bhr.getBlockPos().getX(), bhr.getBlockPos().getY(), bhr.getBlockPos().getZ(), bhr.getBlockPos().getX(), bhr.getBlockPos().getY(), bhr.getBlockPos().getZ());

            float noom; //ам ням ебался

            switch (mode.getValue()) {
                case Grow -> noom = Render2DEngine.interpolateFloat(prevProgress, MathUtility.clamp(mc.interactionManager.currentBreakingProgress, 0f, 1f), Render3DEngine.getTickDelta());
                case Shrink -> noom = 1f - Render2DEngine.interpolateFloat(prevProgress, mc.interactionManager.currentBreakingProgress, Render3DEngine.getTickDelta());
                default -> noom = 1;
            }

            Render3DEngine.drawFilledBox(
                    stack,
                    shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                    Render2DEngine.interpolateColorC(color.getValue().getColorObject(),color2.getValue().getColorObject(),noom)
            );
            Render3DEngine.drawBoxOutline(
                    shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                    Render2DEngine.interpolateColorC(ocolor.getValue().getColorObject(),ocolor2.getValue().getColorObject(),noom),
                    lineWidth.getValue()
            );

            switch (mode.getValue()) {
                case Grow -> prevProgress = noom;
                case Shrink -> prevProgress = 1 - noom;
                default -> prevProgress = 1f;
            }
        }
        ((IWorldRenderer) mc.worldRenderer).getBlockBreakingInfos().forEach(((integer, destroyBlockProgress) -> {
            Entity object = mc.world.getEntityById(integer);
            if (object != null && otherPlayer.getValue() && !object.getName().equals(mc.player.getName())) {
                BlockPos pos = destroyBlockProgress.getPos();
                Render3DEngine.drawTextIn3D(String.valueOf(object.getName().getString()),pos.toCenterPos(),0,0.1,0,textColor.getValue().getColorObject());
                Box shrunkMineBox = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());

                float noom;
                switch (mode.getValue()) {
                    case Grow -> noom = MathUtility.clamp((destroyBlockProgress.getStage() / 10f), 0f, 1f);
                    case Shrink -> noom = 1f - (destroyBlockProgress.getStage() / 10f);
                    default -> noom = 1;
                }

                Render3DEngine.drawFilledBox(
                        stack,
                        shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                        Render2DEngine.interpolateColorC(color.getValue().getColorObject(),color2.getValue().getColorObject(),noom)
                );

                Render3DEngine.drawBoxOutline(
                        shrunkMineBox.shrink(noom, noom, noom).offset(0.5 + noom * 0.5, 0.5 + noom * 0.5, 0.5 + noom * 0.5),
                        Render2DEngine.interpolateColorC(ocolor.getValue().getColorObject(),ocolor2.getValue().getColorObject(),noom),
                        lineWidth.getValue()
                );
            }
        }));
    }

    private enum Mode {
        Grow, Shrink, Static
    }
}
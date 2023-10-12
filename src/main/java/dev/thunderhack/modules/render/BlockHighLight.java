package dev.thunderhack.modules.render;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.render.Render2DEngine;
import dev.thunderhack.utils.render.Render3DEngine;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;

public class BlockHighLight extends Module {
    public BlockHighLight() {
        super("BlockHighLight", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting("Mode", Mode.Outline);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFFFFFFFF));
    private final Setting<Float> lineWidth = new Setting<>("LineWidth", 1F, 0f, 5F);

    private enum Mode {
        Both, BothSide, Fill, FilledSide, Outline, OutlinedSide
    }

    public void onRender3D(MatrixStack stack) {
        if (mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return;

        switch (mode.getValue()) {
            case Both -> {
                Render3DEngine.drawBoxOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(), 255), lineWidth.getValue());
                Render3DEngine.drawFilledBox(stack, new Box(bhr.getBlockPos()), color.getValue().getColorObject());
            }
            case BothSide -> {
                Render3DEngine.drawSideOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue(),bhr.getSide());
                Render3DEngine.drawFilledSide(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject(),bhr.getSide());
            }
            case Fill -> Render3DEngine.drawFilledBox(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject());
            case FilledSide -> Render3DEngine.drawFilledSide(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject(),bhr.getSide());

            case Outline ->  Render3DEngine.drawBoxOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue());
            case OutlinedSide -> Render3DEngine.drawSideOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue(),bhr.getSide());
        }
    }
}

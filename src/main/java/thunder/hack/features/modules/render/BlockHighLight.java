package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

import static thunder.hack.utility.render.Render3DEngine.*;

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
                drawBoxOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(), 255), lineWidth.getValue());
                drawFilledBox(stack, new Box(bhr.getBlockPos()), color.getValue().getColorObject());
            }
            case BothSide -> {
                drawSideOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue(),bhr.getSide());
                drawFilledSide(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject(),bhr.getSide());
            }
            case Fill -> drawFilledBox(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject());
            case FilledSide -> drawFilledSide(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject(),bhr.getSide());

            case Outline ->  drawBoxOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue());
            case OutlinedSide -> drawSideOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue(),bhr.getSide());
        }
    }
}

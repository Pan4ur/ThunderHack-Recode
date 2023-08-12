package thunder.hack.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class BlockHighLight extends Module {
    public BlockHighLight() {
        super("BlockHighLight", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting("Mode", Mode.Outline);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFFFFFFFF));
    private final Setting<Float> lineWidth = new Setting<>("LineWidth", 1F, 0f, 5F);

    private enum Mode{
        Outline, Fill, Both, FilledSide, OutlinedSide, BothSide
    }


    public void onRender3D(MatrixStack stack){
        if(mc.crosshairTarget == null) return;
        if(mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;
        if(!(mc.crosshairTarget instanceof BlockHitResult bhr)) return;

        if(mode.getValue() == Mode.Outline){
            Render3DEngine.drawBoxOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue());
        } else if(mode.getValue() == Mode.Fill){
            Render3DEngine.drawFilledBox(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject());
        } else if(mode.getValue() == Mode.Both){
            Render3DEngine.drawBoxOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue());
            Render3DEngine.drawFilledBox(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject());
        } else if(mode.getValue() == Mode.OutlinedSide){
            Render3DEngine.drawSideOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue(),bhr.getSide());
        } else if(mode.getValue() == Mode.FilledSide){
            Render3DEngine.drawFilledSide(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject(),bhr.getSide());
        } else if(mode.getValue() == Mode.BothSide){
            Render3DEngine.drawSideOutline(new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(color.getValue().getColorObject(),255), lineWidth.getValue(),bhr.getSide());
            Render3DEngine.drawFilledSide(stack,new Box(bhr.getBlockPos()),color.getValue().getColorObject(),bhr.getSide());
        }
    }
}

package thunder.hack.features.modules.render;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VoidESP extends Module {
    public VoidESP() {
        super("VoidESP", Category.RENDER);
    }

    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(0xD7FFA600, true)));
    public Setting<Float> range = new Setting<>("Range", 6.0f, 3.0f, 16.0f);

    private List<BlockPos> holes = new ArrayList<>();

    public void onRender3D(MatrixStack stack) {
        holes.forEach(h -> Render3DEngine.renderCrosses(new Box(h), color.getValue().getColorObject(), 2.0f));
    }

    public List<BlockPos> calcHoles() {
        ArrayList<BlockPos> voidHoles = new ArrayList<>();
        for (int x = (int) (mc.player.getX() - range.getValue()); x < mc.player.getX() + range.getValue(); x++)
            for (int z = (int) (mc.player.getZ() - range.getValue()); z < mc.player.getZ() + range.getValue(); z++) {
                BlockPos pos = BlockPos.ofFloored(x, mc.world.getBottomY(), z);
                if (mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) continue;
                voidHoles.add(pos);
            }
        return voidHoles;
    }

    @Override
    public void onThread() {
        holes = calcHoles();
    }
}

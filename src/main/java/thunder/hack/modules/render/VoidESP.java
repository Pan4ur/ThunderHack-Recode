package thunder.hack.modules.render;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.modules.Module;
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
    public Setting<Float> range = new Setting<>("Range",6.0f, 3.0f, 16.0f);
    public Setting<Boolean> down = new Setting<>("Up", false);
    private List<BlockPos> holes = new ArrayList<>();

    public void onRender3D(MatrixStack stack) {
        for (BlockPos pos : holes) {
            Render3DEngine.renderCrosses(down.getValue() ? new Box(pos.up()) : new Box(pos), color.getValue().getColorObject(), 2.0f);
        }
    }

    public List<BlockPos> calcHoles() {
        ArrayList<BlockPos> voidHoles = new ArrayList<>();
        for(int x = (int) (mc.player.getX() - range.getValue()); x < mc.player.getX() + range.getValue(); x++)
            for(int y = (int) (mc.player.getY() - range.getValue()); y < mc.player.getY() + range.getValue(); y++)
                for(int z = (int) (mc.player.getZ() - range.getValue()); z < mc.player.getZ() + range.getValue(); z++){
                    BlockPos pos = BlockPos.ofFloored(x,y,z);
                    if ( pos.getY() != mc.world.getBottomY() || mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK) continue;
                    voidHoles.add(pos);
                }
        return voidHoles;
    }

    @Override
    public void onThread(){
        holes = calcHoles();
    }
}

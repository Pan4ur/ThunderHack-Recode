package thunder.hack.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.modules.Module;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.BarrierBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Search extends Module {

    public static CopyOnWriteArrayList<BlockVec> blocks = new CopyOnWriteArrayList<>();
    public static ArrayList<Block> defaultBlocks = new ArrayList<>();
    private final Setting<Float> range = new Setting<>("Range", 100f, 1f, 500f);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFF00FFFF));
    private final Setting<Boolean> illegals = new Setting<>("Illegals", true);
    private final Setting<Boolean> tracers = new Setting<>("Tracers", false);
    private final Setting<Boolean> fill = new Setting<>("Fill", true);
    private final Setting<Boolean> outline = new Setting<>("Outline", true);


    public Search() {
        super("Search", "подсветка блоков", Category.RENDER);
    }

    @Override
    public void onEnable() {
        blocks.clear();
    }


    @Override
    public void onThread() {
        if (mc.world == null || mc.player == null) return;
        ArrayList<BlockVec> bloks = new ArrayList<>();
        for (BlockPos pos : BlockPos.iterateOutwards(mc.player.getBlockPos(), (int) (float) range.getValue(), 128,  (int) (float)range.getValue())) {
            if (shouldAdd(mc.world.getBlockState(pos).getBlock(), pos)) {
                bloks.add(new BlockVec(pos.getX(), pos.getY(), pos.getZ()));
            }
        }
        blocks.clear();
        blocks.addAll(bloks);
        if(FrameRateCounter.INSTANCE.getFps() < 10) disable("Saving ur pc :)");
    }

    public void onRender3D(MatrixStack stack) {
        if (fullNullCheck() || blocks.isEmpty()) return;

        if (fill.getValue() || outline.getValue()) {
            for (BlockVec vec : blocks) {
                if (vec.getDistance(new BlockVec(mc.player.getX(), mc.player.getY(), mc.player.getZ())) > range.getValue() || !shouldRender(vec)) {
                    blocks.remove(vec);
                    continue;
                }

                BlockPos pos = new BlockPos((int) vec.x, (int) vec.y, (int) vec.z);

                if (fill.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), color.getValue().getColorObject());
                }
                if(outline.getValue()){
                    Render3DEngine.drawBoxOutline(new Box(pos), color.getValue().getColorObject(),1f);
                }
            }
        }

        if (tracers.getValue()) {
            for (BlockVec vec : blocks) {
                if (vec.getDistance(new BlockVec(mc.player.getX(), mc.player.getY(), mc.player.getZ())) > range.getValue() || !shouldRender(vec)) {
                    blocks.remove(vec);
                    continue;
                }

                Vec3d vec2 = new Vec3d(0, 0, 75)
                        .rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
                        .rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
                        .add(mc.cameraEntity.getEyePos());

                Render3DEngine.drawLine(vec2.x, vec2.y, vec2.z, vec.x + 0.5f, vec.y + 0.5f, vec.z + 0.5f, color.getValue().getColorObject(), 1f);
            }
        }
    }

    private boolean shouldAdd(Block block, BlockPos pos) {
        if (defaultBlocks.contains(block)) {
            return true;
        }
        if (illegals.getValue()) {
            return isIllegal(block, pos);
        }
        return false;
    }

    private boolean shouldRender(BlockVec vec) {
        if (defaultBlocks.contains(mc.world.getBlockState(new BlockPos((int) vec.x, (int) vec.y, (int) vec.z)).getBlock())) {
            return true;
        }

        if (illegals.getValue()) {
            return isIllegal(mc.world.getBlockState(new BlockPos((int) vec.x, (int) vec.y, (int) vec.z)).getBlock(), new BlockPos((int) vec.x, (int) vec.y, (int) vec.z));
        }

        return false;
    }

    private boolean isIllegal(Block block, BlockPos pos) {
        if (block instanceof CommandBlock || block instanceof BarrierBlock) return true;

        if (block == Blocks.BEDROCK) {
            if (!isHell()) {
                return pos.getY() > 4;
            } else {
                return pos.getY() > 127 || (pos.getY() < 123 && pos.getY() > 4);
            }
        }
        return false;
    }


    public boolean isHell() {
        if (mc.world == null) return false;
        return Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "the_nether");
    }

    private static class BlockVec {
        public final double x;
        public final double y;
        public final double z;

        public BlockVec(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getDistance(BlockVec v) {
            double dx = x - v.x;
            double dy = y - v.y;
            double dz = z - v.z;

            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }
}
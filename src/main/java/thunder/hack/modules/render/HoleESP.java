package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.HoleUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoleESP extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.CubeOutline);
    private final Setting<Integer> rangeXZ = new Setting<>("Range XY", 10, 1, 128);
    private final Setting<Integer> rangeY = new Setting<>("Range Y", 5, 1, 128);

    private final Setting<ColorSetting> indestrictibleColor = new Setting<>("Indestrictible Color", new ColorSetting(new Color(0x7A00FF).getRGB()));
    private final Setting<ColorSetting> bedrockColor = new Setting<>("Bedrock Color", new ColorSetting(new Color(0x00FF51).getRGB()));

    private final Setting<Float> height = new Setting<>("Height", 1f, 0.01f, 5f);
    private final Setting<Float> lineWith = new Setting<>("Line Width", 0.5f, 0.01f, 5f);

    private enum Mode {
        Fade,
        Fade2,
        CubeOutline,
        CubeFill,
        CubeBoth
    }

    private final List<PosWithColor> positions = new CopyOnWriteArrayList<>();

    public HoleESP() {
        super("HoleESP", Category.RENDER);
    }

    public void onRender3D(MatrixStack stack) {
        if (positions.isEmpty()) return;

        for (PosWithColor pwc : positions) {
            switch (mode.getValue()){
                case Fade -> renderFade(pwc, stack);
                case Fade2 -> renderFade2(pwc, stack);
                case CubeFill -> renderFill(pwc, stack);
                case CubeOutline -> renderOutline(pwc, stack);
                case CubeBoth -> {
                    renderOutline(pwc, stack);
                    renderFill(pwc, stack);
                }
            }
        }
    }

    public void renderFade(PosWithColor posWithColor, MatrixStack stack) {
        RenderSystem.disableCull();
        Render3DEngine.drawFilledFadeBox(stack,
                new Box(
                        posWithColor.getBp().getX(),
                        posWithColor.getBp().getY(),
                        posWithColor.getBp().getZ(),
                        posWithColor.getBp().getX() + (posWithColor.checkDirX() ? 2f : 1f),
                        posWithColor.getBp().getY() + height.getValue(),
                        posWithColor.getBp().getZ() + (posWithColor.checkDirZ() ? 2f : 1f)
                ), Render2DEngine.applyOpacity(posWithColor.getColor(), 60), Render2DEngine.applyOpacity(posWithColor.getColor(), 0)
        );
        Render3DEngine.drawBottomOutline(
                new Box(
                        posWithColor.getBp().getX(),
                        posWithColor.getBp().getY(),
                        posWithColor.getBp().getZ(),
                        posWithColor.getBp().getX() + (posWithColor.checkDirX() ? 2f : 1f),
                        posWithColor.getBp().getY() + height.getValue(),
                        posWithColor.getBp().getZ() + (posWithColor.checkDirZ() ? 2f : 1f)
                ), posWithColor.getColor(), lineWith.getValue()
        );
        RenderSystem.enableCull();
    }


    public void renderFade2(PosWithColor posWithColor, MatrixStack stack) {
        RenderSystem.disableCull();
        Render3DEngine.drawFilledFadeBox(stack,
                new Box(
                        posWithColor.getBp().getX(),
                        posWithColor.getBp().getY(),
                        posWithColor.getBp().getZ(),
                        posWithColor.getBp().getX() + (posWithColor.checkDirX() ? 2f : 1f),
                        posWithColor.getBp().getY() + height.getValue(),
                        posWithColor.getBp().getZ() + (posWithColor.checkDirZ() ? 2f : 1f)
                ), Render2DEngine.applyOpacity(posWithColor.getColor(), 60), Render2DEngine.applyOpacity(posWithColor.getColor(), 0)
        );
        Render3DEngine.drawHoleOutline(
                new Box(
                        posWithColor.getBp().getX(),
                        posWithColor.getBp().getY(),
                        posWithColor.getBp().getZ(),
                        posWithColor.getBp().getX() + (posWithColor.checkDirX() ? 2f : 1f),
                        posWithColor.getBp().getY() + height.getValue(),
                        posWithColor.getBp().getZ() + (posWithColor.checkDirZ() ? 2f : 1f)
                ), posWithColor.getColor(), lineWith.getValue()
        );

        Render3DEngine.drawFilledBox(stack,
                new Box(
                        posWithColor.getBp().getX(),
                        posWithColor.getBp().getY(),
                        posWithColor.getBp().getZ(),
                        posWithColor.getBp().getX() + (posWithColor.checkDirX() ? 2f : 1f),
                        posWithColor.getBp().getY() + 0.01,
                        posWithColor.getBp().getZ() + (posWithColor.checkDirZ() ? 2f : 1f)
                ), posWithColor.getColor()
        );

        RenderSystem.enableCull();
    }

    public void renderOutline(PosWithColor posWithColor, MatrixStack stack) {
        Render3DEngine.drawBoxOutline(
                new Box(
                        posWithColor.getBp().getX(),
                        posWithColor.getBp().getY(),
                        posWithColor.getBp().getZ(),
                        posWithColor.getBp().getX() + (posWithColor.checkDirX() ? 2f : 1f),
                        posWithColor.getBp().getY() + height.getValue(),
                        posWithColor.getBp().getZ() + (posWithColor.checkDirZ() ? 2f : 1f)
                ), posWithColor.getColor(), lineWith.getValue()
        );
    }

    public void renderFill(PosWithColor posWithColor, MatrixStack stack) {
        Render3DEngine.drawFilledBox(stack,
                new Box(
                        posWithColor.getBp().getX(),
                        posWithColor.getBp().getY(),
                        posWithColor.getBp().getZ(),
                        posWithColor.getBp().getX() + (posWithColor.checkDirX() ? 2f : 1f),
                        posWithColor.getBp().getY() + height.getValue(),
                        posWithColor.getBp().getZ() + (posWithColor.checkDirZ() ? 2f : 1f)
                ), posWithColor.getColor()
        );
    }


    @Override
    public void onThread() {
        if (fullNullCheck()) return;
        findHoles();
    }

    private void findHoles() {
        ArrayList<PosWithColor> blocks = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();

        for (int i = centerPos.getX() - rangeXZ.getValue(); i < centerPos.getX() + rangeXZ.getValue(); i++) {
            for (int j = centerPos.getY() - rangeY.getValue(); j < centerPos.getY() + rangeY.getValue(); j++) {
                for (int k = centerPos.getZ() - rangeXZ.getValue(); k < centerPos.getZ() + rangeXZ.getValue(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (HoleUtility.validIndestructible(pos)) {
                        blocks.add(new PosWithColor(pos, false, false, indestrictibleColor.getValue().getColorObject()));
                    } else if (HoleUtility.validBedrock(pos)) {
                        blocks.add(new PosWithColor(pos, false, false, bedrockColor.getValue().getColorObject()));
                    } else if (HoleUtility.validTwoBlockBedrockXZ(pos)) {
                        blocks.add(new PosWithColor(pos, true, false, bedrockColor.getValue().getColorObject()));
                    } else if (HoleUtility.validTwoBlockIndestructibleXZ(pos)) {
                        blocks.add(new PosWithColor(pos, true, false, indestrictibleColor.getValue().getColorObject()));
                    } else if (HoleUtility.validTwoBlockBedrockXZ1(pos)) {
                        blocks.add(new PosWithColor(pos, false, true, bedrockColor.getValue().getColorObject()));
                    } else if (HoleUtility.validTwoBlockIndestructibleXZ1(pos)) {
                        blocks.add(new PosWithColor(pos, false, true, indestrictibleColor.getValue().getColorObject()));
                    } else if (HoleUtility.validQuadBedrock(pos)) {
                        blocks.add(new PosWithColor(pos, true, true, bedrockColor.getValue().getColorObject()));
                    } else if (HoleUtility.validQuadIndestructible(pos)) {
                        blocks.add(new PosWithColor(pos, true, true, indestrictibleColor.getValue().getColorObject()));
                    }
                }
            }
        }
        positions.clear();
        positions.addAll(blocks);
    }

    public static class PosWithColor {
        private final BlockPos bp;
        private final boolean dirX;
        private final boolean dirZ;
        private final Color color;

        public PosWithColor(BlockPos pos, boolean dirX, boolean dirZ, Color color) {
            this.bp = pos;
            this.dirX = dirX;
            this.dirZ = dirZ;
            this.color = color;
        }

        public BlockPos getBp() {
            return bp;
        }

        public boolean checkDirX() {
            return dirX;
        }

        public boolean checkDirZ() {
            return dirZ;
        }

        public Color getColor() {
            return color;
        }
    }
}

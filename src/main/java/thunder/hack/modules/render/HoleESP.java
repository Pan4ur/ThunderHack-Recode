package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
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

    private final List<BoxWithColor> positions = new CopyOnWriteArrayList<>();

    public HoleESP() {
        super("HoleESP", Category.RENDER);
    }

    public void onRender3D(MatrixStack stack) {
        if (positions.isEmpty()) return;

        for (BoxWithColor pwc : positions) {
            switch (mode.getValue()) {
                case Fade -> renderFade(pwc, stack);
                case Fade2 -> renderFade2(pwc, stack);
                case CubeFill -> renderFill(pwc);
                case CubeOutline -> renderOutline(pwc);
                case CubeBoth -> {
                    renderOutline(pwc);
                    renderFill(pwc);
                }
            }
        }
    }

    public void renderFade(@NotNull HoleESP.BoxWithColor posWithColor, MatrixStack stack) {
        RenderSystem.disableCull();
        Render3DEngine.drawFilledFadeBox(stack,
                posWithColor.box, Render2DEngine.applyOpacity(posWithColor.color(), 60), Render2DEngine.applyOpacity(posWithColor.color(), 0)
        );
        Render3DEngine.drawSideOutline(
                posWithColor.box, posWithColor.color(), lineWith.getValue(), Direction.DOWN
        );
        RenderSystem.enableCull();
    }


    public void renderFade2(@NotNull HoleESP.BoxWithColor boxWithColor, MatrixStack stack) {
        RenderSystem.disableCull();
        Render3DEngine.drawFilledFadeBox(stack,
                boxWithColor.box, Render2DEngine.applyOpacity(boxWithColor.color(), 60), Render2DEngine.applyOpacity(boxWithColor.color(), 0)
        );
        Render3DEngine.drawHoleOutline(
                boxWithColor.box, boxWithColor.color(), lineWith.getValue()
        );

        Render3DEngine.drawFilledBox(stack,
                new Box(boxWithColor.box.minX, boxWithColor.box.minY, boxWithColor.box.minZ,
                        boxWithColor.box.maxX, boxWithColor.box.minY + 0.01f, boxWithColor.box.maxZ), boxWithColor.color()
        );

        RenderSystem.enableCull();
    }

    public void renderOutline(@NotNull HoleESP.BoxWithColor boxWithColor) {
        Render3DEngine.drawBoxOutline(
                boxWithColor.box, boxWithColor.color(), lineWith.getValue()
        );
    }

    public void renderFill(@NotNull HoleESP.BoxWithColor boxWithColor) {
        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(boxWithColor.box(), boxWithColor.color()));
    }


    @Override
    public void onThread() {
        if (fullNullCheck()) return;
        findHoles();
    }

    private void findHoles() {
        ArrayList<BoxWithColor> blocks = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();
        List<Box> boxes = new ArrayList<>();

        for (int i = centerPos.getX() - rangeXZ.getValue(); i < centerPos.getX() + rangeXZ.getValue(); i++) {
            for (int j = centerPos.getY() - rangeY.getValue(); j < centerPos.getY() + rangeY.getValue(); j++) {
                for (int k = centerPos.getZ() - rangeXZ.getValue(); k < centerPos.getZ() + rangeXZ.getValue(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + height.getValue(), pos.getZ() + 1);
                    Color color = indestrictibleColor.getValue().getColorObject();
                    if (HoleUtility.validIndestructible(pos)) {
                    } else if (HoleUtility.validBedrock(pos)) {
                        color = bedrockColor.getValue().getColorObject();
                    } else if (HoleUtility.validTwoBlockBedrockXZ(pos)) {
                        boolean east = mc.world.isAir(pos.offset(Direction.EAST));
                        boolean south = mc.world.isAir(pos.offset(Direction.SOUTH));
                        box = new Box(box.minX, box.minY, box.minZ, box.maxX + (east ? 1 : 0), box.maxY, box.maxZ + (south ? 1 : 0));
                        color = bedrockColor.getValue().getColorObject();
                    } else if (HoleUtility.validTwoBlockIndestructibleXZ(pos)) {
                        boolean east = mc.world.isAir(pos.offset(Direction.EAST));
                        boolean south = mc.world.isAir(pos.offset(Direction.SOUTH));
                        box = new Box(box.minX, box.minY, box.minZ, box.maxX + (east ? 1 : 0), box.maxY, box.maxZ + (south ? 1 : 0));
                    } else if (HoleUtility.validQuadBedrock(pos)) {
                        box = new Box(box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY, box.maxZ + 1);
                        color = bedrockColor.getValue().getColorObject();
                    } else if (HoleUtility.validQuadIndestructible(pos)) {
                        box = new Box(box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY, box.maxZ + 1);
                    } else {
                        continue;
                    }

                    boolean skip = false;
                    for (Box boxOffset : boxes) {
                        if (boxOffset.intersects(box))
                            skip = true;
                    }

                    if (skip)
                        continue;

                    blocks.add(new BoxWithColor(box, color));
                    boxes.add(box);
                }
            }
        }
        positions.clear();
        positions.addAll(blocks);
    }

    public record BoxWithColor(Box box, Color color) {
    }
}

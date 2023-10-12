package dev.thunderhack.modules.render;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.render.Render2DEngine;
import dev.thunderhack.utils.render.Render3DEngine;
import dev.thunderhack.utils.world.HoleUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoleESP extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.CubeOutline);
    private final Setting<Integer> rangeXZ = new Setting<>("Range XY", 10, 1, 128);
    private final Setting<Integer> rangeY = new Setting<>("Range Y", 5, 1, 128);

    private final Setting<ColorSetting> indestrictibleColor = new Setting<>("Indestructible", new ColorSetting(new Color(0x7A00FF).getRGB()));
    private final Setting<ColorSetting> bedrockColor = new Setting<>("Bedrock", new ColorSetting(new Color(0x00FF51).getRGB()));

    private final Setting<Float> height = new Setting<>("Height", 1f, 0.01f, 5f);
    private final Setting<Float> lineWith = new Setting<>("Line Width", 0.5f, 0.01f, 5f);

    private enum Mode {
        Fade,
        Fade2,
        CubeOutline,
        CubeFill,
        CubeBoth
    }

    private final Timer logicTimer = new Timer();
    private final List<BoxWithColor> positions = new CopyOnWriteArrayList<>();

    public HoleESP() {
        super("HoleESP", Category.RENDER);
    }

    @Override
    public void onDisable() {
        positions.clear();
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (positions.isEmpty()) return;

        for (BoxWithColor pwc : positions) {
            switch (mode.getValue()) {
                case Fade -> renderFade(pwc);
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

    public void renderFade(@NotNull HoleESP.BoxWithColor posWithColor) {
        Render3DEngine.FADE_QUEUE.add(
                new Render3DEngine.FadeAction(posWithColor.box, Render2DEngine.applyOpacity(posWithColor.color(), 60), Render2DEngine.applyOpacity(posWithColor.color(), 0))
        );
        Render3DEngine.OUTLINE_SIDE_QUEUE.add(
                new Render3DEngine.OutlineSideAction(posWithColor.box, posWithColor.color(), lineWith.getValue(), Direction.DOWN)
        );
    }


    public void renderFade2(@NotNull HoleESP.BoxWithColor boxWithColor, MatrixStack stack) {
        Render3DEngine.FADE_QUEUE.add(
                new Render3DEngine.FadeAction(boxWithColor.box, Render2DEngine.applyOpacity(boxWithColor.color(), 60), Render2DEngine.applyOpacity(boxWithColor.color(), 0)
                ));
        Render3DEngine.drawHoleOutline(
                boxWithColor.box, boxWithColor.color(), lineWith.getValue()
        );

        Render3DEngine.FILLED_QUEUE.add(
                new Render3DEngine.FillAction(new Box(boxWithColor.box.minX, boxWithColor.box.minY, boxWithColor.box.minZ,
                        boxWithColor.box.maxX, boxWithColor.box.minY + 0.01f, boxWithColor.box.maxZ), boxWithColor.color()
                )
        );
    }

    public void renderOutline(@NotNull HoleESP.BoxWithColor boxWithColor) {
        Render3DEngine.OUTLINE_QUEUE.add(
                new Render3DEngine.OutlineAction(boxWithColor.box, boxWithColor.color(), lineWith.getValue())
        );
    }

    public void renderFill(@NotNull HoleESP.BoxWithColor boxWithColor) {
        Render3DEngine.FILLED_QUEUE.add(
                new Render3DEngine.FillAction(boxWithColor.box(), boxWithColor.color())
        );
    }

    @Override
    public void onThread() {
        if (fullNullCheck() || !logicTimer.passedMs(500))
            return;

        findHoles();
        logicTimer.reset();
    }

    private void findHoles() {
        ArrayList<BoxWithColor> blocks = new ArrayList<>();
        if (mc.world == null || mc.player == null) {
            positions.clear();
            return;
        }
        BlockPos centerPos = BlockPos.ofFloored(mc.player.getPos());
        List<Box> boxes = new ArrayList<>();

        for (int i = centerPos.getX() - rangeXZ.getValue(); i < centerPos.getX() + rangeXZ.getValue(); i++) {
            for (int j = centerPos.getY() - rangeY.getValue(); j < centerPos.getY() + rangeY.getValue(); j++) {
                for (int k = centerPos.getZ() - rangeXZ.getValue(); k < centerPos.getZ() + rangeXZ.getValue(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + height.getValue(), pos.getZ() + 1);
                    Color color = indestrictibleColor.getValue().getColorObject();
                    if (HoleUtils.validIndestructible(pos)) {
                    } else if (HoleUtils.validBedrock(pos)) {
                        color = bedrockColor.getValue().getColorObject();
                    } else if (HoleUtils.validTwoBlockBedrockXZ(pos)) {
                        boolean east = mc.world.isAir(pos.offset(Direction.EAST));
                        boolean south = mc.world.isAir(pos.offset(Direction.SOUTH));
                        box = new Box(box.minX, box.minY, box.minZ, box.maxX + (east ? 1 : 0), box.maxY, box.maxZ + (south ? 1 : 0));
                        color = bedrockColor.getValue().getColorObject();
                    } else if (HoleUtils.validTwoBlockIndestructibleXZ(pos)) {
                        boolean east = mc.world.isAir(pos.offset(Direction.EAST));
                        boolean south = mc.world.isAir(pos.offset(Direction.SOUTH));
                        box = new Box(box.minX, box.minY, box.minZ, box.maxX + (east ? 1 : 0), box.maxY, box.maxZ + (south ? 1 : 0));
                    } else if (HoleUtils.validQuadBedrock(pos)) {
                        box = new Box(box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY, box.maxZ + 1);
                        color = bedrockColor.getValue().getColorObject();
                    } else if (HoleUtils.validQuadIndestructible(pos)) {
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

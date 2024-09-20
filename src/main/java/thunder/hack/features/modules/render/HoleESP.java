package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.HoleUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoleESP extends Module {
    public HoleESP() {
        super("HoleESP", Category.RENDER);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.CubeOutline);
    private final Setting<Integer> rangeXZ = new Setting<>("Range XY", 10, 1, 128);
    private final Setting<Integer> rangeY = new Setting<>("Range Y", 5, 1, 128);

    private final Setting<ColorSetting> indestrictibleColor = new Setting<>("Indestructible", new ColorSetting(new Color(0x7A00FF).getRGB()));
    private final Setting<ColorSetting> bedrockColor = new Setting<>("Bedrock", new ColorSetting(new Color(0x00FF51).getRGB()));

    private final Setting<Float> height = new Setting<>("Height", 1f, 0.01f, 5f);
    private final Setting<Float> lineWith = new Setting<>("Line Width", 0.5f, 0.01f, 5f);
    public final Setting<Boolean> culling = new Setting<>("Culling", true, v -> mode.getValue() == Mode.Fade || mode.getValue() == Mode.Fade2);

    private final Timer logicTimer = new Timer();
    private final List<BoxWithColor> positions = new CopyOnWriteArrayList<>();

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
                case Fade2 -> renderFade2(pwc);
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
                new Render3DEngine.FadeAction(posWithColor.box, getColor(posWithColor.box, posWithColor.color(), 60), getColor(posWithColor.box, posWithColor.color(), 0))
        );
        Render3DEngine.OUTLINE_SIDE_QUEUE.add(
                new Render3DEngine.OutlineSideAction(posWithColor.box, getColor(posWithColor.box, posWithColor.color(), posWithColor.color.getAlpha()), lineWith.getValue(), Direction.DOWN)
        );
    }

    public void renderFade2(@NotNull HoleESP.BoxWithColor boxWithColor) {
        Render3DEngine.FADE_QUEUE.add(
                new Render3DEngine.FadeAction(boxWithColor.box, getColor(boxWithColor.box, boxWithColor.color(), 60), getColor(boxWithColor.box, boxWithColor.color(), 0)
                ));

        Render3DEngine.drawHoleOutline(
                boxWithColor.box, getColor(boxWithColor.box, boxWithColor.color(), boxWithColor.color.getAlpha()), lineWith.getValue()
        );

        Render3DEngine.FILLED_QUEUE.add(
                new Render3DEngine.FillAction(new Box(boxWithColor.box.minX, boxWithColor.box.minY, boxWithColor.box.minZ,
                        boxWithColor.box.maxX, boxWithColor.box.minY + 0.01f, boxWithColor.box.maxZ), getColor(boxWithColor.box, boxWithColor.color(), boxWithColor.color.getAlpha())
                )
        );
    }

    private Color getColor(Box box, Color color, int alpha) {
        float dist = PlayerUtility.squaredDistance2d(box.getCenter().getX(), box.getCenter().getZ());
        float factor = dist / (rangeXZ.getPow2Value());

        factor = 1f - easeOutExpo(factor);

        factor = MathUtility.clamp(factor, 0f, 1f);

        return Render2DEngine.injectAlpha(color, (int) (factor * alpha));
    }

    private float easeOutExpo(float x) {
        return x == 1f ? 1f : (float) (1f - Math.pow(2f, -10f * x));
    }

    public void renderOutline(@NotNull HoleESP.BoxWithColor boxWithColor) {
        Render3DEngine.OUTLINE_QUEUE.add(
                new Render3DEngine.OutlineAction(boxWithColor.box, getColor(boxWithColor.box, boxWithColor.color(), boxWithColor.color.getAlpha()), lineWith.getValue())
        );
    }

    public void renderFill(@NotNull HoleESP.BoxWithColor boxWithColor) {
        Render3DEngine.FILLED_QUEUE.add(
                new Render3DEngine.FillAction(boxWithColor.box(), getColor(boxWithColor.box, boxWithColor.color(), boxWithColor.color.getAlpha()))
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
                    if (HoleUtility.validIndestructible(pos)) {

                    } else if (HoleUtility.validBedrock(pos)) {
                        color = bedrockColor.getValue().getColorObject();
                    } else if (HoleUtility.validTwoBlockBedrock(pos)) {
                        boolean east = mc.world.isAir(pos.offset(Direction.EAST));
                        boolean south = mc.world.isAir(pos.offset(Direction.SOUTH));
                        box = new Box(box.minX, box.minY, box.minZ, box.maxX + (east ? 1 : 0), box.maxY, box.maxZ + (south ? 1 : 0));
                        color = bedrockColor.getValue().getColorObject();
                    } else if (HoleUtility.validTwoBlockIndestructible(pos)) {
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

    private enum Mode {
        Fade,
        Fade2,
        CubeOutline,
        CubeFill,
        CubeBoth
    }
}

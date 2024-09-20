package thunder.hack.features.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TunnelEsp extends Module {
    public TunnelEsp() {
        super("TunnelEsp", Category.RENDER);
    }

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(0xAE8A8AF6, true)));
    public Setting<Boolean> box = new Setting<>("Box", true);
    public Setting<Boolean> outline = new Setting<>("Outline", true);
    List<Box> renderBoxes = new ArrayList<>();
    private Timer delayTimer = new Timer();

    public void onRender3D(MatrixStack stack) {
        try {
            for (Box box_ : renderBoxes) {
                // рандомные генерации
                if (box_.getLengthZ() < 5 && box_.getLengthX() < 5)
                    continue;

                if (box.getValue()) Render3DEngine.drawFilledBox(stack, box_, color.getValue().getColorObject());
                if (outline.getValue())
                    Render3DEngine.drawBoxOutline(box_, Render2DEngine.injectAlpha(color.getValue().getColorObject(), 255), 2);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onEnable() {
        renderBoxes.clear();
    }

    @Override
    public void onThread() {
        if (delayTimer.passedMs(2000)) {
            for (int x = (int) (mc.player.getX() - 128); x < mc.player.getX() + 128; ++x) {
                for (int z = (int) (mc.player.getZ() - 128); z < mc.player.getZ() + 128; ++z) {
                    for (int y = 0; y < 121; ++y) {
                        BlockPos bp = new BlockPos(x, y, z);

                        if (one_two(bp) && !alreadyIn(new Box(bp.getX(), bp.getY(), bp.getZ(), bp.getX() + 1, bp.getY() + 2, bp.getZ() + 1))) {
                            Box renderBox = new Box(bp.getX(), bp.getY(), bp.getZ(), bp.getX() + 1, bp.getY() + 2, bp.getZ() + 1);
                            renderBoxes.add(getFullBox(renderBox, x, y, z, 1));
                        }

                        if (one_one(bp) && !alreadyIn(new Box(bp))) {
                            Box renderBox = new Box(bp);
                            renderBoxes.add(getFullBox(renderBox, x, y, z, 0));
                        }

                        /*
                        if (one_three(bp) && !alreadyIn(new Box(bp.getX(), bp.getY(), bp.getZ(), bp.getX() + 1, bp.getY() + 3, bp.getZ() + 1))) {
                            Box renderBox = new Box(bp.getX(), bp.getY(), bp.getZ(), bp.getX() + 1, bp.getY() + 3, bp.getZ() + 1);
                            renderBoxes.add(getFullBox(renderBox, x, y, z, 2));
                        }
                         */
                    }
                }
            }
            delayTimer.reset();
        }
    }

    private Box getFullBox(Box raw, int x, int y, int z, int mode) {
        BlockPos checkBp1 = new BlockPos(x, y, z + 1);

        Function<BlockPos, Boolean> check = getCheckByMode(mode);

        while (check.apply(checkBp1)) {
            raw = raw.withMaxZ(raw.maxZ + 1);
            checkBp1 = checkBp1.south();
        }

        BlockPos checkBp2 = new BlockPos(x + 1, y, z);
        while (check.apply(checkBp2)) {
            raw = raw.withMaxX(raw.maxX + 1);
            checkBp2 = checkBp2.east();
        }

        BlockPos checkBp3 = new BlockPos(x, y, z - 1);
        while (check.apply(checkBp3)) {
            raw = raw.withMinZ(raw.minZ - 1);
            checkBp3 = checkBp3.north();
        }

        BlockPos checkBp4 = new BlockPos(x - 1, y, z);
        while (check.apply(checkBp4)) {
            raw = raw.withMinX(raw.minX - 1);
            checkBp4 = checkBp4.west();
        }

        return raw;
    }

    private Function<BlockPos, Boolean> getCheckByMode(int mode) {
        return switch (mode) {
            case 1 -> TunnelEsp::one_two;
            case 2 -> TunnelEsp::one_three;
            default -> TunnelEsp::one_one;
        };
    }

    private boolean alreadyIn(Box box) {
        for (Box box2 : renderBoxes) {
            if (box.intersects(box2))
                return true;
        }
        return false;
    }

    //1 x 2 check
    private static boolean one_three(BlockPos pos) {
        if (!isAir(pos) || !isAir(pos.up()) || !isAir(pos.up().up())) return false;
        if (isAir(pos.down()) || isAir(pos.up().up().up())) return false;

        if (isAir(pos.up().north()) && isAir(pos.up().south()))
            return !isAir(pos.up().east()) && !isAir(pos.up().west());

        if (isAir(pos.up().east()) && isAir(pos.up().west()))
            return !isAir(pos.up().north()) && !isAir(pos.up().south());

        return false;
    }

    //1 x 2 check
    private static boolean one_two(BlockPos pos) {
        if (!isAir(pos) || !isAir(pos.up())) return false;
        if (isAir(pos.down()) || isAir(pos.up().up())) return false;

        if (isAir(pos.north()) && isAir(pos.south()) && isAir(pos.up().north()) && isAir(pos.up().south()))
            return !isAir(pos.east()) && !isAir(pos.west()) && !isAir(pos.up().east()) && !isAir(pos.up().west());

        if (isAir(pos.east()) && isAir(pos.west()) && isAir(pos.up().east()) && isAir(pos.up().west()))
            return !isAir(pos.north()) && !isAir(pos.south()) && !isAir(pos.up().north()) && !isAir(pos.up().south());

        return false;
    }

    //1 x 1 check
    private static boolean one_one(BlockPos pos) {
        if (!isAir(pos)) return false;
        if (isAir(pos.down()) || isAir(pos.up())) return false;

        if (isAir(pos.north()) && isAir(pos.south()))
            return !isAir(pos.east()) && !isAir(pos.west()) && !isAir(pos.up().east()) && !isAir(pos.up().west());

        if (isAir(pos.east()) && isAir(pos.west()))
            return !isAir(pos.north()) && !isAir(pos.south());

        return false;
    }


    private static boolean isAir(BlockPos bp) {
        return mc.world.isAir(bp);
    }
}

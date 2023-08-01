package thunder.hack.modules.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.Render3DEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoleEsp extends Module {

    public HoleEsp() {
        super("HoleEsp", Category.RENDER);
    }

    private List<PosWithColor> positions = new CopyOnWriteArrayList<>();


    private final Setting<Mode> mode = new Setting("Mode", Mode.CubeOutline);
    private Setting<Integer> rangeXZ = new Setting<>("Range XY", 10, 1, 128);
    private Setting<Integer> rangeY = new Setting<>("Range Y", 5, 1, 128);

    public final Setting<ColorSetting> obbyColor1 = new Setting<>("Obby Color", new ColorSetting(new Color(0x7A00FF).getRGB()));
    public final Setting<ColorSetting> bedrockColor1 = new Setting<>("Bedrock Color", new ColorSetting(new Color(0x00FF51).getRGB()));

    private Setting<Float> height = new Setting<>("Height", 1f, 0.01f, 5f);
    private Setting<Float> lineWith = new Setting<>("Line With", 0.5f, 0.01f, 5f);


    private enum Mode {
        Fade, CubeOutline, CubeFill, CubeBoth
    }


    @Subscribe
    public void onRender3D(Render3DEvent e) {
        if (positions.isEmpty()) return;
        for (PosWithColor posWithColor : positions) {
            if (mode.getValue() == Mode.CubeOutline || mode.getValue() == Mode.CubeBoth) {
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
            if (mode.getValue() == Mode.CubeFill || mode.getValue() == Mode.CubeBoth) {
                Render3DEngine.drawFilledBox(e.getMatrixStack(),
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

            if (mode.getValue() == Mode.Fade) {
                RenderSystem.disableCull();
                Render3DEngine.drawFilledFadeBox(e.getMatrixStack(),
                        new Box(
                                posWithColor.getBp().getX(),
                                posWithColor.getBp().getY(),
                                posWithColor.getBp().getZ(),
                                posWithColor.getBp().getX() + (posWithColor.checkDirX() ? 2f : 1f),
                                posWithColor.getBp().getY() + height.getValue(),
                                posWithColor.getBp().getZ() + (posWithColor.checkDirZ() ? 2f : 1f)
                        ), Render2DEngine.injectAlpha(posWithColor.getColor(), 60), Render2DEngine.injectAlpha(posWithColor.getColor(), 0)
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
        }
    }

    @Override
    public void onThread() {
        if (fullNullCheck()) return;
        //   long timer1 = System.currentTimeMillis();
        findHoles();
        //  Command.sendMessage(System.currentTimeMillis() - timer1 + " ");
    }

    private void findHoles() {
        ArrayList<PosWithColor> bloks = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();
        for (int i = centerPos.getX() - rangeXZ.getValue(); i < centerPos.getX() + rangeXZ.getValue(); i++) {
            for (int j = centerPos.getY() - rangeY.getValue(); j < centerPos.getY() + rangeY.getValue(); j++) {
                for (int k = centerPos.getZ() - rangeXZ.getValue(); k < centerPos.getZ() + rangeXZ.getValue(); k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (validObi(pos)) {
                        bloks.add(new PosWithColor(pos, false, false, obbyColor1.getValue().getColorObject()));
                    } else if (validBedrock(pos)) {
                        bloks.add(new PosWithColor(pos, false, false, bedrockColor1.getValue().getColorObject()));
                    } else if (validTwoBlockBedrockXZ(pos)) {
                        bloks.add(new PosWithColor(pos, true, false, bedrockColor1.getValue().getColorObject()));
                    } else if (validTwoBlockObiXZ(pos)) {
                        bloks.add(new PosWithColor(pos, true, false, obbyColor1.getValue().getColorObject()));
                    } else if (validTwoBlockBedrockXZ1(pos)) {
                        bloks.add(new PosWithColor(pos, false, true, bedrockColor1.getValue().getColorObject()));
                    } else if (validTwoBlockObiXZ1(pos)) {
                        bloks.add(new PosWithColor(pos, false, true, obbyColor1.getValue().getColorObject()));
                    } else if (validQuadBedrock(pos)) {
                        bloks.add(new PosWithColor(pos, true, true, bedrockColor1.getValue().getColorObject()));
                    } else if (validQuadObby(pos)) {
                        bloks.add(new PosWithColor(pos, true, true, obbyColor1.getValue().getColorObject()));
                    }
                }
            }
        }
        positions.clear();
        positions.addAll(bloks);
    }

    public static boolean validObi(BlockPos pos) {
        return !validBedrock(pos)
                && (isObby(pos.add(0, -1, 0)) || isBedrock(pos.add(0, -1, 0)))
                && (isObby(pos.add(1, 0, 0)) || isBedrock(pos.add(1, 0, 0)))
                && (isObby(pos.add(-1, 0, 0)) || isBedrock(pos.add(-1, 0, 0)))
                && (isObby(pos.add(0, 0, 1)) || isBedrock(pos.add(0, 0, 1)))
                && (isObby(pos.add(0, 0, -1)) || isBedrock(pos.add(0, 0, -1)))
                && isAir(pos)
                && isAir(pos.add(0, 1, 0))
                && isAir(pos.add(0, 2, 0));
    }

    public static boolean validBedrock(BlockPos pos) {
        return isBedrock(pos.add(0, -1, 0))
                && isBedrock(pos.add(1, 0, 0))
                && isBedrock(pos.add(-1, 0, 0))
                && isBedrock(pos.add(0, 0, 1))
                && isBedrock(pos.add(0, 0, -1))
                && isAir(pos)
                && isAir(pos.add(0, 1, 0))
                && isAir(pos.add(0, 2, 0));
    }

    public static boolean validTwoBlockObiXZ(BlockPos pos) {
        if (
                (isObby(pos.down()) || isBedrock(pos.down()))
                        && (isObby(pos.west()) || isBedrock(pos.west()))
                        && (isObby(pos.south()) || isBedrock(pos.south()))
                        && (isObby(pos.north()) || isBedrock(pos.north()))
                        && isAir(pos)
                        && isAir(pos.up())
                        && isAir(pos.up(2))
                        && (isObby(pos.east().down()) || isBedrock(pos.east().down()))
                        && (isObby(pos.east(2)) || isBedrock(pos.east(2)))
                        && (isObby(pos.east().south()) || isBedrock(pos.east().south()))
                        && (isObby(pos.east().north()) || isBedrock(pos.east().north()))
                        && isAir(pos.east())
                        && isAir(pos.east().up())
                        && isAir(pos.east().up(2))
        ) {
            return true;
        }
        return false;
    }

    public static boolean validTwoBlockObiXZ1(BlockPos pos) {
        if (
                (isObby(pos.down()) || isBedrock(pos.down()))
                        && (isObby(pos.west()) || isBedrock(pos.west()))
                        && (isObby(pos.east()) || isBedrock(pos.east()))
                        && (isObby(pos.north()) || isBedrock(pos.north()))
                        && isAir(pos)
                        && isAir(pos.up())
                        && isAir(pos.up(2))
                        && (isObby(pos.south().down()) || isBedrock(pos.south().down()))
                        && (isObby(pos.south(2)) || isBedrock(pos.south(2)))
                        && (isObby(pos.south().east()) || isBedrock(pos.south().east()))
                        && (isObby(pos.south().west()) || isBedrock(pos.south().west()))
                        && isAir(pos.south())
                        && isAir(pos.south().up())
                        && isAir(pos.south().up(2))
        ) {
            return true;
        }
        return false;
    }

    public static boolean validQuadObby(BlockPos pos) {
        if (
                ((isObby(pos.down()) || isBedrock(pos.down())) && (isAir(pos)) && isAir(pos.up()) && isAir(pos.up(2)))
                        && ((isObby(pos.south().down()) || isBedrock(pos.south().down())) && (isAir(pos.south())) && isAir(pos.south().up()) && isAir(pos.south().up(2)))
                        && ((isObby(pos.east().down()) || isBedrock(pos.east().down())) && (isAir(pos.east())) && isAir(pos.east().up()) && isAir(pos.east().up(2)))
                        && ((isObby(pos.south().east().down()) || isBedrock(pos.south().east().down())) && (isAir(pos.south().east())) && isAir(pos.south().east().up()) && isAir(pos.south().east().up(2)))

                        && ((isObby(pos.north()) || isBedrock(pos.north())) && (isObby(pos.west()) || isBedrock(pos.west())))
                        && ((isObby(pos.east().north()) || isBedrock(pos.east().north())) && (isObby(pos.east().east()) || isBedrock(pos.east().east())))
                        && ((isObby(pos.south().west()) || isBedrock(pos.south().west())) && (isObby(pos.south().south()) || isBedrock(pos.south().south())))
                        && ((isObby(pos.east().south().south()) || isBedrock(pos.east().south().south())) && (isObby(pos.east().south().east()) || isBedrock(pos.east().south().east())))
        ) return true;

        return false;
    }

    public static boolean validQuadBedrock(BlockPos pos) {
        if (
                ((isBedrock(pos.down())) && (isAir(pos)) && isAir(pos.up()) && isAir(pos.up(2)))
                        && ((isBedrock(pos.south().down())) && (isAir(pos.south())) && isAir(pos.south().up()) && isAir(pos.south().up(2)))
                        && ((isBedrock(pos.east().down())) && (isAir(pos.east())) && isAir(pos.east().up()) && isAir(pos.east().up(2)))
                        && ((isBedrock(pos.south().east().down())) && (isAir(pos.south().east())) && isAir(pos.south().east().up()) && isAir(pos.south().east().up(2)))

                        && (isBedrock(pos.north()) && isBedrock(pos.west()))
                        && (isBedrock(pos.east().north()) && isBedrock(pos.east().east()))
                        && (isBedrock(pos.south().west()) && isBedrock(pos.south().south()))
                        && (isBedrock(pos.east().south().south()) && isBedrock(pos.east().south().east()))

        ) return true;

        return false;
    }

    public static boolean validTwoBlockBedrockXZ(BlockPos pos) {
        if (
                (isBedrock(pos.down()))
                        && (isBedrock(pos.west()))
                        && (isBedrock(pos.south()))
                        && (isBedrock(pos.north()))
                        && isAir(pos)
                        && isAir(pos.up())
                        && isAir(pos.up(2))
                        && (isBedrock(pos.east().down()))
                        && (isBedrock(pos.east(2)))
                        && (isBedrock(pos.east().south()))
                        && (isBedrock(pos.east().north()))
                        && isAir(pos.east())
                        && isAir(pos.east().up())
                        && isAir(pos.east().up(2))
        ) {
            return true;
        }
        return false;
    }

    public static boolean validTwoBlockBedrockXZ1(BlockPos pos) {
        if (
                (isBedrock(pos.down()))
                        && (isBedrock(pos.west()))
                        && (isBedrock(pos.east()))
                        && (isBedrock(pos.north()))
                        && isAir(pos)
                        && isAir(pos.up())
                        && isAir(pos.up(2))
                        && (isBedrock(pos.south().down()))
                        && (isBedrock(pos.south(2)))
                        && (isBedrock(pos.south().east()))
                        && (isBedrock(pos.south().west()))
                        && isAir(pos.south())
                        && isAir(pos.south().up())
                        && isAir(pos.south().up(2))
        ) {
            return true;
        }
        return false;
    }

    private static boolean isObby(BlockPos bp) {
        return mc.world.getBlockState(bp).getBlock() == Blocks.OBSIDIAN;
    }

    private static boolean isBedrock(BlockPos bp) {
        return mc.world.getBlockState(bp).getBlock() == Blocks.BEDROCK;
    }

    private static boolean isAir(BlockPos bp) {
        return mc.world.getBlockState(bp).getBlock() == Blocks.AIR;
    }

    public class PosWithColor {
        private BlockPos bp;
        private boolean dirX;
        private boolean dirZ;
        private Color color;

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

package thunder.hack.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.cmd.Command;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.modules.client.MainSettings.isRu;

public class TunnelEsp extends Module {
    public TunnelEsp() {
        super("TunnelEsp", Category.RENDER);
    }

    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(0xAE8A8AF6, true)));
    public Setting<Boolean> box = new Setting<>("Box", true);
    public Setting<Boolean> outline = new Setting<>("Outline", true);
    List<BlockPos> tunnelbp = new ArrayList<>();
    private Timer delayTimer = new Timer();

    public void onRender3D(MatrixStack stack) {
        try {
            for (BlockPos bp : tunnelbp) {
                if (box.getValue()) Render3DEngine.drawFilledBox(stack, new Box(bp), color.getValue().getColorObject());
                if (outline.getValue()) Render3DEngine.drawBoxOutline(new Box(bp), Render2DEngine.injectAlpha(color.getValue().getColorObject(), 255), 2);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onThread() {
        if (delayTimer.passedMs(2000)) {
            for (int x = (int) (mc.player.getX() - 128); x < mc.player.getX() + 128; ++x) {
                for (int z = (int) (mc.player.getZ() - 128); z < mc.player.getZ() + 128; ++z) {
                    for (int y = 0; y < 121; ++y) {
                        if ((one_two(new BlockPos(x, y, z)) && one_two(new BlockPos(x + 1, y, z)) && one_two(new BlockPos(x - 1, y, z)) && one_two(new BlockPos(x + 2, y, z)) && one_two(new BlockPos(x - 2, y, z)))) {
                            tunnelbp.add(new BlockPos(x, y, z));
                            tunnelbp.add(new BlockPos(x, y + 1, z));

                            tunnelbp.add(new BlockPos(x + 1, y, z));
                            tunnelbp.add(new BlockPos(x + 1, y + 1, z));

                            tunnelbp.add(new BlockPos(x - 1, y, z));
                            tunnelbp.add(new BlockPos(x - 1, y + 1, z));

                            tunnelbp.add(new BlockPos(x + 2, y, z));
                            tunnelbp.add(new BlockPos(x + 2, y + 1, z));

                            tunnelbp.add(new BlockPos(x - 2, y, z));
                            tunnelbp.add(new BlockPos(x - 2, y + 1, z));
                            Command.sendMessage(isRu() ? "Нашёл тоннель!" : "Tunnel found!");
                        }
                        if ((one_two(new BlockPos(x, y, z)) && one_two(new BlockPos(x, y, z + 1)) && one_two(new BlockPos(x, y, z - 1)) && one_two(new BlockPos(x, y, z + 2)) && one_two(new BlockPos(x, y, z - 2)))) {
                            tunnelbp.add(new BlockPos(x, y, z));
                            tunnelbp.add(new BlockPos(x, y + 1, z));

                            tunnelbp.add(new BlockPos(x, y, z + 1));
                            tunnelbp.add(new BlockPos(x, y + 1, z + 1));

                            tunnelbp.add(new BlockPos(x, y, z - 1));
                            tunnelbp.add(new BlockPos(x, y + 1, z - 1));

                            tunnelbp.add(new BlockPos(x, y, z + 2));
                            tunnelbp.add(new BlockPos(x, y + 1, z + 2));

                            tunnelbp.add(new BlockPos(x, y, z - 2));
                            tunnelbp.add(new BlockPos(x, y + 1, z - 2));
                            Command.sendMessage(isRu() ? "Нашёл тоннель!" : "Tunnel found!");
                        }
                    }
                }
            }
            delayTimer.reset();
        }
    }

    //1 x 2 check
    private boolean one_two(BlockPos pos) {
        if (tunnelbp.contains(pos)) return false;
        if (!mc.world.isAir(pos) || !mc.world.isAir(pos.up())) return false;
        if (mc.world.isAir(pos.down()) || mc.world.isAir(pos.up().up())) return false;
        if (mc.world.isAir(pos.north()) && mc.world.isAir(pos.south()) && mc.world.isAir(pos.up().north()) && mc.world.isAir(pos.up().south())) {
            return !mc.world.isAir(pos.east()) && !mc.world.isAir(pos.west()) && !mc.world.isAir(pos.up().east()) && !mc.world.isAir(pos.up().west());
        }
        if (mc.world.isAir(pos.east()) && mc.world.isAir(pos.west()) && mc.world.isAir(pos.up().east()) && mc.world.isAir(pos.up().west())) {
            return !mc.world.isAir(pos.north()) && !mc.world.isAir(pos.south()) && !mc.world.isAir(pos.up().north()) && !mc.world.isAir(pos.up().south());
        }
        return false;
    }

    /*1 x 1 check
    private boolean one_one(BlockPos pos) {
        if (tunnelbp.contains(pos)) return false;
        if (!mc.world.isAir(pos)) return false;
        if (mc.world.isAir(pos.down()) || mc.world.isAir(pos.up())) return false;
        if (mc.world.isAir(pos.north()) && mc.world.isAir(pos.south()) && mc.world.isAir(pos.up().north()) && mc.world.isAir(pos.up().south())) {
            return !mc.world.isAir(pos.east()) && !mc.world.isAir(pos.west()) && !mc.world.isAir(pos.up().east()) && !mc.world.isAir(pos.up().west());
        }
        if (mc.world.isAir(pos.east()) && mc.world.isAir(pos.west()) && mc.world.isAir(pos.up().east()) && mc.world.isAir(pos.up().west())) {
            return !mc.world.isAir(pos.north()) && !mc.world.isAir(pos.south()) && !mc.world.isAir(pos.up().north()) && !mc.world.isAir(pos.up().south());
        }
        return false;
    }*/
}

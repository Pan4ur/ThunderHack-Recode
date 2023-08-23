package thunder.hack.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;

public class XRay extends Module {
    public static int done;
    public static int all;
    public Setting<Boolean> brutForce = new Setting<>("OreDeobf", false);
    public Setting<Integer> checkSpeed = new Setting<>("CheckSpeed", 4, 1, 5, v -> brutForce.getValue());
    public Setting<Integer> rxz = new Setting<>("RadiusXZ", 20, 5, 200, v -> brutForce.getValue());
    public Setting<Integer> ry = new Setting<>("RadiusY", 6, 2, 50, v -> brutForce.getValue());
    public static Setting<Boolean> netherite = new Setting<>("Netherite", false);
    public static Setting<Boolean> diamond = new Setting<>("Diamond ", false);
    public static Setting<Boolean> gold = new Setting<>("Gold", false);
    public static Setting<Boolean> iron = new Setting<>("Iron", false);
    public static Setting<Boolean> emerald = new Setting<>("Emerald", false);
    public static Setting<Boolean> redstone = new Setting<>("Redstone", false);
    public static Setting<Boolean> lapis = new Setting<>("Lapis", false);
    public static Setting<Boolean> coal = new Setting<>("Coal", false);
    public static Setting<Boolean> water = new Setting<>("Water", false);
    public static Setting<Boolean> lava = new Setting<>("Lava", false);
    
    ArrayList<BlockPos> ores = new ArrayList<>();
    ArrayList<BlockPos> toCheck = new ArrayList<>();
    BlockPos displayBlock;

    public XRay() {
        super("XRay", "Искать алмазы на ezzzzz", Category.MISC);
    }

    @Override
    public void onEnable() {
        ores.clear();
        toCheck.clear();
        for (BlockPos pos : getBlocks()) {
            if(mc.world.isAir(pos)) continue;
            toCheck.add(pos);
        }
        all = toCheck.size();
        done = 0;
        mc.worldRenderer.reload();
    }

    @Override
    public void onDisable() {
        mc.worldRenderer.reload();
    }

    @Override
    public void onUpdate() {
        if(!brutForce.getValue()) return;
        for (int i = 0; i < checkSpeed.getValue(); ++i) {
            if (toCheck.size() < 1) {
                return;
            }
            BlockPos pos = toCheck.remove(0);
            ++done;
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
            displayBlock = pos;
        }
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive e) {
        if (e.getPacket() instanceof BlockUpdateS2CPacket pac) {
            if (isCheckableOre(pac.getState().getBlock())) {
                ores.add(pac.getPos());
            }
        }
    }


    public void onRender3D(MatrixStack stack) {
        try {
            for (BlockPos pos : ores) {
                Block block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.DIAMOND_ORE && diamond.getValue()) {
                        Render3DEngine.drawFilledBox(stack,new Box(pos), new Color(0, 255, 255, 100));
                        Render3DEngine.drawBoxOutline(new Box(pos), new Color(0, 255, 255, 200), 2);
                    }
                    if (block == Blocks.GOLD_ORE && gold.getValue()) {
                        Render3DEngine.drawFilledBox(stack,new Box(pos), new Color(255, 215, 0, 100));
                        Render3DEngine.drawBoxOutline(new Box(pos), new Color(255, 215, 0, 200), 2);
                    }
                    if (block == Blocks.IRON_ORE  && iron.getValue()) {
                        Render3DEngine.drawFilledBox(stack,new Box(pos), new Color(213, 213, 213, 100));
                        Render3DEngine.drawBoxOutline(new Box(pos), new Color(213, 213, 213, 200), 2);
                    }
                    if (block == Blocks.EMERALD_ORE  && emerald.getValue()) {
                        Render3DEngine.drawFilledBox(stack,new Box(pos), new Color(0, 255, 77, 100));
                        Render3DEngine.drawBoxOutline(new Box(pos), new Color(0, 255, 77, 200), 2);
                    }
                    if (block == Blocks.REDSTONE_ORE  && redstone.getValue()) {
                        Render3DEngine.drawFilledBox(stack,new Box(pos), new Color(255, 0, 0, 100));
                        Render3DEngine.drawBoxOutline(new Box(pos), new Color(255, 0, 0, 200), 2);
                    }
                    if (block == Blocks.COAL_ORE  && coal.getValue()) {
                        Render3DEngine.drawFilledBox(stack,new Box(pos), new Color(0, 0, 0, 100));
                        Render3DEngine.drawBoxOutline(new Box(pos), new Color(0, 0, 0, 200), 2);
                    }
                    if (block == Blocks.LAPIS_ORE  && lapis.getValue() ) {
                        Render3DEngine.drawFilledBox(stack,new Box(pos), new Color(38, 97, 156, 100));
                        Render3DEngine.drawBoxOutline(new Box(pos), new Color(38, 97, 156, 200), 2);
                    }
                    if (block == Blocks.ANCIENT_DEBRIS  && netherite.getValue() ) {
                        Render3DEngine.drawFilledBox(stack,new Box(pos), new Color(255, 255, 255, 100));
                        Render3DEngine.drawBoxOutline(new Box(pos), new Color(255, 255, 255, 200), 2);
                    }
            }
            if (displayBlock != null && (done != all)) {
                Render3DEngine.drawFilledBox(stack,new Box(displayBlock), new Color(255, 0, 30));
                Render3DEngine.drawBoxOutline(new Box(displayBlock), new Color(255, 0, 60), 2);
            }
        } catch (Exception ignored) {}
    }


    public void onRender2D(DrawContext context) {
        FontRenderers.modules.drawCenteredString(context.getMatrices(),"Done: " + done + " / " + "All: " + all,mc.getWindow().getScaledWidth() / 2f, 50,-1);
    }

    public static boolean isCheckableOre(Block block) {
        if (diamond.getValue() && block == Blocks.DIAMOND_ORE)
            return true;

        if (gold.getValue() && block == Blocks.GOLD_ORE)
            return true;

        if (iron.getValue() && block == Blocks.IRON_ORE)
            return true;

        if (emerald.getValue() && block == Blocks.EMERALD_ORE)
            return true;

        if (redstone.getValue() && block == Blocks.REDSTONE_ORE)
            return true;

        if (coal.getValue() && block == Blocks.COAL_ORE)
            return true;

        if (netherite.getValue() && block == Blocks.ANCIENT_DEBRIS)
            return true;

        if (water.getValue() && block == Blocks.WATER)
            return true;

        if (lava.getValue() && block == Blocks.LAVA)
            return true;

        return lapis.getValue() && block == Blocks.LAPIS_ORE;
    }

    private ArrayList<BlockPos> getBlocks() {
        ArrayList<BlockPos> positions = new ArrayList<>();
        for(int x = (int) (mc.player.getX() - rxz.getValue()); x < mc.player.getX() + rxz.getValue(); x++)
            for(int y = (int) (mc.player.getY() - ry.getValue()); y < mc.player.getY() + ry.getValue(); y++)
                for(int z = (int) (mc.player.getZ() - rxz.getValue()); z < mc.player.getZ() + rxz.getValue(); z++)
                    positions.add(new BlockPos(x,y,z));
        return positions;
    }
}

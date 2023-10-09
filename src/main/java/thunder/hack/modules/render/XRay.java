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
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;

public class XRay extends Module {
    private final Setting<Boolean> brutForce = new Setting<>("OreDeobf", false);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false, v -> brutForce.getValue());
    private final Setting<Boolean> fast = new Setting<>("Fast", false, v -> brutForce.getValue());
    private final Setting<Integer> checkSpeed = new Setting<>("CheckSpeed", 4, 1, 5, v -> brutForce.getValue());
    private final Setting<Integer> rxz = new Setting<>("RadiusXZ", 5, 5, 64, v -> brutForce.getValue());
    private final Setting<Integer> ry = new Setting<>("RadiusY", 5, 2, 50, v -> brutForce.getValue());
    private static final Setting<Boolean> netherite = new Setting<>("Netherite", false);
    private static final Setting<Boolean> diamond = new Setting<>("Diamond ", false);
    private static final Setting<Boolean> gold = new Setting<>("Gold", false);
    private static final Setting<Boolean> iron = new Setting<>("Iron", false);
    private static final Setting<Boolean> emerald = new Setting<>("Emerald", false);
    private static final Setting<Boolean> redstone = new Setting<>("Redstone", false);
    private static final Setting<Boolean> lapis = new Setting<>("Lapis", false);
    private static final Setting<Boolean> coal = new Setting<>("Coal", false);
    private static final Setting<Boolean> water = new Setting<>("Water", false);
    private static final Setting<Boolean> lava = new Setting<>("Lava", false);

    private final ArrayList<BlockPos> ores = new ArrayList<>();
    private final ArrayList<BlockPos> toCheck = new ArrayList<>();
    private BlockPos displayBlock;
    private int done;
    private int all;

    public XRay() {
        super("XRay", "Искать алмазы на ezzzzz", Category.MISC);
    }

    @Override
    public void onEnable() {
        ores.clear();
        toCheck.clear();
        for (BlockPos pos : getBlocks()) {
            if (mc.world.isAir(pos)) continue;

            if (fast.getValue()) if (pos.getX() % 2 == 0 || pos.getZ() % 2 == 0 || pos.getY() % 2 == 0) continue;

            toCheck.add(pos);
        }
        all = toCheck.size();
        done = 0;
        mc.chunkCullingEnabled = false;
        mc.worldRenderer.reload();
    }


    @Override
    public void onDisable() {
        mc.worldRenderer.reload();
        mc.chunkCullingEnabled = true;

    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!brutForce.getValue()) return;
        if (toCheck.isEmpty()) return;
        if (!rotate.getValue()) return;

        BlockPos pos = toCheck.get(0);
        InteractionUtility.BreakData bdata = InteractionUtility.getBreakData(pos, InteractionUtility.Interact.Strict);
        if (bdata != null) {
            float[] angle = InteractionUtility.calculateAngle(bdata.vector());
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (!brutForce.getValue()) return;
        for (int i = 0; i < checkSpeed.getValue(); ++i) {
            if (toCheck.isEmpty()) return;
            BlockPos pos = toCheck.remove(0);
            ++done;
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
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
                if (block == Blocks.DIAMOND_ORE && diamond.getValue()) draw(stack, pos, 0, 255, 255);
                if (block == Blocks.GOLD_ORE && gold.getValue()) draw(stack, pos, 255, 215, 0);
                if (block == Blocks.IRON_ORE && iron.getValue()) draw(stack, pos, 213, 213, 213);
                if (block == Blocks.EMERALD_ORE && emerald.getValue()) draw(stack, pos, 0, 255, 77);
                if (block == Blocks.REDSTONE_ORE && redstone.getValue()) draw(stack, pos, 255, 0, 0);
                if (block == Blocks.COAL_ORE && coal.getValue()) draw(stack, pos, 0, 0, 0);
                if (block == Blocks.LAPIS_ORE && lapis.getValue()) draw(stack, pos, 38, 97, 156);
                if (block == Blocks.ANCIENT_DEBRIS && netherite.getValue()) draw(stack, pos, 255, 255, 255);
            }
            if (displayBlock != null && (done != all)) draw(stack, displayBlock, 255, 0, 60);
        } catch (Exception ignored) {
        }
    }

    private void draw(MatrixStack stack, BlockPos pos, int r, int g, int b) {
        Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(r, g, b, 100));
        Render3DEngine.drawBoxOutline(new Box(pos), new Color(r, g, b, 200), 2);
    }

    public void onRender2D(DrawContext context) {
        if (brutForce.getValue())
            FontRenderers.modules.drawCenteredString(context.getMatrices(), "Done: " + done + " / " + "All: " + all, mc.getWindow().getScaledWidth() / 2f, 50, -1);
    }

    public static boolean isCheckableOre(Block block) {
        if (diamond.getValue() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return true;
        if (gold.getValue() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE)) return true;
        if (iron.getValue() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) return true;
        if (emerald.getValue() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return true;
        if (redstone.getValue() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE))
            return true;
        if (coal.getValue() && (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)) return true;
        if (netherite.getValue() && block == Blocks.ANCIENT_DEBRIS) return true;
        if (water.getValue() && block == Blocks.WATER) return true;
        if (lava.getValue() && block == Blocks.LAVA) return true;

        return lapis.getValue() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE);
    }

    private ArrayList<BlockPos> getBlocks() {
        ArrayList<BlockPos> positions = new ArrayList<>();
        for (int x = (int) (mc.player.getX() - rxz.getValue()); x < mc.player.getX() + rxz.getValue(); x++)
            for (int y = (int) (mc.player.getY() - ry.getValue()); y < mc.player.getY() + ry.getValue(); y++)
                for (int z = (int) (mc.player.getZ() - rxz.getValue()); z < mc.player.getZ() + rxz.getValue(); z++)
                    positions.add(new BlockPos(x, y, z));
        return positions;
    }
}

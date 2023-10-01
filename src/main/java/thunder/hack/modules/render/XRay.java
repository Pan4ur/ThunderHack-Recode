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
import java.util.HashMap;

public class XRay extends Module {
    private final Setting<Boolean> bruteForce = new Setting<>("OreDeobf", false);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false, v -> bruteForce.getValue());
    private final Setting<Boolean> fast = new Setting<>("Fast", false, v -> bruteForce.getValue());
    private final Setting<Integer> checkSpeed = new Setting<>("CheckSpeed", 4, 1, 5, v -> bruteForce.getValue());
    private final Setting<Integer> rxz = new Setting<>("RadiusXZ", 5, 5, 64, v -> bruteForce.getValue());
    private final Setting<Integer> ry = new Setting<>("RadiusY", 5, 2, 50, v -> bruteForce.getValue());
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

    private final HashMap<BlockPos, Boolean> ores = new HashMap<>();
    private final HashMap<BlockPos, Boolean> toCheck = new HashMap<>();
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
        getBlocks:
        for (BlockPos pos : getBlocks()) {
            int[][] dirs = {{0, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
            for (int[] dir : dirs) {
                if (mc.world.isAir(new BlockPos(pos.getX() + dir[0], pos.getY() + dir[1], pos.getZ() + dir[2])))
                    continue getBlocks;
            }
            if (fast.getValue())
                if (pos.getX() % 2 == 0 || pos.getZ() % 2 == 0 || pos.getY() % 2 == 0)
                    continue;
            toCheck.put(pos, false);
        }
        all = toCheck.size();
        done = 0;
        mc.worldRenderer.reload();
    }

    @Override
    public void onDisable() {
        mc.worldRenderer.reload();
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!bruteForce.getValue() ||toCheck.isEmpty() || !rotate.getValue()) return;
        BlockPos pos = getNext(toCheck);
        if (pos == null) return;
        InteractionUtility.BreakData blockData = InteractionUtility.getBreakData(pos, InteractionUtility.Interact.Strict);
        if (blockData != null) {
            float[] angle = InteractionUtility.calculateAngle(blockData.vector());
            mc.player.setYaw(angle[0]);
            mc.player.setPitch(angle[1]);
        }
    }

    @EventHandler
    public void onPostSync(EventPostSync e) {
        if (!bruteForce.getValue())
            return;
        for (int i = 0; i < checkSpeed.getValue(); ++i) {
            if (toCheck.isEmpty()) { displayBlock = null; return; }
            BlockPos pos = getNext(toCheck);
            if (pos == null) { displayBlock = null; return; }
            ++done;
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
            displayBlock = pos;
        }
    }
    private BlockPos getNext(HashMap<BlockPos, Boolean> hashMap) {
        for (HashMap.Entry<BlockPos, Boolean> entry : hashMap.entrySet()) {
            if (!entry.getValue()) {
                toCheck.put(entry.getKey(), true);
                return entry.getKey();
            }
        }
        return null;
    }
    @EventHandler
    public void onReceivePacket(PacketEvent.Receive e) {
        if (e.getPacket() instanceof BlockUpdateS2CPacket pac) {
            if (isCheckableOre(pac.getState().getBlock())) {
                BlockPos pos = pac.getPos();
                if (ores.containsKey(pos)) return;
                ores.put(pos, true);
                int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
                toCheck.put(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), true);
                for (int[] dir : dirs) {
                    if (!toCheck.containsKey(new BlockPos(pos.getX() + dir[0], pos.getY() + dir[1], pos.getZ() + dir[2]))){
                        toCheck.put(new BlockPos(pos.getX() + dir[0], pos.getY() + dir[1], pos.getZ() + dir[2]), false);
                    }
                }
            }
        }
    }

    public void onRender3D(MatrixStack stack) {
        try {
            for (HashMap.Entry<BlockPos, Boolean> entry : ores.entrySet()) {
                BlockPos pos = entry.getKey();
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.DIAMOND_ORE && diamond.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(0, 255, 255, 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), new Color(0, 255, 255, 200), 2);
                }
                if (block == Blocks.GOLD_ORE && gold.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(255, 215, 0, 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), new Color(255, 215, 0, 200), 2);
                }
                if (block == Blocks.IRON_ORE && iron.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(213, 213, 213, 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), new Color(213, 213, 213, 200), 2);
                }
                if (block == Blocks.EMERALD_ORE && emerald.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(0, 255, 77, 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), new Color(0, 255, 77, 200), 2);
                }
                if (block == Blocks.REDSTONE_ORE && redstone.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(255, 0, 0, 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), new Color(255, 0, 0, 200), 2);
                }
                if (block == Blocks.COAL_ORE && coal.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(0, 0, 0, 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), new Color(0, 0, 0, 200), 2);
                }
                if (block == Blocks.LAPIS_ORE && lapis.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(38, 97, 156, 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), new Color(38, 97, 156, 200), 2);
                }
                if (block == Blocks.ANCIENT_DEBRIS && netherite.getValue()) {
                    Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(255, 255, 255, 100));
                    Render3DEngine.drawBoxOutline(new Box(pos), new Color(255, 255, 255, 200), 2);
                }
            }
            if (displayBlock != null && (done != all)) {
                Render3DEngine.drawFilledBox(stack, new Box(displayBlock), new Color(255, 0, 30, 100));
                Render3DEngine.drawBoxOutline(new Box(displayBlock), new Color(255, 0, 60), 2);
            }
        } catch (Exception ignored) {
        }
    }

    public void onRender2D(DrawContext context) {
        if (bruteForce.getValue())
            FontRenderers.modules.drawCenteredString(context.getMatrices(), "Done: " + done + " / " + "All: " + all, mc.getWindow().getScaledWidth() / 2f, 50, -1);
    }

    public static boolean isCheckableOre(Block block) {
        if (diamond.getValue() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return true;
        if (gold.getValue() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE)) return true;
        if (iron.getValue() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) return true;
        if (emerald.getValue() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return true;
        if (redstone.getValue() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)) return true;
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

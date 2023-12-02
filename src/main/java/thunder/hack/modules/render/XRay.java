package thunder.hack.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import jdk.jfr.EventSettings;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL40C;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.SettingEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.impl.TargetHud;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.MovementUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static thunder.hack.modules.client.MainSettings.isRu;

public class XRay extends Module {

    public XRay() {
        super("XRay", Category.MISC);
    }

    public final Setting<Boolean> wallHack = new Setting<>("WallHack", false);
    private final Setting<Boolean> brutForce = new Setting<>("OreDeobf", false);
    private final Setting<Boolean> fast = new Setting<>("Fast", false, v -> brutForce.getValue());
    private final Setting<Integer> delay = new Setting<>("Delay", 25, 1, 100, v -> brutForce.getValue());
    private final Setting<Integer> radius = new Setting<>("Radius", 5, 5, 64, v -> brutForce.getValue());
    private final Setting<Integer> up = new Setting<>("Up", 5, 2, 50, v -> brutForce.getValue());
    private final Setting<Integer> down = new Setting<>("Down", 5, 2, 50, v -> brutForce.getValue());
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


    private final Timer delayTimer = new Timer();
    private final ArrayList<BlockPos> ores = new ArrayList<>();
    private final ArrayList<BlockPos> toCheck = new ArrayList<>();
    private BlockPos displayBlock;
    private int done;
    private int all;
    private Box area = new Box(BlockPos.ORIGIN);

    @Override
    public void onEnable() {
        ores.clear();
        toCheck.clear();
        toCheck.addAll(getBlocks());
        all = toCheck.size();
        done = 0;
        mc.chunkCullingEnabled = false;
        mc.worldRenderer.reload();
        area = new Box(mc.player.getX() - radius.getValue(), mc.player.getY() - down.getValue(), mc.player.getZ() - radius.getValue(),
                mc.player.getX() + radius.getValue(), mc.player.getY() + up.getValue(), mc.player.getZ() + radius.getValue());
    }

    @Override
    public void onDisable() {
        mc.worldRenderer.reload();
        mc.chunkCullingEnabled = true;
    }

    @Override
    public void onThread() {
        // FABOS IDI NAHUI

        if (toCheck.isEmpty() || !brutForce.getValue())
            return;

        if(mc.isInSingleplayer()){
            disable(isRu() ? "Братан, ты в синглплеере" : "Bro, you're in singleplayer");
        }

        if (delayTimer.every(delay.getValue())) {
            int index = toCheck.size() - 1 <= 1 ? 0 : ThreadLocalRandom.current().nextInt(0, toCheck.size() - 1);
            BlockPos pos = toCheck.remove(index);
            mc.interactionManager.attackBlock(pos, mc.player.getHorizontalFacing());
            mc.interactionManager.cancelBlockBreaking();
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            displayBlock = pos;
            ++done;
        }
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive e) {
        if (e.getPacket() instanceof BlockUpdateS2CPacket pac)
            if (isCheckableOre(pac.getState().getBlock()) && !ores.contains(pac.getPos()))
                ores.add(pac.getPos());
    }

    @EventHandler
    public void  onSettingChange(SettingEvent e) {
        if(e.getSetting() == wallHack) {
            mc.worldRenderer.reload();
        }
    }

    @EventHandler
    public void onMove(EventMove e) {
        if(brutForce.getValue()) {
            if(all != done) {
                e.setZ(0);
                e.setX(0);
                e.cancel();
                if(mc.player.age % 8 == 0 && MovementUtility.isMoving())
                    sendMessage(isRu() ? "Не двигайся пока идет деобфускация!" : "Don't move while deobf!");
            } else {
                Box newArea = new Box(mc.player.getX() - radius.getValue(), mc.player.getY() - down.getValue(), mc.player.getZ() - radius.getValue(),
                        mc.player.getX() + radius.getValue(), mc.player.getY() + up.getValue(), mc.player.getZ() + radius.getValue());

                if(!newArea.intersects(area)) {
                    area = newArea;
                    toCheck.clear();
                    toCheck.addAll(getBlocks());
                    all = toCheck.size();
                    done = 0;
                }
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

        if(brutForce.getValue())
            Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(area, HudEditor.getColor(1), 2));
    }

    private void draw(MatrixStack stack, BlockPos pos, int r, int g, int b) {
        Render3DEngine.drawFilledBox(stack, new Box(pos), new Color(r, g, b, 100));
        Render3DEngine.drawBoxOutline(new Box(pos), new Color(r, g, b, 200), 2);
    }

    public void onRender2D(DrawContext context) {
        if (brutForce.getValue()) {

            float posX = mc.getWindow().getScaledWidth() / 2f - 68;
            float posY = mc.getWindow().getScaledHeight() / 2f + 68;

            // Основа
            Render2DEngine.drawGradientBlurredShadow(context.getMatrices(), posX + 2, posY + 2, 133, 44, 14, HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90));
            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(180), HudEditor.getColor(90), posX, posY, 137, 47.5f, 9);
            Render2DEngine.drawRound(context.getMatrices(), posX + 0.5f, posY + 0.5f, 136f, 46, 9, Render2DEngine.injectAlpha(Color.BLACK, 220));

            // Баллон
            Render2DEngine.drawGradientRound(context.getMatrices(), posX + 4, posY + 32, 129, 11, 4f, HudEditor.getColor(0).darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker(), HudEditor.getColor(0).darker().darker().darker().darker());

            Render2DEngine.renderRoundedGradientRect(context.getMatrices(), HudEditor.getColor(270), HudEditor.getColor(0), HudEditor.getColor(0), HudEditor.getColor(270),
                    posX + 4, posY + 32, (int) MathHelper.clamp((129 * ((float) done / Math.max((float) all, 1))), 8, 129), 11, 4f);

            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), (int) ((float) done / (float) all * 100) + "%", posX + 68, posY + 33f, -1);
            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(),"XRay", posX + 68, posY + 7, -1);
            FontRenderers.sf_bold.drawCenteredString(context.getMatrices(),done + " / " + all + (isRu() ? " Осталось: " : " Estimated time: ") + MathUtility.round((all - done) * delay.getValue() / 1000f, 1) + "s", posX + 68, posY + 18, -1);

        }
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
        for (int x = (int) (mc.player.getX() - radius.getValue()); x < mc.player.getX() + radius.getValue(); x++)
            for (int y = (int) (mc.player.getY() - down.getValue()); y < mc.player.getY() + up.getValue(); y++)
                for (int z = (int) (mc.player.getZ() - radius.getValue()); z < mc.player.getZ() + radius.getValue(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (mc.world.isAir(pos))
                        continue;
                    if (fast.getValue() && (x % 2 == 0 || y % 2 == 0 || z % 2 == 0))
                        continue;
                    positions.add(pos);
                }
        return positions;
    }
}

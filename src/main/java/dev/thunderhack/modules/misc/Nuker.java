package dev.thunderhack.modules.misc;

import dev.thunderhack.event.events.EventAttackBlock;
import dev.thunderhack.event.events.EventSetBlockState;
import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.modules.Module;
import dev.thunderhack.modules.player.SpeedMine;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.ColorSetting;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.player.InteractionUtility;
import dev.thunderhack.utils.player.PlayerUtility;
import dev.thunderhack.utils.render.Render2DEngine;
import dev.thunderhack.utils.render.Render3DEngine;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.modules.client.HudEditor;

import java.awt.*;

import static net.minecraft.block.Blocks.BEDROCK;
import static dev.thunderhack.modules.client.MainSettings.isRu;

public class Nuker extends Module {
    public Nuker() {
        super("Nuker", Category.MISC);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Default);
    private final Setting<Integer> delay = new Setting<>("Delay", 25, 0, 1000);
    private final Setting<BlockSelection> blocks = new Setting<>("Blocks", BlockSelection.Select);
    private final Setting<Boolean> flatten = new Setting<>("Flatten", false);
    private final Setting<Boolean> creative = new Setting<>("Creative", false);
    private final Setting<Float> range = new Setting<>("Range", 4.2f, 1.5f, 5f);
    private final Setting<ColorMode> colorMode = new Setting<>("ColorMode", ColorMode.Sync);
    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4), v -> colorMode.getValue() == ColorMode.Custom);

    private Block targetBlockType;
    private BlockData blockData;
    private Timer breakTimer = new Timer();

    private NukerThread nukerThread = new NukerThread();

    @Override
    public void onEnable() {
        nukerThread = new NukerThread();
        nukerThread.setName("ThunderHack-NukerThread");
        nukerThread.setDaemon(true);
        nukerThread.start();
    }

    @Override
    public void onDisable() {
        nukerThread.interrupt();
    }

    @Override
    public void onUpdate() {
        if (!nukerThread.isAlive()) {
            nukerThread = new NukerThread();
            nukerThread.setName("ThunderHack-NukerThread");
            nukerThread.setDaemon(true);
            nukerThread.start();
        }
    }

    @EventHandler
    public void onBlockInteract(EventAttackBlock e) {
        if (mc.world.isAir(e.getBlockPos())) return;
        if (blocks.getValue().equals(BlockSelection.Select) && targetBlockType != mc.world.getBlockState(e.getBlockPos()).getBlock()) {
            targetBlockType = mc.world.getBlockState(e.getBlockPos()).getBlock();
            sendMessage(isRu() ? "Выбран блок: " + Formatting.AQUA + targetBlockType.getName().getString() : "Selected block: " + Formatting.AQUA + targetBlockType.getName().getString());
        }
    }

    @EventHandler
    public void onBlockDestruct(EventSetBlockState e) {
        if (blockData != null && e.getPos() == blockData.bp && e.getState().isAir()) {
            blockData = null;
            new Thread(() -> {
                if ((targetBlockType != null || blocks.getValue().equals(BlockSelection.All)) && !mc.options.attackKey.isPressed() && blockData == null) {
                    blockData = getNukerBlockPos();
                }
            }).start();
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (blockData != null) {
            if ((mc.world.getBlockState(blockData.bp).getBlock() != targetBlockType && blocks.getValue().equals(BlockSelection.Select))
                    || PlayerUtility.squaredDistanceFromEyes(blockData.bp.toCenterPos()) > range.getPow2Value()
                    || mc.world.isAir(blockData.bp))
                blockData = null;
        }

        if (blockData == null || mc.options.attackKey.isPressed()) return;

        float[] angle = InteractionUtility.calculateAngle(blockData.vec3d);
        mc.player.setYaw(angle[0]);
        mc.player.setPitch(angle[1]);

        if (mode.getValue() == Mode.Default) {
            breakBlock();
        }

        if (mode.getValue() == Mode.FastAF) {
            int intRange = (int) (Math.floor(range.getValue()) + 1);
            Iterable<BlockPos> blocks_ = BlockPos.iterateOutwards(new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange);

            for (BlockPos b : blocks_) {
                if (flatten.getValue() && b.getY() < mc.player.getY())
                    continue;

                BlockState state = mc.world.getBlockState(b);

                if (PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) <= range.getPow2Value()) {
                    if (state.getBlock() == targetBlockType || (blocks.getValue().equals(BlockSelection.All) && state.getBlock() != BEDROCK)) {
                        try {
                            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, b, Direction.UP, PlayerUtility.getWorldActionId(mc.world)));
                            mc.interactionManager.breakBlock(b);
                            mc.player.swingHand(Hand.MAIN_HAND);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    public synchronized void breakBlock() {
        if (blockData == null || mc.options.attackKey.isPressed()) return;
        if (ModuleManager.speedMine.isEnabled() && ModuleManager.speedMine.mode.getValue() == SpeedMine.Mode.Packet) {
            if (SpeedMine.minePosition != blockData.bp) {
                mc.interactionManager.attackBlock(blockData.bp, blockData.dir);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            BlockPos cache = blockData.bp;
            mc.interactionManager.updateBlockBreakingProgress(blockData.bp, blockData.dir);
            mc.player.swingHand(Hand.MAIN_HAND);
            if (creative.getValue())
                mc.interactionManager.breakBlock(cache);
        }
    }

    public void onRender3D(MatrixStack stack) {
        BlockPos renderBp = null;

        if (blockData != null && blockData.bp != null)
            renderBp = blockData.bp;

        if (renderBp != null) {
            Color color1 = colorMode.getValue() == ColorMode.Sync ? HudEditor.getColor(1) : color.getValue().getColorObject();
            Render3DEngine.drawBoxOutline(new Box(blockData.bp), color1, 2);
            Render3DEngine.drawFilledBox(stack, new Box(blockData.bp), Render2DEngine.injectAlpha(color1, 100));
        }

        if (mode.getValue() == Mode.Fast && breakTimer.passedMs(delay.getValue())) {
            breakBlock();
            breakTimer.reset();
        }
    }

    public BlockData getNukerBlockPos() {
        int intRange = (int) (Math.floor(range.getValue()) + 1);
        Iterable<BlockPos> blocks_ = BlockPos.iterateOutwards(new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange);

        for (BlockPos b : blocks_) {
            BlockState state = mc.world.getBlockState(b);
            if (flatten.getValue() && b.getY() < mc.player.getY())
                continue;
            if (PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) <= range.getPow2Value()) {
                if (state.getBlock() == targetBlockType || (blocks.getValue().equals(BlockSelection.All) && state.getBlock() != BEDROCK)) {
                    for (float x1 = 0f; x1 <= 1f; x1 += 0.2f) {
                        for (float y1 = 0f; y1 <= 1; y1 += 0.2f) {
                            for (float z1 = 0f; z1 <= 1; z1 += 0.2f) {
                                Vec3d p = new Vec3d(b.getX() + x1, b.getY() + y1, b.getZ() + z1);
                                BlockHitResult bhr = mc.world.raycast(new RaycastContext(InteractionUtility.getEyesPos(mc.player), p, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
                                if (bhr != null && bhr.getType() == HitResult.Type.BLOCK && bhr.getBlockPos().equals(b))
                                    return new BlockData(b, p, bhr.getSide());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public class NukerThread extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!Module.fullNullCheck()) {
                        while (ThunderHack.asyncManager.ticking.get()) {
                        }

                        if ((targetBlockType != null || blocks.getValue().equals(BlockSelection.All)) && !mc.options.attackKey.isPressed() && blockData == null) {
                            blockData = getNukerBlockPos();
                        }
                    } else {
                        Thread.yield();
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }


    private enum Mode {
        Default, Fast, FastAF
    }

    private enum ColorMode {
        Custom, Sync
    }

    private enum BlockSelection {
        Select, All
    }

    public record BlockData(BlockPos bp, Vec3d vec3d, Direction dir) {}
}

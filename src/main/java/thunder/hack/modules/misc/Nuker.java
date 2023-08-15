package thunder.hack.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.player.PlaceUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class Nuker extends Module {
    public Nuker() {
        super("Nuker", Category.MISC);
    }

    private Block nukerTargetBlock;
    private BlockPosWithRotation nukerTargetBlockpos;

    private Setting<Boolean> flatten = new Setting<>("Flatten", false);
    private Setting<Float> range = new Setting<>("Range", 4.2f, 0f, 5f);
    private final Setting<Mode> colorMode = new Setting<>("ColorMode", Mode.Sync);
    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4), v -> colorMode.getValue() == Mode.Custom);

    private enum Mode {
        Custom, Sync
    }


    @EventHandler
    public void onBlockInteract(EventAttackBlock e) {
        if (mc.world.isAir(e.getBlockPos())) return;
        nukerTargetBlock = mc.world.getBlockState(e.getBlockPos()).getBlock();
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (nukerTargetBlockpos != null) {
            if (mc.world.getBlockState(nukerTargetBlockpos.bp).getBlock() != nukerTargetBlock || mc.player.squaredDistanceTo(nukerTargetBlockpos.bp.toCenterPos()) > range.getPow2Value())
                nukerTargetBlockpos = null;
        }

        if (nukerTargetBlockpos == null || mc.options.attackKey.isPressed()) return;

        float[] angle = PlaceUtility.calculateAngle(nukerTargetBlockpos.vec3d);
        mc.player.setYaw(angle[0]);
        mc.player.setPitch(angle[1]);
        Direction dir = PlaceUtility.getBreakDirection(nukerTargetBlockpos.bp, true);
        if (dir == null) return;

        if (ModuleManager.speedMine.isEnabled()) {
            if (SpeedMine.minePosition != nukerTargetBlockpos.bp) {
                mc.interactionManager.attackBlock(nukerTargetBlockpos.bp, dir);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            mc.interactionManager.updateBlockBreakingProgress(nukerTargetBlockpos.bp, dir);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public void onRender3D(MatrixStack stack) {
        if (nukerTargetBlockpos != null && nukerTargetBlockpos.bp != null) {
            Color color1 = colorMode.getValue() == Mode.Sync ? HudEditor.getColor(1) : color.getValue().getColorObject();
            Render3DEngine.drawBoxOutline(new Box(nukerTargetBlockpos.bp), color1, 2);
            Render3DEngine.drawFilledBox(stack, new Box(nukerTargetBlockpos.bp), Render2DEngine.injectAlpha(color1, 100));
        }
    }

    @Override
    public void onThread() {
        if (nukerTargetBlock != null && !mc.options.attackKey.isPressed() && nukerTargetBlockpos == null) {
            nukerTargetBlockpos = getNukerBlockPos();
        }
    }

    public BlockPosWithRotation getNukerBlockPos() {

        int startY = flatten.getValue() ? (int) mc.player.getY() : (int) (mc.player.getY() - (range.getValue() + 1));

        for (int x = (int) (mc.player.getX() - (range.getValue() + 1)); x < mc.player.getX() + (range.getValue() + 1); x++)
            for (int y = startY; y < mc.player.getY() + (range.getValue() + 1); y++)
                for (int z = (int) (mc.player.getZ() - (range.getValue() + 1)); z < mc.player.getZ() + (range.getValue() + 1); z++) {
                    BlockPos bp = BlockPos.ofFloored(x, y, z);
                    if (mc.player.squaredDistanceTo(bp.toCenterPos()) > range.getPow2Value()) continue;
                    if (mc.world.getBlockState(bp).getBlock() == nukerTargetBlock) {
                        for (Vec3d point : PlaceUtility.multiPoint) {
                            Vec3d p = new Vec3d(bp.getX() + point.getX(), bp.getY() + point.getY(), bp.getZ() + point.getZ());
                            BlockHitResult bhr = mc.world.raycast(new RaycastContext(PlaceUtility.getEyesPos(mc.player), p, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
                            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK && bhr.getBlockPos().equals(bp)) {
                                return new BlockPosWithRotation(bp, p);
                            }
                        }
                    }
                }
        return null;
    }

    public record BlockPosWithRotation(BlockPos bp, Vec3d vec3d) {
    }
}

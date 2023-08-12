package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.render.Render3DEngine;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.utility.player.PlaceUtility;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

public class AutoWeb extends Module {
    public AutoWeb() {
        super("AutoWeb", Category.COMBAT);
    }

    private Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private  Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    private  Setting<Integer> actionShift = new Setting<>("ActionShift", 2, 1, 2);
    private  Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 0, 10);
    private  Setting<Float> placeRange = new Setting<>("TargetRange", 3.5F, 1f, 6f);
    private  Setting<Boolean> head = new Setting<>("Head", true);
    private  Setting<Boolean> toggelable = new Setting<>("DisableWhenDone", false);

    public static Timer inactivityTimer = new Timer();

    private int tickCounter = 0;

    private ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    public void onRender3D(MatrixStack stack) {
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                Render3DEngine.drawBoxOutline(new Box(pos), HudEditor.getColor(0), 2);
            }
        });
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!mc.player.isOnGround()) return;

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        PlayerEntity nearestTarget = Thunderhack.combatManager.getTargets(placeRange.getValue())
                .stream()
                .filter(this::isValidBase)
                .min(Comparator.comparing(e -> mc.player.distanceTo(e)))
                .orElse(null);;

        if (nearestTarget == null || tickCounter < actionInterval.getValue()) {
            return;
        }

        BlockPos feetPos = new BlockPos((int) Math.floor(nearestTarget.getX() + (nearestTarget.getX() - nearestTarget.prevX)), (int) Math.floor(nearestTarget.getY()), (int) Math.floor(nearestTarget.getZ() + (nearestTarget.getZ() - nearestTarget.prevZ)));

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos nextPos = PlaceUtility.canPlaceBlock(feetPos,true) ? feetPos : head.getValue() ? PlaceUtility.canPlaceBlock(feetPos.up(),true) ? feetPos.up() : null : null;
            if (nextPos != null) {
                if (PlaceUtility.place( nextPos, rotate.getValue(), strictDirection.getValue(), Hand.MAIN_HAND, InventoryUtil.findHotbarBlock(Blocks.COBWEB),false)) {
                    blocksPlaced++;
                    PlaceUtility.ghostBlocks.put(nextPos, System.currentTimeMillis());
                    renderPoses.put(nextPos, System.currentTimeMillis());
                    tickCounter = 0;
                    inactivityTimer.reset();
                } else {
                    break;
                }
            } else {
                if (toggelable.getValue()) {
                    toggle();
                }
                break;
            }
        }
    }

    private boolean isValidBase(PlayerEntity player) {
        Block baseBlock = mc.world.getBlockState(BlockPos.ofFloored(player.getPos()).down()).getBlock();
        return !(baseBlock instanceof AirBlock) && !(baseBlock instanceof FluidBlock);
    }
}

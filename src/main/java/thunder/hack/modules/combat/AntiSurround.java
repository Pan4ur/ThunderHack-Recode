package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.world.HoleUtility;

import static thunder.hack.modules.client.MainSettings.isRu;

public class AntiSurround extends Module {
    private final Setting<Float> range = new Setting<>("Range", 5f, 1f, 7f);
    private final Setting<Boolean> autoDisable = new Setting<>("Auto Disable", true);
    private final Setting<Boolean> autoSwitch = new Setting<>("Switch", true);
    private final Setting<Boolean> requirePickaxe = new Setting<>("Only Pickaxe", false);
    private final Setting<Boolean> oldVers = new Setting<>("1.12 Mode", false);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<TargetLogic> targetLogic = new Setting<>("TargetLogic", TargetLogic.Distance);

    private BlockPos blockPos;

    public AntiSurround() {
        super("AntiSurround", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        blockPos = null;
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (fullNullCheck()) return;
        if (requirePickaxe.getValue() && !checkPickaxe()) return;

        if (blockPos != null && mc.world.isAir(blockPos)) {
            if (autoDisable.getValue()) {
                disable(isRu() ? "Сарраунд сломан! Выключаю..." : "Surround has been broken! Turning off...");
                return;
            }
            blockPos = null;
        }

        BlockPos minePos = null;
        PlayerEntity target = null;
        boolean found = false;

        switch (targetLogic.getValue()) {
            case HP -> target = ThunderHack.combatManager.getTargetByHP(range.getValue() + 1f);
            case Distance -> target = ThunderHack.combatManager.getNearestTarget(range.getValue() + 1f);
            case FOV -> target = ThunderHack.combatManager.getTargetByFOV(range.getValue() + 1f);
        }

        if(target == null) return;

        BlockPos targetPos = BlockPos.ofFloored(target.getPos());

        if(!HoleUtility.isHole(targetPos)) return;

        // 1 прогон - ищем на которые можно поставить
        for (BlockPos offset : HoleUtility.getSurroundPoses(targetPos)) {
            if (mc.world.getBlockState(offset).getBlock() != Blocks.OBSIDIAN)
                continue;
            if (!mc.world.isAir(offset.add(0, 1, 0)))
                continue;
            if (mc.player.squaredDistanceTo(offset.toCenterPos()) < range.getPow2Value()) {
                minePos = offset;
                found = true;
                break;
            }
        }

        // 2 прогон - ищем любой
        if(!found && !oldVers.getValue()) {
            for (BlockPos pos2 : HoleUtility.getSurroundPoses(targetPos)) {
                if (mc.world.getBlockState(pos2).getBlock() != Blocks.OBSIDIAN)
                    continue;
                if (mc.player.squaredDistanceTo(pos2.toCenterPos()) < range.getPow2Value()) {
                    minePos = pos2;
                    break;
                }
            }
        }

        if (minePos != null) {
            if (autoSwitch.getValue())
                InventoryUtility.getPickAxe().switchTo();

            if (rotate.getValue()) {
                float[] rotation = getRotations(minePos);
                if (rotation != null) {
                    mc.player.setYaw(rotation[0]);
                    mc.player.setPitch(rotation[1]);
                }
            }

            if (!requirePickaxe.getValue() || mc.player.getMainHandStack().getItem() instanceof PickaxeItem) {
                if (ModuleManager.speedMine.isEnabled() && SpeedMine.progress != 0)
                    return;
                InteractionUtility.BreakData data = InteractionUtility.getBreakData(minePos, InteractionUtility.Interact.Strict);
                if (data == null)
                    return;
                mc.interactionManager.attackBlock(minePos, data.dir());
                mc.player.swingHand(Hand.MAIN_HAND);
                blockPos = minePos;
            }
        }
    }

    public static float @Nullable [] getRotations(@NotNull BlockPos blockPos) {
        InteractionUtility.BreakData data = InteractionUtility.getBreakData(blockPos, InteractionUtility.Interact.Strict);
        return data == null ? null : InteractionUtility.calculateAngle(data.vector());
    }

    public boolean checkPickaxe() {
        return mc.player.getMainHandStack().getItem() instanceof PickaxeItem;
    }

    private enum TargetLogic {Distance, HP, FOV}
}

package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventPlaceBlock;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanParent;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;
import java.util.List;

public class CombatUtils extends Module {
    private final Setting<Integer> range = new Setting<>("Range", 1, 1, 7);
    private final Setting<Integer> actionShift = new Setting<>("Place Per Tick", 1, 1, 5);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);

    private final Setting<BooleanParent> autoMine = new Setting<>("AutoMine", new BooleanParent(true));
    private final Setting<Boolean> allowManual = new Setting<>("Allow Manual", true).withParent(autoMine);
    private final Setting<Parent> logic = new Setting<>("Logic", new Parent(false, 1)).withParent(autoMine);
    private final Setting<Boolean> antiSurround = new Setting<>("Surround", true).withParent(logic);
    private final Setting<Boolean> antiCev = new Setting<>("Cev", true).withParent(logic);
    private final Setting<Boolean> antiCiv = new Setting<>("Civ", true).withParent(logic);
    private final Setting<Boolean> blockerReMine = new Setting<>("ReMine On Blocker", true).withParent(logic);

    private final List<BlockPos> placePoses = new ArrayList<>();
    private BlockPos minePos;
    private PlayerEntity target;

    public CombatUtils() {
        super("CombatUtils", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        placePoses.clear();
        minePos = null;
        target = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        if (target == null || mc.player.distanceTo(target) > range.getValue()) {
            target = ThunderHack.combatManager.getNearestTarget(range.getValue());
            return;
        }
    }

    @EventHandler
    private void onPostSync(EventPostSync event) {
        placePoses.forEach(pos -> {
            if (InteractionUtility.placeBlock(pos, rotate.getValue(), interactMode.getValue(), placeMode.getValue(), InventoryUtility.findItemInHotBar(Items.OBSIDIAN), true, false)) {
                placePoses.remove(pos);
            }
        });
        if (minePos != null) {
            InteractionUtility.BreakData data = InteractionUtility.getBreakData(minePos, InteractionUtility.Interact.Strict);
            if (data == null)
                return;

            if (ModuleManager.speedMine.isEnabled()) mc.interactionManager.attackBlock(minePos, data.dir());
        }
    }

    @EventHandler
    private void onPlaceBlock(EventPlaceBlock event) {
        if (!mc.world.getBlockState(minePos.up()).isReplaceable()
                && autoMine.getValue().isEnabled()
                && blockerReMine.getValue()) {
            findMinePos();
        }
    }

    private void findMinePos() {
        BlockPos targetPos = BlockPos.ofFloored(target.getPos());
        boolean found = false;

        if (!HoleUtility.isHole(targetPos)
                || mc.world == null
                || mc.player == null) return;

        // 1 прогон - ищем на которые можно поставить
        if (antiSurround.getValue()) {
            for (BlockPos offset : HoleUtility.getSurroundPoses(target.getPos())) {
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
        }

        // 2 прогон кив брейкаем
        if (!found && antiCiv.getValue()) {
            for (BlockPos pos : HoleUtility.getSurroundPoses(target.getPos()).stream()
                    .map(BlockPos::up)
                    .toList()) {
                if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN) || !mc.world.isAir(pos)) continue;
                if (mc.player.squaredDistanceTo(pos.toCenterPos()) < range.getPow2Value()) {
                    minePos = pos;
                    found = true;
                    break;
                }
            }
        }

        // 3 прогон кев брейкаем
        if (!found && antiCev.getValue()) {
            for (BlockPos pos : HoleUtility.getHolePoses(target.getPos()).stream()
                    .map(bp -> bp.up(2))
                    .toList()) {
                if (mc.world.getBlockState(pos).getBlock().equals(Blocks.OBSIDIAN)
                        && mc.world.getBlockState(pos.up()).isReplaceable()) {
                    minePos = pos;
                    found = true;
                    break;
                }
            }
        }

        // 4 прогон - ищем любой
        if (!found && antiSurround.getValue()) {
            for (BlockPos pos : HoleUtility.getSurroundPoses(target.getPos())) {
                if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN)
                    continue;
                if (mc.player.squaredDistanceTo(pos.toCenterPos()) < range.getPow2Value()) {
                    minePos = pos;
                    break;
                }
            }
        }
    }
}

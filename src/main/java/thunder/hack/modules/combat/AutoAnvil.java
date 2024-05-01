package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.CombatManager;
import thunder.hack.events.impl.EventTick;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.world.HoleUtility;

import java.util.Arrays;

public class AutoAnvil extends Module {
    private final Setting<Float> range = new Setting<>("Range", 5f, 1f, 7f);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<InteractionUtility.Rotate> rotate = new Setting<>("Rotate", InteractionUtility.Rotate.None);
    private final Setting<CombatManager.TargetBy> targetBy = new Setting<>("TargetBy", CombatManager.TargetBy.Distance);

    private PlayerEntity target;

    public AutoAnvil() {
        super("AutoAnvil", Category.COMBAT);
    }

    @EventHandler
    private void onTick(EventTick event) {
        if (mc.player == null) return;
        if (target == null || target.isDead()) {
            target = ThunderHack.combatManager.getTarget(range.getValue(), targetBy.getValue());
            return;
        }

        // Find poses to place
        final SearchInvResult anvilResult = InventoryUtility.findItemInHotBar(Items.ANVIL);
        final SearchInvResult plateResult = InventoryUtility.findItemInHotBar(Items.STONE_PRESSURE_PLATE, Items.BIRCH_PRESSURE_PLATE, Items.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.OAK_PRESSURE_PLATE);

        final BlockPos anvilPos = BlockPos.ofFloored(target.getPos()).up(2);
        if (!anvilResult.found() || !plateResult.found()) return;

        Block targetBlock = mc.world.getBlockState(BlockPos.ofFloored(target.getPos())).getBlock();

        if(!(targetBlock instanceof PressurePlateBlock) && targetBlock != Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE && targetBlock != Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            InteractionUtility.placeBlock(BlockPos.ofFloored(target.getPos()), rotate.getValue(), interact.getValue(), placeMode.getValue(), plateResult, true, true);
            return;
        }

        if (!InteractionUtility.canPlaceBlock(anvilPos, interact.getValue(), false)) {
            if (needObsidian(anvilPos)) {
                final BlockPos obsidianPos = Arrays.stream(HoleUtility.VECTOR_PATTERN).parallel()
                        .map(anvilPos::add)
                        .filter(pos -> InteractionUtility.canPlaceBlock(pos, interact.getValue(), false))
                        .filter(pos -> pos.getSquaredDistance(mc.player.getPos()) <= range.getPow2Value())
                        .findFirst()
                        .orElse(null);
                final SearchInvResult result = InventoryUtility.findItemInHotBar(Items.OBSIDIAN);

                if (obsidianPos != null && result.found()) {
                    InteractionUtility.placeBlock(obsidianPos, rotate.getValue(), interact.getValue(), placeMode.getValue(), result, true, false);
                }
            }
            return;
        }

        InteractionUtility.placeBlock(anvilPos, rotate.getValue(), interact.getValue(), placeMode.getValue(), anvilResult, true, false);
    }

    private boolean needObsidian(BlockPos anvilPos) {
        if (mc.world == null) return false;

        return Arrays.stream(HoleUtility.VECTOR_PATTERN)
                .map(anvilPos::add)
                .filter(pos -> !mc.world.getBlockState(pos).isReplaceable())
                .toList()
                .isEmpty();
    }
}

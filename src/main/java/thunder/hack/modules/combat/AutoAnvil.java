package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.CombatManager;
import thunder.hack.events.impl.EventPostSync;
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
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<CombatManager.TargetBy> targetBy = new Setting<>("TargetBy", CombatManager.TargetBy.Distance);

    private PlayerEntity target;

    public AutoAnvil() {
        super("AnvilKicker", Category.COMBAT);
    }

    @EventHandler
    private void onPostSync(EventPostSync event) {
        if (mc.player == null) return;
        if (target == null || target.isDead()) {
            target = ThunderHack.combatManager.getTarget(range.getValue(), targetBy.getValue());
            return;
        }

        // Find poses to place
        final SearchInvResult anvilResult = InventoryUtility.findItemInHotBar(Items.ANVIL);
        final BlockPos anvilPos = target.getBlockPos().up(2);
        if (!anvilResult.found()) return;

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

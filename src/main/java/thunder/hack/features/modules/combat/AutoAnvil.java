package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.player.CombatManager;
import thunder.hack.events.impl.EventTick;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.world.HoleUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class AutoAnvil extends Module {
    private final Setting<Float> range = new Setting<>("Range", 5f, 1f, 7f);
    private final Setting<Boolean> once = new Setting<>("Once", false);
    private final Setting<Boolean> placePlates = new Setting<>("PlacePlates", false);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<InteractionUtility.Rotate> rotate = new Setting<>("Rotate", InteractionUtility.Rotate.None);
    private final Setting<CombatManager.TargetBy> targetBy = new Setting<>("TargetBy", CombatManager.TargetBy.Distance);
    private final Setting<Boolean> sand = new Setting<>("Sand", false);
    private final Setting<Boolean> gravel = new Setting<>("Gravel", false);
    private final Setting<Boolean> concrete = new Setting<>("Сoncrete", false);
    private final Setting<Boolean> anvils = new Setting<>("Anvils", true);

    private PlayerEntity target;

    public AutoAnvil() {
        super("AutoAnvil", Category.COMBAT);
    }

    @EventHandler
    private void onTick(EventTick event) {
        if (mc.player == null) return;
        if (target == null || target.isDead()) {
            target = Managers.COMBAT.getTarget(range.getValue(), targetBy.getValue());
            return;
        }

        final SearchInvResult result = getBlockResult();
        final SearchInvResult plateResult = InventoryUtility.findItemInHotBar(Items.STONE_PRESSURE_PLATE, Items.BIRCH_PRESSURE_PLATE, Items.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.OAK_PRESSURE_PLATE);

        final BlockPos anvilPos = BlockPos.ofFloored(target.getPos()).up(2);

        if (!result.found() || (!plateResult.found() && placePlates.getValue()))
            return;

        Block targetBlock = mc.world.getBlockState(BlockPos.ofFloored(target.getPos())).getBlock();

        if (!(targetBlock instanceof PressurePlateBlock) && targetBlock != Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE && targetBlock != Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE && placePlates.getValue()) {
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
                final SearchInvResult obbyResult = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN);

                if (obsidianPos != null && obbyResult.found()) {
                    InteractionUtility.placeBlock(obsidianPos, rotate.getValue(), interact.getValue(), placeMode.getValue(), obbyResult, true, false);
                    if(once.getValue())
                        disable(isRu() ? "Блок размещен" : "Done");
                }
            }
            return;
        }

        InteractionUtility.placeBlock(anvilPos, rotate.getValue(), interact.getValue(), placeMode.getValue(), result, true, false);
    }

    private boolean needObsidian(BlockPos anvilPos) {
        if (mc.world == null) return false;

        return Arrays.stream(HoleUtility.VECTOR_PATTERN)
                .map(anvilPos::add)
                .filter(pos -> !mc.world.getBlockState(pos).isReplaceable())
                .toList()
                .isEmpty();
    }

    protected SearchInvResult getBlockResult() {
        final List<Block> canUseBlocks = new ArrayList<>();

        if (mc.player == null) return SearchInvResult.notFound();
        if (anvils.getValue()) canUseBlocks.add(Blocks.ANVIL);
        if (sand.getValue()) canUseBlocks.add(Blocks.SAND);
        if (gravel.getValue()) canUseBlocks.add(Blocks.GRAVEL);


        SearchInvResult defaultResult = InventoryUtility.findBlockInHotBar(canUseBlocks);
        SearchInvResult concreteResult = InventoryUtility.findInHotBar(i -> (i.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ConcretePowderBlock));

        return concrete.getValue() && concreteResult.found() ? concreteResult : defaultResult;
    }
}

package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;

import java.util.ArrayList;
import java.util.List;

public class CombatUtils extends Module {
    private final Setting<Integer> actionShift = new Setting<>("Place Per Tick", 1, 1, 5);
    private final Setting<Integer> actionInterval = new Setting<>("Delay", 0, 0, 5);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", false);
    private final Setting<InteractionUtility.Interact> interactMode = new Setting<>("Interact Mode", InteractionUtility.Interact.Vanilla);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);

    private final List<BlockPos> placePoses = new ArrayList<>();
    private BlockPos minePos;

    public CombatUtils() {
        super("CombatUtils", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        placePoses.clear();
        minePos = null;
    }

    @EventHandler
    private void onPostSync(EventPostSync event) {
        placePoses.forEach(pos -> {
            if (InteractionUtility.placeBlock(pos, rotate.getValue(), interactMode.getValue(), placeMode.getValue(), InventoryUtility.findItemInHotBar(Items.OBSIDIAN), true, false)) {
                placePoses.remove(pos);
            }
        });
        if (minePos != null) {

        }
    }
}

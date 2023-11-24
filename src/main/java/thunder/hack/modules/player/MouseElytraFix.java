package thunder.hack.modules.player;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.modules.Module;
import thunder.hack.utility.Timer;

public class MouseElytraFix extends Module {

    public MouseElytraFix() {
        super("MouseElytraFix", Category.PLAYER);
    }

    private final Timer delay = new Timer();

    @Override
    public void onUpdate() {
        ItemStack stack = mc.player.currentScreenHandler.getCursorStack();
        if (stack.getItem() instanceof ArmorItem armor && delay.passedMs(300)) {
            if (armor.getType() == ArmorItem.Type.CHESTPLATE && mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
                int nullSlot = findEmptySlot();
                boolean needDrop = nullSlot == 999;
                if (needDrop) nullSlot = 9;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, nullSlot, 0, SlotActionType.PICKUP, mc.player);
                if (needDrop)
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, -999, 0, SlotActionType.PICKUP, mc.player);
                sendMessage("fixed");
                delay.reset();
            }
        }
    }

    public static int findEmptySlot() {
        for (int i = 0; i < 36; i++)
            if (mc.player.getInventory().getStack(i).isEmpty()) return i < 9 ? i + 36 : i;
        return 999;
    }
}

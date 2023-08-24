package thunder.hack.modules.player;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.cmd.Command;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

public class ElytraSwap extends Module {
    public ElytraSwap() {
        super("ElytraSwap", Category.PLAYER);
    }

    public int swap = 0;

    public static int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE,Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE};
        for (Item item : items) {
            SearchInvResult slot = InventoryUtility.findItemInInventory(item);
            if (slot.found()) {
                return slot.slot();
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        if (mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            int slot = getChestPlateSlot();
            if (slot != -1) {
                clickSlot(slot);
                clickSlot(6);
                clickSlot(slot);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                swap = 1;
            } else {
                disable(MainSettings.isRu() ? "У тебя нет нагрудника!" : "You don't have chestplate!");
                return;
            }
        } else if (InventoryUtility.getItemSlot(Items.ELYTRA) != -1) {
            int slot = InventoryUtility.getItemSlot(Items.ELYTRA);
            clickSlot(slot);
            clickSlot(6);
            clickSlot(slot);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            swap = 2;
        } else {
            disable(MainSettings.isRu() ? "У тебя нет элитры!" : "You don't have elytra!");
            return;
        }
        disable(MainSettings.isRu() ? "Свапнул! Отключаю.." : "Swapped! Disabling..");
    }
}

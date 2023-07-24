package thunder.hack.modules.player;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.cmd.Command;
import thunder.hack.modules.Module;
import thunder.hack.utility.player.InventoryUtil;

public class ElytraSwap extends Module {
    public ElytraSwap() {
        super("ElytraSwap", Category.PLAYER);
    }

    public int swap = 0;


    public static int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE,Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE};
        for (Item item : items) {
            if (InventoryUtil.getItemSlot(item) != -1) {
                return InventoryUtil.getItemSlot(item);
            }
        }
        return -1;
    }

    public static int getClickSlot(int id) {
        if (id == -1) {
            return id;
        }
        if (id < 9) {
            id += 36;
            return id;
        }
        if (id == 39) {
            id = 5;
        } else if (id == 38) {
            id = 6;
        } else if (id == 37) {
            id = 7;
        } else if (id == 36) {
            id = 8;
        } else if (id == 40) {
            id = 45;
        }
        return id;
    }

    public static void clickSlot(int id) {
        if (id != -1) mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, getClickSlot(id), 0, SlotActionType.PICKUP, mc.player);
    }

    @Override
    public void onEnable() {
        if (mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            int slot = getChestPlateSlot();
            if (slot != -1) {
                clickSlot(slot);
                clickSlot(38);
                clickSlot(slot);
                swap = 1;
            } else {
                Command.sendMessage("У тебя нет честплейта!");
            }
        } else if (InventoryUtil.getItemSlot(Items.ELYTRA) != -1) {
            int slot = InventoryUtil.getItemSlot(Items.ELYTRA);
            clickSlot(slot);
            clickSlot(38);
            clickSlot(slot);
            swap = 2;
        } else {
            Command.sendMessage("У тебя нет элитры!");
        }
        disable();
    }
}

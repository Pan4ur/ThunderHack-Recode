package thunder.hack.modules.player;


import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;

import java.util.Arrays;
import java.util.List;

public class InventoryCleaner extends Module {
    public InventoryCleaner() {
        super("InventoryCleaner", Category.PLAYER);
    }


    private static final List<Item> nexusShit = Arrays.asList(
            Items.PAPER, Items.OAK_SAPLING, Items.PLAYER_HEAD, Items.WOODEN_AXE,
            Items.KNOWLEDGE_BOOK, Items.BIRCH_SAPLING, Items.STONE_SHOVEL, Items.SPRUCE_SAPLING,
            Items.WHEAT_SEEDS, Items.TORCH, Items.BUCKET, Items.FLINT_AND_STEEL, Items.STICK
    );



    public final Setting<Mode> mode = new Setting<>("Mode", Mode.NexusGrief);

    public enum Mode {
        NexusGrief
    }


    public final Setting<DropWhen> dropWhen = new Setting<>("DropWhen", DropWhen.NotInInventory);

    public enum DropWhen {
        Inventory, Always, NotInInventory
    }

    @Override
    public void onUpdate() {
        boolean inInv = mc.currentScreen instanceof GenericContainerScreen;

        if(dropWhen.getValue() == DropWhen.Inventory && !inInv)
            return;

        if(dropWhen.getValue() == DropWhen.NotInInventory && inInv)
            return;

        for (int slot = 0; slot < 36; slot++) {
            Item itemFromslot = mc.player.getInventory().getStack(slot).getItem();
            if (nexusShit.contains(itemFromslot)) {
                // Порой бывает что деревянный топорик - твое единственное оружие...
                if (itemFromslot == Items.WOODEN_AXE && InventoryUtility.getItemCount(Items.WOODEN_AXE) == 1)
                    continue;

                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot < 9 ? slot + 36 : slot, 1, SlotActionType.THROW, mc.player);
            }
        }
    }
}

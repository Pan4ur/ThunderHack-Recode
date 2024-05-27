package thunder.hack.setting.impl;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;

public class ItemSelectSetting {
    private List<String> itemsById;
    private List<Item> items = new ArrayList<>();

    public ItemSelectSetting(List<String> itemsById) {
        this.itemsById = itemsById;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<String> getItemsById() {
        return itemsById;
    }

    public void updateItems() {
        items.clear();

        for (Block block : Registries.BLOCK)
            if (itemsById.contains(block.getTranslationKey().replace("block.minecraft.", "")))
                items.add(block.asItem());

        for (Item item : Registries.ITEM)
            if (itemsById.contains(item.getTranslationKey().replace("item.minecraft.", "")))
                items.add(item.asItem());
    }
}

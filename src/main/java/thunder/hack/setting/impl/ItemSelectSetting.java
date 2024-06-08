package thunder.hack.setting.impl;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.List;

public class ItemSelectSetting {
    private List<String> itemsById;

    public ItemSelectSetting(List<String> itemsById) {
        this.itemsById = itemsById;
    }

    public List<String> getItemsById() {
        return itemsById;
    }

    public void add(String s) {
        itemsById.add(s);
    }

    public void remove(String s) {
        itemsById.remove(s);
    }

    public boolean contains(String s) {
        return itemsById.contains(s);
    }

    public void add(Block b) {
        add(b.getTranslationKey().replace("block.minecraft.", ""));
    }

    public void add(Item i) {
        add(i.getTranslationKey().replace("item.minecraft.", ""));
    }

    public void remove(Block b) {
        remove(b.getTranslationKey().replace("block.minecraft.", ""));
    }

    public void remove(Item i) {
        remove(i.getTranslationKey().replace("item.minecraft.", ""));
    }

    public boolean contains(Block b) {
        return contains(b.getTranslationKey().replace("block.minecraft.", ""));
    }

    public boolean contains(Item i) {
        return contains(i.getTranslationKey().replace("item.minecraft.", ""));
    }

    public void clear() {
        itemsById.clear();
    }
}

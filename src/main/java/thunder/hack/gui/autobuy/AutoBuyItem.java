package thunder.hack.gui.autobuy;

import net.minecraft.item.Item;
import net.minecraft.util.Pair;

import java.util.ArrayList;

public class AutoBuyItem {
    private Item item;
    private ArrayList<Pair<String, Integer>> enchantments;
    private int price;
    private int count;

    public AutoBuyItem(Item item, ArrayList<Pair<String, Integer>> enchantments, int price, int count) {
        this.item = item;
        this.enchantments = enchantments;
        this.price = price;
        this.count = count;
    }

    public Item getItem() {
        return item;
    }

    public ArrayList<Pair<String, Integer>> getEnchantments() {
        return enchantments;
    }

    public String[] getEnchantmentstoArray() {
        String[] array = new String[enchantments.size()];
        int i = 0;
        for (Pair<String, Integer> pair : enchantments) {
            array[i] = pair.getLeft()+":"+pair.getRight();
            i++;
        }
        return array;
    }

    public int getPrice() {
        return price;
    }

    public void setEnchantments(ArrayList<Pair<String, Integer>> enchantments) {
        this.enchantments = enchantments;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
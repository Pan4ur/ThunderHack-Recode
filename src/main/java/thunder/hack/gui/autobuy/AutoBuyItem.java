package thunder.hack.gui.autobuy;

import net.minecraft.item.Item;
import net.minecraft.util.Pair;

import java.util.ArrayList;

public class AutoBuyItem {
    private Item item;
    private ArrayList<Pair<String, Integer>> enchantments;
    private ArrayList<String> attributes;
    private int price;
    private int count;
    private boolean checkForStar;

    public AutoBuyItem(Item item, ArrayList<Pair<String, Integer>> enchantments, ArrayList<String> atributes, int price, int count, boolean checkForStar) {
        this.item = item;
        this.enchantments = enchantments;
        this.price = price;
        this.count = count;
        this.attributes = atributes;
        this.checkForStar = checkForStar;
    }

    public Item getItem() {
        return item;
    }

    public ArrayList<Pair<String, Integer>> getEnchantments() {
        return enchantments;
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }

    public String[] getAttributesToArray() {
        String[] array = new String[attributes.size()];
        int i = 0;
        for (String s : attributes) {
            array[i] = s;
            i++;
        }
        return array;
    }

    public String[] getEnchantmentsToArray() {
        String[] array = new String[enchantments.size()];
        int i = 0;
        for (Pair<String, Integer> pair : enchantments) {
            array[i] = pair.getLeft() + ":" + pair.getRight();
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

    public void setAttributes(ArrayList<String> attributes) {
        this.attributes = attributes;
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

    public boolean checkForStar() {
        return checkForStar;
    }

    public void setCheckForStar(boolean checkForStar) {
        this.checkForStar = checkForStar;
    }
}
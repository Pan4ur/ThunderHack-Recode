package thunder.hack.utility;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.text.Text;

import static thunder.hack.utility.Util.mc;

public class InventoryUtil {


    public static int getItemCount(Item item) {
        if (mc.player == null) {
            return 0;
        }
        int n = 0;
        int n2 = 44;
        for (int i = 0; i <= n2; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.getItem() != item) continue;
            n += itemStack.getCount();
        }
        return n;
    }

    public static int getCrystalSlot() {
        int crystalSlot = -1;

        if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            crystalSlot = mc.player.getInventory().selectedSlot;
        }
        if (crystalSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.getInventory().getStack(l).getItem() == Items.END_CRYSTAL) {
                    crystalSlot = l;
                    break;
                }
            }
        }

        return crystalSlot;
    }

    public static int getBestAxe() {
        int b = -1;
        float f = 1.0F;
        for (int b1 = 9; b1 < 45; b1++) {
            ItemStack itemStack = mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof AxeItem axe) {
                float f1 = axe.getMaxDamage();
                f1 += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, itemStack);
                if (f1 > f) {
                    f = f1;
                    b = b1;
                }
            }
        }
        return b;
    }

    public static int getPickaxe() {
        int b = -1;
        float f = 1.0F;
        for (int b1 = 9; b1 < 45; b1++) {
            ItemStack itemStack = mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof PickaxeItem) {
                float f1 = 0;
                f1 += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
                if (f1 > f) {
                    f = f1;
                    b = b1;
                }
            }
        }
        return b;
    }

    public static int getBestSword() {
        int b = -1;
        float f = 1.0F;
        for (int b1 = 9; b1 < 45; b1++) {
            ItemStack itemStack = mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof SwordItem sword) {
                float f1 = sword.getMaxDamage();
                f1 += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, itemStack);
                if (f1 > f) {
                    f = f1;
                    b = b1;
                }
            }
        }
        return b;
    }


    public static int getItemHotbar(Item input) {
        for (int i = 0; i < 9; ++i) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (Item.getRawId(item) != Item.getRawId(input)) continue;
            return i;
        }
        return -1;
    }

    public static int findHotbarBlock(Block blockIn) {
        for (int i = 0; i < 9; ++i) {
            Block block;
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof BlockItem) || (block = ((BlockItem) stack.getItem()).getBlock()) != blockIn)
                continue;
            return i;
        }
        return -1;
    }

    public static int getFireWorks() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof FireworkRocketItem) {
                return i;
            }
        }
        return -1;
    }

    public static int findItem(Class clazz) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY) continue;
            if (clazz.isInstance(stack.getItem())) {
                return i;
            }
            if (!(stack.getItem() instanceof BlockItem) || !clazz.isInstance(((BlockItem) stack.getItem()).getBlock()))
                continue;
            return i;
        }
        return -1;
    }

    public static int getRodSlot() {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = mc.player.getInventory().getStack(i);
            if (item.getItem() == Items.FISHING_ROD && item.getDamage() < 52) {
                return i;
            }
        }
        return -1;
    }

    public static int getElytra() {
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.getItem() == Items.ELYTRA && stack.getDamage() < 430) {
                return -2;
            }
        }
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.ELYTRA && s.getDamage() < 430) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }

    public static int getItemSlot(Item input) {
        if (input == mc.player.getOffHandStack().getItem()) return 999;
        for (int i = 36; i >= 0; i--) {
            final Item item = mc.player.getInventory().getStack(i).getItem();
            if (item == input) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return -1;
    }

    public static int getItemSlotHotbar(Item input) {
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).getItem() == input) return i;
        return -1;
    }

    public static int getPowderAtHotbar() {
        for (int i = 0; i < 9; ++i) if (mc.player.getInventory().getStack(i).getItem() == Items.GUNPOWDER) return i;
        return -1;
    }

    public static int getAmericanoAtHotbar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() == Items.HONEY_BOTTLE)) continue;
            if (!(itemStack.getName().getString().contains("Американо"))) continue;
            return i;
        }
        return -1;
    }


    public static int getCappuchinoAtHotbar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() == Items.HONEY_BOTTLE)) continue;
            if (!(itemStack.getName().getString().contains("Каппучино"))) continue;
            return i;
        }
        return -1;
    }
}

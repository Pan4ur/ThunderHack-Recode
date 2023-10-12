package dev.thunderhack.utils.player;

import dev.thunderhack.modules.Module;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import org.jetbrains.annotations.NotNull;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.ModuleManager;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class InventoryUtility {
    private static int cachedSlot = -1;

    public static int getItemCount(Item item) {
        if (Module.mc.player == null) return 0;

        int counter = 0;

        for (int i = 0; i <= 44; ++i) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(i);
            if (itemStack.getItem() != item) continue;
            counter += itemStack.getCount();
        }

        return counter;
    }

    public static SearchInvResult getCrystal() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        if (Module.mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            return new SearchInvResult(Module.mc.player.getInventory().selectedSlot, true, Module.mc.player.getMainHandStack());
        }

        return findItemInHotBar(Items.END_CRYSTAL);
    }

    public static SearchInvResult getXp() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        ItemStack stack = Module.mc.player.getMainHandStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ExperienceBottleItem) {
            return new SearchInvResult(Module.mc.player.getInventory().selectedSlot, true, stack);
        }

        return findItemInHotBar(Items.EXPERIENCE_BOTTLE);
    }

    public static SearchInvResult getAnchor() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        ItemStack stack = Module.mc.player.getMainHandStack();
        if (!stack.isEmpty() && stack.getItem().equals(Items.RESPAWN_ANCHOR)) {
            return new SearchInvResult(Module.mc.player.getInventory().selectedSlot, true, stack);
        }

        return findItemInHotBar(Items.RESPAWN_ANCHOR);
    }

    public static SearchInvResult getGlowStone() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        ItemStack stack = Module.mc.player.getMainHandStack();
        if (!stack.isEmpty() && stack.getItem().equals(Items.GLOWSTONE)) {
            return new SearchInvResult(Module.mc.player.getInventory().selectedSlot, true, stack);
        }

        return findItemInHotBar(Items.GLOWSTONE);
    }

    public static SearchInvResult getAxe() {
        if (Module.mc.player == null) return SearchInvResult.notFound();
        int slot = -1;
        float f = 1.0F;

        for (int b1 = 9; b1 < 45; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1 >= 36 ? b1 - 36 : b1);
            if (itemStack != null && itemStack.getItem() instanceof AxeItem axe) {
                float f1 = axe.getMaxDamage();
                f1 += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, itemStack);
                if (f1 > f) {
                    f = f1;
                    slot = b1;
                }
            }
        }

        if (slot >= 36) slot = slot - 36;

        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, Module.mc.player.getInventory().getStack(slot));
    }

    public static SearchInvResult getPickAxeHotbar() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        int slot = -1;
        float f = 1.0F;
        for (int b1 = 0; b1 < 9; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof PickaxeItem) {
                float f1 = 0;
                f1 += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
                if (f1 > f) {
                    f = f1;
                    slot = b1;
                }
            }
        }

        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, Module.mc.player.getInventory().getStack(slot));
    }

    public static SearchInvResult getPickAxe() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        int slot = -1;
        float f = 1.0F;
        for (int b1 = 9; b1 < 45; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof PickaxeItem) {
                float f1 = 0;
                f1 += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
                if (f1 > f) {
                    f = f1;
                    slot = b1;
                }
            }
        }

        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, Module.mc.player.getInventory().getStack(slot));
    }

    public static SearchInvResult getPickAxeHotBar() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        int slot = -1;
        float f = 1.0F;
        for (int b1 = 0; b1 < 9; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof PickaxeItem) {
                float f1 = 0;
                f1 += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
                if (f1 > f) {
                    f = f1;
                    slot = b1;
                }
            }
        }

        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, Module.mc.player.getInventory().getStack(slot));
    }

    public static SearchInvResult getSkull() {
        if (Module.mc.player == null) return SearchInvResult.notFound();
        int slot = -1;
        for (int b1 = 0; b1 < 9; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1);
            if (itemStack != null &&
                    (itemStack.getItem().equals(Items.SKELETON_SKULL)
                            || itemStack.getItem().equals(Items.WITHER_SKELETON_SKULL)
                            || itemStack.getItem().equals(Items.CREEPER_HEAD)
                            || itemStack.getItem().equals(Items.PLAYER_HEAD)
                            || itemStack.getItem().equals(Items.ZOMBIE_HEAD))) {
                slot = b1;
                break;
            }
        }
        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, Module.mc.player.getInventory().getStack(slot));
    }

    public static SearchInvResult getSword() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        int slot = -1;
        float f = 1.0F;
        for (int b1 = 9; b1 < 45; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof SwordItem sword) {
                float f1 = sword.getMaxDamage();
                f1 += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, itemStack);
                if (f1 > f) {
                    f = f1;
                    slot = b1;
                }
            }
        }

        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, Module.mc.player.getInventory().getStack(slot));
    }

    public static SearchInvResult getSwordHotBar() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        int slot = -1;
        float f = 1.0F;
        for (int b1 = 0; b1 < 9; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof SwordItem sword) {
                float f1 = sword.getMaxDamage();
                f1 += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, itemStack);
                if (f1 > f) {
                    f = f1;
                    slot = b1;
                }
            }
        }

        if (slot == -1) return SearchInvResult.notFound();
        return new SearchInvResult(slot, true, Module.mc.player.getInventory().getStack(slot));
    }

    // Needs rewrite
    @Deprecated
    public static int getElytra() {
        for (ItemStack stack : Module.mc.player.getInventory().armor) {
            if (stack.getItem() == Items.ELYTRA && stack.getDamage() < 430) {
                return -2;
            }
        }

        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = Module.mc.player.getInventory().getStack(i);
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

    @Deprecated
    public static int getItemSlot(Item input) {
        if (input == Module.mc.player.getOffHandStack().getItem()) return 999;
        for (int i = 36; i >= 0; i--) {
            final Item item = Module.mc.player.getInventory().getStack(i).getItem();
            if (item == input) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return -1;
    }

    public static SearchInvResult findInHotBar(Searcher searcher) {
        if (Module.mc.player != null) {
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = Module.mc.player.getInventory().getStack(i);
                if (searcher.isValid(stack)) {
                    return new SearchInvResult(i, true, stack);
                }
            }
        }

        return SearchInvResult.notFound();
    }

    public static SearchInvResult findItemInHotBar(List<Item> items) {
        return findInHotBar(stack -> items.contains(stack.getItem()));
    }

    public static SearchInvResult findItemInHotBar(Item... items) {
        return findItemInHotBar(Arrays.asList(items));
    }

    public static SearchInvResult findInInventory(Searcher searcher) {
        if (Module.mc.player != null) {
            for (int i = 36; i >= 0; i--) {
                ItemStack stack = Module.mc.player.getInventory().getStack(i);
                if (searcher.isValid(stack)) {
                    if (i < 9) i += 36;
                    return new SearchInvResult(i, true, stack);
                }
            }
        }

        return SearchInvResult.notFound();
    }

    public static SearchInvResult findItemInInventory(List<Item> items) {
        return findInInventory(stack -> items.contains(stack.getItem()));
    }

    public static SearchInvResult findItemInInventory(Item... items) {
        return findItemInInventory(Arrays.asList(items));
    }

    public static SearchInvResult findBlockInHotBar(@NotNull List<Block> blocks) {
        return findItemInHotBar(blocks.stream().map(Block::asItem).toList());
    }

    public static SearchInvResult findBlockInHotBar(Block... blocks) {
        return findItemInHotBar(Arrays.stream(blocks).map(Block::asItem).toList());
    }

    public static SearchInvResult findBlockInInventory(@NotNull List<Block> blocks) {
        return findItemInInventory(blocks.stream().map(Block::asItem).toList());
    }

    public static SearchInvResult findBlockInInventory(Block... blocks) {
        return findItemInInventory(Arrays.stream(blocks).map(Block::asItem).toList());
    }

    public static void saveSlot() {
        cachedSlot = Module.mc.player.getInventory().selectedSlot;
    }

    public static void returnSlot() {
        if (cachedSlot != -1)
            switchTo(cachedSlot);
        cachedSlot = -1;
    }

    public static void switchTo(int slot) {
        if (Module.mc.player == null || Module.mc.getNetworkHandler() == null) return;
        if (Module.mc.player.getInventory().selectedSlot == slot && ThunderHack.playerManager.serverSideSlot == slot) return;
        Module.mc.player.getInventory().selectedSlot = slot;
        Module.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    public static SearchInvResult getAntiWeaknessItem() {
        if (Module.mc.player == null) return SearchInvResult.notFound();

        Item mainHand = Module.mc.player.getMainHandStack().getItem();
        if (mainHand instanceof SwordItem
                || mainHand instanceof PickaxeItem
                || mainHand instanceof AxeItem
                || mainHand instanceof ShovelItem) {
            return new SearchInvResult(Module.mc.player.getInventory().selectedSlot, true, Module.mc.player.getMainHandStack());
        }

        return findInHotBar(
                itemStack -> itemStack.getItem() instanceof SwordItem
                        || itemStack.getItem() instanceof PickaxeItem
                        || itemStack.getItem() instanceof AxeItem
                        || itemStack.getItem() instanceof ShovelItem
        );
    }

    public static float getHitDamage(@NotNull ItemStack weapon, PlayerEntity ent) {
        if (Module.mc.player == null) return 0;
        float baseDamage = 1f;

        if (weapon.getItem() instanceof SwordItem swordItem)
            baseDamage = swordItem.getAttackDamage();

        if (weapon.getItem() instanceof AxeItem axeItem)
            baseDamage = axeItem.getAttackDamage();

        if (Module.mc.player.fallDistance > 0 || ModuleManager.criticals.isEnabled())
            baseDamage += baseDamage / 2f;

        baseDamage += EnchantmentHelper.getLevel(Enchantments.SHARPNESS, weapon);

        if (Module.mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
            int strength = Objects.requireNonNull(Module.mc.player.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier() + 1;
            baseDamage += 3 * strength;
        }

        // Reduce by resistance
        // baseDamage = resistanceReduction(target, damage);

        // Reduce by armour
        baseDamage = DamageUtil.getDamageLeft(baseDamage, ent.getArmor(), (float) ent.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

        // Reduce by enchants
        // damage = normalProtReduction(target, damage);

        return baseDamage;
    }

    public static SearchInvResult findBedInHotbar() {
        if (Module.mc.player == null) return SearchInvResult.notFound();
        for (int b1 = 0; b1 < 9; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1);
            if (itemStack != null && itemStack.getItem() instanceof BedItem)
                return new SearchInvResult(b1, true, Module.mc.player.getInventory().getStack(b1));
        }
        return SearchInvResult.notFound();
    }

    public static SearchInvResult findBed() {
        if (Module.mc.player == null) return SearchInvResult.notFound();
        for (int b1 = 9; b1 < 45; b1++) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(b1 >= 36 ? b1 - 36 : b1);
            if (itemStack != null && itemStack.getItem() instanceof BedItem)
                return new SearchInvResult(b1, true, Module.mc.player.getInventory().getStack(b1));
        }
        return SearchInvResult.notFound();
    }

    public static int getBedsCount() {
        if (Module.mc.player == null) return 0;

        int counter = 0;

        for (int i = 0; i <= 44; ++i) {
            ItemStack itemStack = Module.mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() instanceof BedItem)) continue;
            counter += itemStack.getCount();
        }

        return counter;
    }


    public interface Searcher {
        boolean isValid(ItemStack stack);
    }
}

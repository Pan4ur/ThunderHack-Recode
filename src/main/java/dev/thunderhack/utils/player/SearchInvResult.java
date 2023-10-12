package dev.thunderhack.utils.player;

import dev.thunderhack.modules.Module;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record SearchInvResult(int slot, boolean found, ItemStack stack) {
    private static final SearchInvResult NOT_FOUND_RESULT = new SearchInvResult(-1, false, null);

    public static SearchInvResult notFound() {
        return NOT_FOUND_RESULT;
    }

    public static @NotNull SearchInvResult inOffhand(ItemStack stack) {
        return new SearchInvResult(999, true, stack);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isHolding() {
        if (Module.mc.player == null) return false;

        return Module.mc.player.getInventory().selectedSlot == slot;
    }

    public void switchTo() {
        if (found && slot < 9)
            InventoryUtility.switchTo(slot);
    }
}

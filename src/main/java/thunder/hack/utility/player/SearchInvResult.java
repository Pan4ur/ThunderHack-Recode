package thunder.hack.utility.player;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import static thunder.hack.modules.Module.mc;

public record SearchInvResult(int slot, boolean found, ItemStack stack) {
    private static final SearchInvResult NOT_FOUND_RESULT = new SearchInvResult(-1, false, null);

    public static SearchInvResult notFound() {
        return NOT_FOUND_RESULT;
    }

    public static SearchInvResult inOffhand(ItemStack stack) {
        return new SearchInvResult(999, true, stack);
    }

    public void switchTo() {
        switchTo(InventoryUtility.SwitchMode.All);
    }

    public void switchTo(InventoryUtility.SwitchMode switchMode) {
        if (!found || mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.getInventory().selectedSlot == slot) return;

        if (switchMode == InventoryUtility.SwitchMode.Normal || switchMode == InventoryUtility.SwitchMode.All) {
            mc.player.getInventory().selectedSlot = slot;
        }
        if (switchMode == InventoryUtility.SwitchMode.Packet || switchMode == InventoryUtility.SwitchMode.All) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }
}

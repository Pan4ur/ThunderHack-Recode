package thunder.hack.modules.player;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class HotbarReplenish extends Module {
    public HotbarReplenish() {
        super("HotbarReplenish", Category.PLAYER);
    }

    private final Setting<Integer> delay = new Setting<>("Delay", 2, 0, 10);
    private final Setting<Integer> refillThr = new Setting<>("Threshold", 16, 5, 32);
    private final Timer timer = new Timer();

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null) return;
        if (!timer.passedMs(delay.getValue() * 1000)) return;
        for (int i = 0; i < 9; ++i) {
            if (!need(i)) continue;
            timer.reset();
            return;
        }
    }

    private boolean need(int slot) {
        ItemStack stack = mc.player.getInventory().getStack(slot);

        if (stack.isEmpty()) return false;
        if (!stack.isStackable()) return false;
        if (stack.getCount() >= refillThr.getValue()) return false;

        for (int i = 9; i < 36; ++i) {
            ItemStack item = mc.player.getInventory().getStack(i);
            if (item.isEmpty() || !canMerge(stack, item)) continue;
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            return true;
        }
        return false;
    }

    private boolean canMerge(ItemStack source, ItemStack stack) {
        return source.getItem() == stack.getItem() && source.getName().equals(stack.getName());
    }
}

package thunder.hack.features.modules.player;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class HotbarReplenish extends Module {
    public HotbarReplenish() {
        super("HotbarReplenish", Category.PLAYER);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.SWAP);
    private final Setting<Integer> delay = new Setting<>("Delay", 2, 0, 10);
    private final Setting<Integer> refillThr = new Setting<>("Threshold", 16, 1, 63);
    private final Setting<Integer> refillSmallThr = new Setting<>("PearlsThreshold", 4, 1, 15);

    private final Timer timer = new Timer();

    private enum Mode {
        QUICK_MOVE, SWAP
    }

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
        if (stack.isEmpty() || !stack.isStackable()) return false;

        if (stack.getMaxCount() == 16 && stack.getCount() > refillSmallThr.getValue()) return false;

        if (stack.getMaxCount() == 64 && stack.getCount() > refillThr.getValue()) return false;

        for (int i = 9; i < 36; ++i) {
            ItemStack item = mc.player.getInventory().getStack(i);
            if (item.isEmpty() || !canMerge(stack, item)) continue;

            boolean swap = mode.is(Mode.QUICK_MOVE);

            clickSlot(i, swap ? slot : 0, swap ? SlotActionType.SWAP : SlotActionType.QUICK_MOVE);
            return true;
        }
        return false;
    }

    private boolean canMerge(ItemStack source, ItemStack stack) {
        return source.getItem() == stack.getItem() && source.getName().equals(stack.getName());
    }
}

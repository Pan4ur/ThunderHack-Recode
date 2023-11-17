package thunder.hack.modules.misc;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChestStealer extends Module {

    public ChestStealer() {
        super("ChestStealer", Category.MISC);
    }

    private final Setting<Integer> delay = new Setting<>("Delay", 100, 0, 1000);
    private final Setting<Boolean> random = new Setting<>("Random", false);
    private final Setting<Boolean> close = new Setting<>("Close", false);
    private final Setting<Sort> sort = new Setting<>("Sort", Sort.None);

    public List<String> items = new ArrayList<>();
    private final Timer timer = new Timer();
    private final Random rnd = new Random();

    public void onRender3D(MatrixStack stack) {
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest) {
            for (int i = 0; i < chest.getInventory().size(); i++) {
                Slot slot = chest.getSlot(i);
                if (slot.hasStack() && isAllowed(slot.getStack()) && timer.every(delay.getValue() + (random.getValue() ? rnd.nextInt(delay.getValue()) : 0)))
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
            if (isContainerEmpty(chest) && close.getValue())
                mc.player.currentScreenHandler.onClosed(mc.player);
        }
    }

    private boolean isAllowed(ItemStack stack) {
        boolean allowed = items.contains(stack.getItem().getTranslationKey().replace("block.minecraft.", "").replace("item.minecraft.", ""));
        return switch (sort.getValue()) {
            case None -> true;
            case WhiteList -> allowed;
            default -> !allowed;
        };
    }

    private boolean isContainerEmpty(GenericContainerScreenHandler container) {
        for(int i = 0; i < (container.getInventory().size() == 90 ? 54 : 27); i++)
            if (container.getSlot(i).hasStack()) return false;
        return true;
    }

    private enum Sort {None, WhiteList, BlackList}
}
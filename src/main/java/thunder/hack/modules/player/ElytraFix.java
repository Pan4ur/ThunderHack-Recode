package thunder.hack.modules.player;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.utility.Timer;

public class ElytraFix extends Module {
    public ElytraFix() {
        super("ElytraFix", Category.PLAYER);
    }

    private Timer delay = new Timer();

    @EventHandler
    public void onPlayerEvent(PlayerUpdateEvent event){
        ItemStack stack = mc.player.currentScreenHandler.getCursorStack();
        if (stack.getItem() instanceof ArmorItem && delay.passedMs(300)) {
            if (((ArmorItem) stack.getItem()).getType() == ArmorItem.Type.CHESTPLATE && mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
                int nullSlot = findNullSlot();
                boolean needDrop = nullSlot == 999;
                if (needDrop) nullSlot = 9;
                Command.sendMessage("[ElytraFix] fixed!");
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, nullSlot, 1, SlotActionType.PICKUP, mc.player);
                if (needDrop) mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, -999, 0, SlotActionType.PICKUP, mc.player);
                delay.reset();
            }
        }
    }

    public static int findNullSlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return 999;
    }
}

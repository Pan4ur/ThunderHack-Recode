package dev.thunderhack.modules.misc;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.player.InventoryUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import dev.thunderhack.ThunderHack;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

import static dev.thunderhack.modules.client.MainSettings.isRu;

public class AutoFish extends Module {
    public AutoFish() {
        super("AutoFish", Category.MISC);
    }

    private final Setting<Boolean> rodSave = new Setting<>("RodSave", true);
    private final Setting<Boolean> changeRod = new Setting<>("ChangeRod", false);
    private final Setting<Boolean> autoSell = new Setting<>("AutoSell", false);

    private boolean flag = false;
    private int rodSlot = -1;
    private final Timer timeout = new Timer();

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            disable("NPE protection");
            return;
        }
        rodSlot = InventoryUtility.findInHotBar(stack -> stack.getItem() instanceof FishingRodItem).slot();
    }

    @Override
    public void onDisable() {
        flag = false;
    }

    @Override
    public void onUpdate() {
        if (mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
            if (mc.player.getMainHandStack().getDamage() > 52) {
                if (rodSave.getValue() && !changeRod.getValue()) {
                    disable(isRu() ? "Удочка почти сломалась!" : "Saving the rod...");
                } else if (changeRod.getValue() && getRodSlot() != -1) {
                    sendMessage(isRu() ? "Свапнулся на новую удочку" : "Swapped to a new rod");
                    mc.player.getInventory().selectedSlot = getRodSlot();
                } else disable(isRu() ? "Удочка почти сломалась!" : "Saving the rod...");
            }
        }
        if (timeout.passedMs(60000)) {
            if (rodSlot == -1)
                rodSlot = InventoryUtility.findInHotBar(stack -> stack.getItem() instanceof FishingRodItem).slot();
            if (rodSlot != -1) {
                sendPacket(new UpdateSelectedSlotC2SPacket(rodSlot));
                mc.player.getInventory().selectedSlot = rodSlot;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                timeout.reset();
            }
        }

        if (mc.player.fishHook != null) {
            boolean caughtFish = mc.player.fishHook.getDataTracker().get(FishingBobberEntity.CAUGHT_FISH);
            if (!flag && caughtFish) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (autoSell.getValue()) {
                    if (timeout.passedMs(1000)) mc.player.networkHandler.sendChatCommand("sellfish");
                }

                ThunderHack.asyncManager.run(() -> mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND), 250);

                timeout.reset();
                flag = true;
            } else if (!caughtFish) flag = false;
        }
    }

    private int getRodSlot() {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = mc.player.getInventory().getStack(i);
            if (item.getItem() == Items.FISHING_ROD && item.getDamage() < 52) {
                return i;
            }
        }

        return -1;
    }
}

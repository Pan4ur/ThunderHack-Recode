package thunder.hack.modules.misc;

import thunder.hack.cmd.Command;
import thunder.hack.core.AsyncManager;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.InventoryUtil;
import thunder.hack.utility.Timer;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoFish extends Module {
    public AutoFish() {
        super("AutoFish", "AutoFish", Category.MISC);
    }

    public Setting<Boolean> rodSave = new Setting<>("RodSave", true);
    public Setting<Boolean> changeRod = new Setting<>("ChangeRod", false);
    public Setting<Boolean> autoSell = new Setting<>("AutoSell", false);

    private int rodSlot = -1;
    private final Timer timeout = new Timer();


    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            toggle();
            return;
        }
        rodSlot = InventoryUtil.findItem(FishingRodItem.class);
    }

    @Override
    public void onUpdate() {
        if (mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
            if (mc.player.getMainHandStack().getDamage() > 52) {
                if (rodSave.getValue() && !changeRod.getValue()) {
                    Command.sendMessage("Saving rod...");
                    toggle();
                } else if (changeRod.getValue() && InventoryUtil.getRodSlot() != -1) {
                    Command.sendMessage("Swapped to a new rod");
                    mc.player.getInventory().selectedSlot = (InventoryUtil.getRodSlot());
                } else {
                    Command.sendMessage("Saving rod...");
                    toggle();
                }
            }
        }
        if (timeout.passedMs(60000)) {
            if (rodSlot == -1)
                rodSlot = InventoryUtil.findItem(FishingRodItem.class);
            if (rodSlot != -1) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(rodSlot));
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
                    if (timeout.passedMs(1000)) {
                        mc.player.networkHandler.sendChatCommand("sellfish");
                    }
                }

                AsyncManager.run(() -> {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                }, 250);

                timeout.reset();
                flag = true;
            } else if (!caughtFish) {
                flag = false;
            }
        }
    }


    boolean flag = false;

    @Override
    public void onDisable(){
        flag = false;
    }

}

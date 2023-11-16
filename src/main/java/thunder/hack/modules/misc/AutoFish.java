package thunder.hack.modules.misc;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import thunder.hack.ThunderHack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;

import static thunder.hack.modules.client.MainSettings.isRu;

public class AutoFish extends Module {
    public AutoFish() {
        super("AutoFish", Category.MISC);
    }

    private final Setting<Boolean> rodSave = new Setting<>("RodSave", true);
    private final Setting<Boolean> changeRod = new Setting<>("ChangeRod", false);
    private final Setting<Boolean> autoSell = new Setting<>("AutoSell", false);

    private boolean flag = false;
    private final Timer timeout = new Timer();
    private final Timer cooldown = new Timer();


    @Override
    public void onEnable() {
        if (fullNullCheck())
            disable("NPE protection");
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
                    InventoryUtility.switchTo(getRodSlot());
                    cooldown.reset();
                } else disable(isRu() ? "Удочка почти сломалась!" : "Saving the rod...");
            }
        }

        if(!cooldown.passedMs(1000))
            return;

        if (timeout.passedMs(25000)) {
            sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            timeout.reset();
            cooldown.reset();
        }

        if (mc.player.fishHook != null) {
            boolean caughtFish = mc.player.fishHook.getDataTracker().get(FishingBobberEntity.CAUGHT_FISH);
            if (!flag && caughtFish) {
                catchFish();
                flag = true;
            } else if (!caughtFish) flag = false;
        }
    }

    private void catchFish() {
        ThunderHack.asyncManager.run(() -> {

            sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            if (autoSell.getValue() && timeout.passedMs(1000))
                mc.player.networkHandler.sendChatCommand("sellfish");

            try {
                Thread.sleep((int) MathUtility.random(899, 1399));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            timeout.reset();
        }, (int) MathUtility.random(199, 349));
    }

    private int getRodSlot() {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = mc.player.getInventory().getStack(i);
            if (item.getItem() == Items.FISHING_ROD && item.getDamage() < 52)
                return i;
        }
        return -1;
    }
}

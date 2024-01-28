package thunder.hack.modules.player;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import thunder.hack.ThunderHack;
import thunder.hack.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.utility.player.InventoryUtility;

import static thunder.hack.modules.client.MainSettings.isRu;

public class ElytraReplace extends Module {
    public ElytraReplace() {
        super("ElytraReplace", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        ItemStack is = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if(is.isOf(Items.ELYTRA) && is.getDamage() > 425){
            int result = InventoryUtility.getElytra();
            if (result >= 0) {
                clickSlot(result);
                clickSlot(6);
                clickSlot(result);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                ThunderHack.notificationManager.publicity("ElytraReplace", isRu() ? "Меняем элитру на новую!" : "Swapping the old elytra for a new one!",2, Notification.Type.SUCCESS);
                sendMessage(isRu() ? "Меняем элитру на новую!" : "Swapping the old elytra for a new one!");
            }
        }
    }
}

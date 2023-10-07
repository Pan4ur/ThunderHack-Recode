package thunder.hack.modules.misc;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;

public class AutoSoup extends Module {
    public AutoSoup() {
        super("AutoSoup", Category.MISC);
    }

    public Setting<Float> health = new Setting<>("TriggerHealth", 7f, 1f, 20f);

    @Override
    public void onUpdate() {
        if (mc.player.getHealth() <= health.getValue()) {
            int soupslot = InventoryUtility.findItemInHotBar(Items.MUSHROOM_STEW).slot();
            int currentslot = mc.player.getInventory().selectedSlot;
            if (soupslot != -1) {
                sendPacket(new UpdateSelectedSlotC2SPacket(soupslot));
                sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
                sendPacket(new UpdateSelectedSlotC2SPacket(currentslot));
            }
        }
    }
}

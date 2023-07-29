package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.player.PlayerUtil;

public class AutoSoup extends Module {
    public AutoSoup() {
        super("AutoSoup", Category.MISC);
    }

    public Setting<Float> thealth = new Setting<>("TriggerHealth", 7f, 1f, 20f);

    @Override
    public void onUpdate() {
        if (mc.player.getHealth() <= thealth.getValue()) {
            int soupslot = InventoryUtil.findSoupAtHotbar();
            int currentslot = mc.player.getInventory().selectedSlot;
            if (soupslot != -1) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(soupslot));
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtil.getWorldActionId(mc.world)));
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(currentslot));
            }
        }
    }
}

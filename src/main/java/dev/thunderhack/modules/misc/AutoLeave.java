package dev.thunderhack.modules.misc;

import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.setting.settings.Parent;
import dev.thunderhack.utils.player.InventoryUtility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import dev.thunderhack.ThunderHack;
import dev.thunderhack.modules.client.MainSettings;

public class AutoLeave extends Module {
    public AutoLeave() {
        super("AutoLeave", Category.MISC);
    }

    public final Setting<Parent> leaveIf = new Setting<>("Leave if", new Parent(false, 0));
    public final Setting<Boolean> playerNear = new Setting<>("PlayerNear", true).withParent(leaveIf);
    public final Setting<Boolean> low_hp = new Setting<>("LowHp", true).withParent(leaveIf);
    public final Setting<Boolean> totems = new Setting<>("Totems", true).withParent(leaveIf);
    public final Setting<Integer> totemsCount = new Setting("TotemsCount", 2, 0, 10, v -> totems.getValue());
    public final Setting<Float> leaveHp = new Setting("HP", 8.0f, 1f, 20.0f, v -> low_hp.getValue());
    public final Setting<Boolean> fastLeave = new Setting<>("InstantLeave", true).withParent(leaveIf);

    @Override
    public void onUpdate() {
        for (PlayerEntity pl : mc.world.getPlayers()) {
            if (pl != mc.player && !ThunderHack.friendManager.isFriend(pl) && playerNear.getValue()) {
                leave(MainSettings.isRu() ? "Ливнул т.к. рядом появился игрок" : "Logged out because there was a player");
            }
        }
        if (totems.getValue() && InventoryUtility.getItemCount(Items.TOTEM_OF_UNDYING) <= totemsCount.getValue())
            leave(MainSettings.isRu() ? "Ливнул т.к. кончились тотемы" : "Logged out because out of totems");
        if (mc.player.getHealth() < leaveHp.getValue() && low_hp.getValue())
            leave(MainSettings.isRu() ? "Ливнул т.к. мало хп" : "Logged out because ur hp is low");
    }

    public void leave(String message) {
        if (fastLeave.getValue()) sendPacket(new UpdateSelectedSlotC2SPacket(228));
        else mc.player.networkHandler.getConnection().disconnect(Text.of("[AutoLeave] " + message));
        disable(message);
    }
}

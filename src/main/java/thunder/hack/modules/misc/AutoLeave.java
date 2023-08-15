package thunder.hack.modules.misc;

import net.minecraft.item.Items;
import thunder.hack.Thunderhack;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import thunder.hack.utility.player.InventoryUtility;

public class AutoLeave extends Module {
    public AutoLeave() {
        super("AutoLeave", Category.MISC);
    }


    public final Setting<Parent> leaveIf = new Setting<>("Leave if", new Parent(false,0));
    public final Setting<Boolean> playerNear = new Setting<>("PlayerNear", true).withParent(leaveIf);
    public final Setting<Boolean> low_hp = new Setting<>("LowHp", true).withParent(leaveIf);
    public final Setting<Boolean> totems = new Setting<>("Totems", true).withParent(leaveIf);
    public final Setting<Integer> totemsCount = new Setting("TotemsCount", 2, 0, 10,v-> totems.getValue());
    public final Setting<Float> leaveHp = new Setting("HP", 8.0f, 1f, 20.0f,v-> low_hp.getValue());
    public final Setting<Boolean> fastLeave = new Setting<>("InstantLeave", true).withParent(leaveIf);


    @Override
    public void onUpdate(){
        for(PlayerEntity pl : mc.world.getPlayers()){
            if(pl != mc.player && !Thunderhack.friendManager.isFriend(pl) && playerNear.getValue()){
                leave(MainSettings.isRu() ? "Ливнул т.к. рядом появился игрок" : "Leaved because there was a player");
            }
        }
        if(totems.getValue() && InventoryUtility.getItemCount(Items.TOTEM_OF_UNDYING) <= totemsCount.getValue()){
            leave(MainSettings.isRu() ? "Ливнул т.к. кончились тотемы" : "Leaved because out of totems");
        }
        if(mc.player.getHealth() < leaveHp.getValue() && low_hp.getValue()){
            leave(MainSettings.isRu() ? "Ливнул т.к. мало хп" : "Leaved because ur hp is low");
        }
    }

    public void leave(String message){
        if(fastLeave.getValue()){
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(228));
        } else {
            mc.player.networkHandler.getConnection().disconnect(Text.of("[AutoLeave] " + message));
        }
        disable(message);
    }
}

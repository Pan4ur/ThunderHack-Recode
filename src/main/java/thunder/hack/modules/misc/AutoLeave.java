package thunder.hack.modules.misc;

import thunder.hack.Thunderhack;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Parent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;

public class AutoLeave extends Module {
    public AutoLeave() {
        super("AutoLeave", Category.MISC);
    }


    public final Setting<Parent> leaveIf = new Setting<>("Leave if", new Parent(false,0));
    public final Setting<Boolean> playerNear = new Setting<>("PlayerNear", true).withParent(leaveIf);
    public final Setting<Boolean> low_hp = new Setting<>("LowHp", true).withParent(leaveIf);
    public final Setting<Float> leaveHp = new Setting("HP", 8.0f, 1f, 20.0f,v-> low_hp.getValue());
    public final Setting<Boolean> fastLeave = new Setting<>("InstantLeave", true).withParent(leaveIf);

    @Override
    public void onUpdate(){
        for(PlayerEntity pl : mc.world.getPlayers()){
            if(pl != mc.player && !Thunderhack.friendManager.isFriend(pl) && playerNear.getValue()){
                if(fastLeave.getValue()){
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(228));
                } else {
                    mc.player.networkHandler.getConnection().disconnect(Text.of("[AutoLeave] Ливнул т.к. рядом появился игрок"));
                }
                disable();
            }
        }
        if(mc.player.getHealth() < leaveHp.getValue() && low_hp.getValue()){
            if(fastLeave.getValue()){
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(228));
            } else {
                mc.player.networkHandler.getConnection().disconnect(Text.of("[AutoLeave] Ливнул т.к. мало хп"));
            }
            disable();
        }
    }

}

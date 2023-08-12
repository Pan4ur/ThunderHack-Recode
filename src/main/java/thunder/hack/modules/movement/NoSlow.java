package thunder.hack.modules.movement;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.setting.Setting;
import thunder.hack.modules.Module;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class NoSlow extends Module {
    public static Setting<mode> Mode = new Setting<>("Mode", mode.NCP);

    public NoSlow() {
        super("NoSlow", "NoSlow", Category.MOVEMENT);
    }


    private  boolean checkItem(Item itm){
        return itm == Items.GOLDEN_APPLE
                || itm ==  Items.CARROT
                || itm ==  Items.BEEF
                || itm ==  Items.PORKCHOP
                || itm ==  Items.COOKED_PORKCHOP
                || itm ==  Items.COOKED_BEEF
                || itm ==  Items.COOKED_CHICKEN
                || itm ==  Items.COOKED_COD
                || itm ==  Items.COOKIE
                || itm ==  Items.COOKED_MUTTON
                || itm ==  Items.COOKED_RABBIT
                || itm ==  Items.COOKED_SALMON
                || itm ==  Items.SALMON
                || itm ==  Items.RABBIT
                || itm ==  Items.APPLE
                || itm ==  Items.SHIELD;
    }


    @EventHandler
    public void onTick(EventSync event) {
        if (mc.player.isUsingItem()) {
            if (Mode.getValue() == mode.StrictNCP || Mode.getValue() == mode.NCP) {
                if (!mc.player.isRiding() && !mc.player.isSneaking()) {
                    if (Mode.getValue() == mode.StrictNCP && (checkItem(mc.player.getMainHandStack().getItem()) || checkItem(mc.player.getOffHandStack().getItem())))
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                }
            }

            if (Mode.getValue() == mode.Matrix) {
                if(!Thunderhack.moduleManager.get(Strafe.class).isEnabled()){
                    if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x *  0.3, mc.player.getVelocity().y,mc.player.getVelocity().z * 0.3);
                    } else if ((double) mc.player.fallDistance > 0.2) mc.player.setVelocity(mc.player.getVelocity().x *  0.95f, mc.player.getVelocity().y,mc.player.getVelocity().z * 0.95f);
                } else {
                    if (!mc.player.isOnGround() &&(double) mc.player.fallDistance > 0.2)
                        mc.player.setVelocity(mc.player.getVelocity().x *  0.7, mc.player.getVelocity().y,mc.player.getVelocity().z * 0.7f);
                }
            }
        }

    }

    public boolean canNoSlow() {
        return !mc.player.isOnGround() || !Thunderhack.moduleManager.get(Strafe.class).isEnabled();
    }

    public enum mode {
        NCP, StrictNCP, Matrix
    }
}

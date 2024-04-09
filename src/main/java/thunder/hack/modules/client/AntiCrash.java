package thunder.hack.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

public class AntiCrash extends Module { //https://github.com/Bram1903/MinecraftPlayerCrasher
    public final Setting<Boolean> debug = new Setting<>("Debug",false);
    final double d = Double.MAX_VALUE / 2;
    final float f = Float.MAX_VALUE / 2;
    final int i = Integer.MAX_VALUE / 2;
    public AntiCrash() {
        super("AntiCrash", Category.CLIENT);
    }
    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive receive) {
        if(receive.getPacket() instanceof ExplosionS2CPacket exp){
            if(
                    exp.getX() == d ||
                    exp.getY() == d ||
                    exp.getZ() == d ||
                    exp.getRadius() == f
            ){
                if(debug.getValue()){
                    sendMessage("ExplosionS2CPacket canceled");
                }
                receive.cancel();
            }
        }
        if(receive.getPacket() instanceof ParticleS2CPacket p){

            if(
                    p.getX() == d ||
                    p.getY() == d ||
                    p.getZ() == d ||
                    p.getSpeed() == f ||
                    p.getOffsetX() == f ||
                    p.getOffsetY() == f ||
                    p.getOffsetZ() == f
            ){
                if(debug.getValue()){
                    sendMessage("ParticleS2CPacket canceled");
                }
                receive.cancel();
            }
        }
        if(receive.getPacket() instanceof PlayerPositionLookS2CPacket pos){

            if(
                    pos.getX() == d ||
                    pos.getY() == d ||
                    pos.getZ() == d ||
                    pos.getYaw() == f ||
                    pos.getPitch() == f ||
                    pos.getTeleportId() == i
            )
            {
                if(debug.getValue()){
                    sendMessage("PlayerPositionLookS2CPacket canceled");
                }
                receive.cancel();
            }
        }

    }
}

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
    public AntiCrash() {
        super("AntiCrash", Category.CLIENT);
    }
    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive receive) {
        if(receive.getPacket() instanceof ExplosionS2CPacket exp){
            if(
                    exp.getX() > 1E9 ||
                            exp.getY() > 1E9 ||
                            exp.getZ() > 1E9 ||
                            exp.getRadius() > 1E9
            ){
                if(debug.getValue()){
                    sendMessage("ExplosionS2CPacket canceled");
                }
                receive.cancel();
            }
        }
        if(receive.getPacket() instanceof ParticleS2CPacket p){
            if(
                    p.getX() > 1E9 ||
                            p.getY() > 1E9 ||
                            p.getZ() > 1E9 ||
                            p.getSpeed() > 1E9 ||
                            p.getOffsetX() > 1E9 ||
                            p.getOffsetY() > 1E9 ||
                            p.getOffsetZ() > 1E9
            ){
                if(debug.getValue()){
                    sendMessage("ParticleS2CPacket canceled");
                }
                receive.cancel();
            }
        }
        else if(receive.getPacket() instanceof PlayerPositionLookS2CPacket pos){
            if(
                    pos.getX() > 1E9 ||
                            pos.getY() > 1E9 ||
                            pos.getZ() > 1E9 ||
                            pos.getYaw() > 1E9 ||
                            pos.getPitch() > 1E9
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

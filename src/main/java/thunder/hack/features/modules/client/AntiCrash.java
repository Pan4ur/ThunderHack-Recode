package thunder.hack.features.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class AntiCrash extends Module { //https://github.com/Bram1903/MinecraftPlayerCrasher
    public final Setting<Boolean> debug = new Setting<>("Debug", false);

    private Timer debugTimer = new Timer();

    public AntiCrash() {
        super("AntiCrash", Category.CLIENT);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive receive) {
        if (receive.getPacket() instanceof ExplosionS2CPacket exp && (exp.getX() > 1E9 || exp.getY() > 1E9 || exp.getZ() > 1E9 || exp.getRadius() > 1E9)) {
            if (debug.getValue() && debugTimer.passedMs(1000)) {
                sendMessage("ExplosionS2CPacket canceled");
                debugTimer.reset();
            }
            receive.cancel();
        } else if (receive.getPacket() instanceof ParticleS2CPacket p && (p.getX() > 1E9 || p.getY() > 1E9 || p.getZ() > 1E9 || p.getSpeed() > 1E9 || p.getOffsetX() > 1E9 || p.getOffsetY() > 1E9 || p.getOffsetZ() > 1E9)) {
            if (debug.getValue() && debugTimer.passedMs(1000)) {
                sendMessage("ParticleS2CPacket canceled");
                debugTimer.reset();
            }
            receive.cancel();
        } else if (receive.getPacket() instanceof PlayerPositionLookS2CPacket pos && (pos.getX() > 1E9 || pos.getY() > 1E9 || pos.getZ() > 1E9 || pos.getYaw() > 1E9 || pos.getPitch() > 1E9)) {
            if (debug.getValue() && debugTimer.passedMs(1000)) {
                sendMessage("PlayerPositionLookS2CPacket canceled");
                debugTimer.reset();
            }
            receive.cancel();
        }
    }
}
package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class Velocity2 extends Module {
    public Velocity2() {
        super("Velocity2", Category.MOVEMENT);
    }

    public Setting<Integer> flagPauseValue = new Setting<>("flagPauseValue", 50, 0, 5000);
    public Setting<Boolean> send = new Setting<>("send", false);


    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket pac) {
            if (pac.getId() == mc.player.getId()) {
                e.cancel();
                gotVelo = true;
            }
        }
        if(e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            flagTimer.reset();
        }
        if (!flagTimer.passedMs(flagPauseValue.getValue())) {
            gotVelo = false;
        }
    }

    boolean gotVelo = false;
    private Timer flagTimer = new Timer();

    @Override
    public void onEnable() {
        gotVelo = false;
        flagTimer.reset();
    }

    @Override
    public void onUpdate() {
        if (!flagTimer.passedMs(flagPauseValue.getValue())) {
            gotVelo = false;
            return;
        }
        if (gotVelo) {
            BlockPos pos = BlockPos.ofFloored(mc.player.getPos());
            sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
            gotVelo = false;
        }
    }
}

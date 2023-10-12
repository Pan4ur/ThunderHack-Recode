package dev.thunderhack.modules.movement;

import dev.thunderhack.event.events.EventSync;
import dev.thunderhack.event.events.PacketEvent;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.player.MovementUtility;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import dev.thunderhack.ThunderHack;

public class TickShift extends Module {
    public TickShift() {
        super("TickShift", Category.MOVEMENT);
    }

    public Setting<Float> timer = new Setting<>("Timer", 2.0f, 0.1f, 100.0f);
    public Setting<Integer> packets = new Setting<>("Packets", 20, 0, 1000);
    public Setting<Integer> lagTime = new Setting<>("LagTime", 1000, 0, 10000);
    public Setting<Boolean> sneaking = new Setting<>("Sneaking", false);
    public Setting<Boolean> cancelGround = new Setting<>("CancelGround", false);
    public Setting<Boolean> cancelRotations = new Setting<>("CancelRotation", false);
    private static double prevPosX, prevPosY, prevPosZ;
    private dev.thunderhack.utils.Timer lagTimer = new Timer();
    private static float yaw, pitch;
    private int ticks;

    @EventHandler
    public void onSync(EventSync e) {
        if (notMoving()) {
            ThunderHack.TICK_TIMER = 1.0f;
            ticks = ticks >= packets.getValue() ? packets.getValue() : ticks + 1;
        }

        prevPosX = mc.player.getX();
        prevPosY = mc.player.getY();
        prevPosZ = mc.player.getZ();
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        if (mc.player == null || mc.world == null || !lagTimer.passedMs(lagTime.getValue())) {
            reset();
        } else if (ticks <= 0 || !MovementUtility.isMoving() || !sneaking.getValue() && mc.player.isSneaking()) {
            ThunderHack.TICK_TIMER = 1.0f;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket)
            lagTimer.reset();
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof PlayerMoveC2SPacket.Full)
            shift(e, true);
        if (e.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround)
            shift(e, true);

        if (e.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround pac)
            if (cancelRotations.getValue() && (cancelGround.getValue() || pac.isOnGround() == mc.player.isOnGround()))
                e.cancel();
            else shift(e, false);

        if (e.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly)
            if (cancelGround.getValue()) e.cancel();
            else shift(e, false);
    }

    @Override
    public String getDisplayInfo() {
        return ticks + "";
    }

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        reset();
    }

    private static boolean notMoving() {
        return prevPosX == mc.player.getX() && prevPosY == mc.player.getY() && prevPosZ == mc.player.getZ() && yaw == mc.player.getYaw() && pitch == mc.player.getPitch();
    }

    private void shift(PacketEvent.Send event, boolean moving) {
        if (event.isCancelled()) return;
        if (moving && MovementUtility.isMoving() &&ticks > 0 && (sneaking.getValue() || !mc.player.isSneaking()))
            ThunderHack.TICK_TIMER = timer.getValue();
        ticks = ticks <= 0 ? 0 : ticks - 1;
    }

    public void reset() {
        ThunderHack.TICK_TIMER = 1.0f;
        ticks = 0;
    }
}

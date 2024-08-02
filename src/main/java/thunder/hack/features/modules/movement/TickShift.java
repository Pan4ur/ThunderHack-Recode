package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.MovementUtility;

public class TickShift extends Module {
    public TickShift() {
        super("TickShift", Category.MOVEMENT);
    }

    private final Setting<Float> timer = new Setting<>("Timer", 2.0f, 0.1f, 100.0f);
    private final Setting<Integer> packets = new Setting<>("Packets", 20, 0, 1000);
    private final Setting<Integer> lagTime = new Setting<>("LagTime", 1000, 0, 10000);
    private final Setting<Boolean> sneaking = new Setting<>("Sneaking", false);
    private final Setting<Boolean> cancelGround = new Setting<>("CancelGround", false);
    private final Setting<Boolean> cancelRotations = new Setting<>("CancelRotation", false);

    private static double prevPosX, prevPosY, prevPosZ;
    private Timer lagTimer = new Timer();
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

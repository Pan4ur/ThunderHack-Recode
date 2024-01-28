package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.modules.client.MainSettings.isRu;

public class Timer extends Module {
    private static final Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL);
    public static final Setting<Float> speed = new Setting<>("Speed", 2.0f, 0.1f, 10.0f, v -> mode.getValue() != Mode.TICKSHIFT);
    private final Setting<Integer> shiftTicks = new Setting<>("ShiftTicks", 10, 1, 40, v -> mode.getValue() == Mode.TICKSHIFT);
    private static final Setting<Float> addOnTheMove = new Setting<>("addOnTheMove", 0.0f, 0.0f, 1.0f, v -> mode.getValue() == Mode.SMART);
    private static final Setting<Float> decreaseRate = new Setting<>("decreaseRate", 1.0f, 0.5f, 3.0f, v -> mode.getValue() == Mode.SMART);
    private final Setting<Bind> boostKey = new Setting<>("BoostKey", new Bind(-1, false, false), v -> mode.getValue() == Mode.GrimFunnyGame);

    public static float violation = 0.0f;
    private static double prevPosX;
    private static double prevPosY;
    private static double prevPosZ;

    private static float yaw;
    private static float pitch;

    private long cancelTime;
    public static double value;
    private CommonPingS2CPacket pingPacket;

    public Timer() {
        super("Timer", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        ThunderHack.TICK_TIMER = 1f;
        if (mode.getValue() == Mode.GrimFunnyGame) {
            violation = 40f;
            cancelTime = System.currentTimeMillis();
        }

    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1f;
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.SMART) {
            if (!MovementUtility.isMoving()) {
                ThunderHack.TICK_TIMER = 1f;
                return;
            }

            ThunderHack.TICK_TIMER = speed.getValue();
            if (ThunderHack.TICK_TIMER <= 1.0f) {
                return;
            }
            if (violation < 90f / speed.getValue()) {
                violation += decreaseRate.getValue();
                violation = MathHelper.clamp(violation, 0.0f, 100f / speed.getValue());
            } else
                disable(isRu() ? "Заряд таймера кончился! Отключаю.." : "Timer's out of charge! Disabling..");
        } else if (mode.getValue() == Mode.NORMAL) {
            ThunderHack.TICK_TIMER = speed.getValue();
        } else {
            if ( violation > 39f || !InputUtil.isKeyPressed(mc.getWindow().getHandle(), boostKey.getValue().getKey())) {
                ThunderHack.TICK_TIMER = 1f;
                return;
            }

            ThunderHack.TICK_TIMER = speed.getValue();
            violation += 0.15f;
            violation = MathHelper.clamp(violation, 0.0f, 40);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (mode.getValue() == Mode.GrimFunnyGame) {
            if (System.currentTimeMillis() - cancelTime > 55000) {
                sendMessage("Resetting..");
                cancelTime = System.currentTimeMillis();
                violation = 40f;
                if (pingPacket != null)
                    pingPacket.apply(mc.player.networkHandler);
            }
            if (e.getPacket() instanceof CommonPingS2CPacket) {
                pingPacket = e.getPacket();
                violation -= 0.8f;
                violation = MathHelper.clamp(violation, 0.0f, 100f / speed.getValue());
                e.cancel();
            }
            if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
                violation = 40f;
                disable(isRu() ? "Отключён т.к. ты получил велосити!" : "Disabled because you got velocity packet!");
            }
        }
    }

    @EventHandler
    public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
        if (mode.getValue() == Mode.TICKSHIFT) {
            int status = MathUtility.clamp((int) (100 - Math.min(violation, 100)), 0, 100);

            if (status < 90f) {
                disable(isRu() ? "Перед повторным использованием необходимо постоять на месте!" : "Standing still is required before reuse!");
                return;
            }
            event.cancel();
            event.setIterations(shiftTicks.getValue());
            violation = 40f;
            disable(isRu() ? "Тики пропущены! Отключаю.." : "Ticks shifted! Disabling..");
        }
    }

    public static void onEntitySync(EventSync e) {
        if (mode.getValue() == Mode.GrimFunnyGame) return;
        violation = notMoving() ? (float) (violation - (decreaseRate.getValue() + 0.4)) : violation - (addOnTheMove.getValue() / 10.0f);
        violation = (float) MathHelper.clamp(violation, 0.0, Math.floor(100f / ThunderHack.TICK_TIMER));
        prevPosX = mc.player.getX();
        prevPosY = mc.player.getY();
        prevPosZ = mc.player.getZ();
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
    }

    private static boolean notMoving() {
        return prevPosX == mc.player.getX() && prevPosY == mc.player.getY() && prevPosZ == mc.player.getZ() && yaw == mc.player.getYaw() && pitch == mc.player.getPitch();
    }

    public enum Mode {
        NORMAL,
        SMART,
        TICKSHIFT,
        GrimFunnyGame
    }
}

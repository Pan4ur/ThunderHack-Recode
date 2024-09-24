package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class Timer extends Module {
    public Timer() {
        super("Timer", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);
    private final Setting<Boolean> old = new Setting<>("Old", false, v -> mode.is(Mode.Matrix));
    public final Setting<Float> speed = new Setting<>("Speed", 2.0f, 0.1f, 10.0f, v -> mode.getValue() != Mode.Shift);
    private final Setting<Integer> shiftTicks = new Setting<>("ShiftTicks", 10, 1, 40, v -> mode.getValue() == Mode.Shift);
    private final Setting<Bind> boostKey = new Setting<>("BoostKey", new Bind(-1, false, false), v -> mode.getValue() == Mode.Grim);
    private final Setting<OnFlag> onFlag = new Setting<>("OnFlag", OnFlag.Reset);

    public static float energy, yaw, pitch;
    private static double prevPosX, prevPosY, prevPosZ;
    private long cancelTime;

    @Override
    public void onEnable() {
        ThunderHack.TICK_TIMER = 1f;
        if (!mode.is(Mode.Matrix))
            energy = 0f;

        if (mode.is(Mode.Grim))
            cancelTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1f;
    }

    @Override
    public void onUpdate() {
        switch (mode.getValue()) {
            case Normal -> ThunderHack.TICK_TIMER = speed.getValue();
            case Matrix -> {
                if (!MovementUtility.isMoving()) {
                    ThunderHack.TICK_TIMER = 1f;
                    return;
                }

                ThunderHack.TICK_TIMER = Math.max(speed.getValue(), 1f);

                if (energy > 0) {
                    energy = MathHelper.clamp(energy - ((0.1f * speed.getValue()) - 0.1f), 0f, 1f);
                } else
                    disable(isRu() ? "Заряд таймера кончился! Отключаю.." : "Timer's out of charge! Disabling..");
            }

            case Grim -> {
                if (energy <= 0 || !isKeyPressed(boostKey) || ThunderHack.core.getSetBackTime() < 2000) {
                    ThunderHack.TICK_TIMER = 1f;
                    return;
                }

                ThunderHack.TICK_TIMER = Math.max(speed.getValue(), 1f);
                energy = MathHelper.clamp(energy - ((0.0025f * speed.getValue()) - 0.0025f), 0f, 1f);
            }
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (mode.is(Mode.Grim)) {
            if (e.getPacket() instanceof CommonPingS2CPacket
                    && ThunderHack.core.getSetBackTime() > 2000) {
                if (System.currentTimeMillis() - cancelTime > 25000) {
                    cancelTime = System.currentTimeMillis();
                    energy = 0f;
                    return;
                }

                if (!MovementUtility.isMoving())
                    energy = Math.clamp(energy + 0.005f, 0f, 1f);

                e.cancel();
            }
        }

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            switch (onFlag.getValue()) {
                case Reset -> {
                    ThunderHack.TICK_TIMER = 1f;
                    energy = 0;
                }
                case Disable -> {
                    energy = 0;
                    disable(isRu() ? "Отключён т.к. тебя флагнуло!" : "Disabled because you got flagged!");
                }
            }
        }

        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket velo
                && velo.getId() == mc.player.getId() && mode.is(Mode.Grim)) {
            ThunderHack.TICK_TIMER = 1f;
            energy = 0;
        }
    }

    @EventHandler
    public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
        if (mode.getValue() == Mode.Shift) {
            if (energy < 0.9f) {
                disable(isRu() ? "Перед повторным использованием необходимо постоять на месте!" : "Standing still is required before reuse!");
                return;
            }
            event.cancel();
            event.setIterations(shiftTicks.getValue());
            disable(isRu() ? "Тики пропущены! Отключаю.." : "Ticks shifted! Disabling..");
        }
    }

    public void onEntitySync() {
        if (mode.is(Mode.Matrix))
            energy = Math.clamp(notMoving() ? energy + 0.025f : energy - (old.getValue() ? 0.005f : 0), 0f, 1f);

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
        Normal,
        Matrix,
        Shift,
        Grim
    }

    public enum OnFlag {
        Disable,
        None,
        Reset
    }
}

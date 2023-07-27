package thunder.hack.modules.movement;


import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.PlayPingS2CPacket;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.util.math.MathHelper;
import thunder.hack.utility.math.MathUtil;
import thunder.hack.utility.player.MovementUtil;

import java.awt.*;

public class Timer extends Module {
    public static double value;
    public Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL);
    public Setting<Float> speed = new Setting("Speed", 2.0f, 0.1f, 10.0f, v -> mode.getValue() != Mode.TICKSHIFT);
    public Setting<Float> shiftTicks = new Setting("ShiftTicks", 10.0F, 1F, 40f, v -> mode.getValue() == Mode.TICKSHIFT);
    public Setting<Float> addOnTheMove = new Setting("addOnTheMove", 0.0f, 0.0f, 1.0f, v -> mode.getValue() == Mode.SMART);
    public Setting<Float> decreaseRate = new Setting("decreaseRate", 1.0f, 0.5f, 3.0f, v -> mode.getValue() == Mode.SMART);


    public Timer() {
        super("Timer", "Timer", Category.MOVEMENT);
    }


    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.SMART) {
            Thunderhack.TICK_TIMER = speed.getValue();
            if (Thunderhack.TICK_TIMER <= 1.0f) {
                return;
            }
            if (violation < 90f / speed.getValue()) {
                violation += decreaseRate.getValue();
                violation = MathHelper.clamp(violation, 0.0f, 100f / speed.getValue());
            } else {
                toggle();
            }
        }
        if (mode.getValue() == Mode.NORMAL) {
            Thunderhack.TICK_TIMER = speed.getValue();
        }
    }

    public static float violation = 0.0f;
    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;

    private float yaw;
    private float pitch;


    public void onEntitySync(EventSync e) {
        violation = notMoving() ? (float)(violation - (decreaseRate.getValue() + 0.4)) : violation - (addOnTheMove.getValue() / 10.0f);
        violation = (float) MathHelper.clamp(violation, 0.0, Math.floor(100f / Thunderhack.TICK_TIMER));
        prevPosX = mc.player.getX();
        prevPosY = mc.player.getY();
        prevPosZ = mc.player.getZ();
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
    }

    private boolean notMoving() {
        return prevPosX == mc.player.getX()
                && prevPosY == mc.player.getY()
                && prevPosZ == mc.player.getZ()
                && yaw == mc.player.getYaw()
                && pitch == mc.player.getPitch();
    }


    @Subscribe
    public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
        if (mode.getValue() == Mode.TICKSHIFT) {
            int status = MathUtil.clamp((int) ( 100 - Math.min(violation, 100)), 0, 100);

            if (status < 90f) {
                Command.sendMessage("Перед повторным использованием необходимо постоять на месте!");
                disable();
                return;
            }
            event.setCancelled(true);
            event.setIterations(shiftTicks.getValue().intValue());
            violation = 100;
            disable();
        }
    }


    @Override
    public void onEnable() {
        Thunderhack.TICK_TIMER = 1f;
    }

    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = 1f;
    }


    public enum Mode {
        NORMAL,
        SMART,
        TICKSHIFT
    }
}

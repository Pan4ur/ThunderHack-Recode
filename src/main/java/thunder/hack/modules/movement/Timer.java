package thunder.hack.modules.movement;


import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.s2c.play.PlayPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.MovementUtility;

public class Timer extends Module {
    public static double value;
    public static Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL);
    public static Setting<Float> speed = new Setting("Speed", 2.0f, 0.1f, 10.0f, v -> mode.getValue() != Mode.TICKSHIFT);
    public Setting<Float> shiftTicks = new Setting("ShiftTicks", 10.0F, 1F, 40f, v -> mode.getValue() == Mode.TICKSHIFT);
    public static Setting<Float> addOnTheMove = new Setting("addOnTheMove", 0.0f, 0.0f, 1.0f, v -> mode.getValue() == Mode.SMART);
    public static Setting<Float> decreaseRate = new Setting("decreaseRate", 1.0f, 0.5f, 3.0f, v -> mode.getValue() == Mode.SMART);
    public Setting<Bind> boostKey = new Setting<>("BoostKey", new Bind(-1,false,false), v -> mode.getValue() == Mode.GrimFunnyGame);

    private long cancelTime;
    private PlayPingS2CPacket pingPacket;

    public Timer() {
        super("Timer", "Timer", Category.MOVEMENT);
    }


    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.SMART) {
            if(!MovementUtility.isMoving()){
                Thunderhack.TICK_TIMER = 1f;
                return;
            };
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
        } else if (mode.getValue() == Mode.NORMAL) {
            Thunderhack.TICK_TIMER = speed.getValue();
        } else {
            if(!MovementUtility.isMoving() || violation > 39f || !InputUtil.isKeyPressed(mc.getWindow().getHandle(),boostKey.getValue().getKey())) {
                Thunderhack.TICK_TIMER = 1f;
                return;
            }

            Thunderhack.TICK_TIMER = speed.getValue();
            violation += 0.15f;
            violation = MathHelper.clamp(violation, 0.0f, 40);
        }
    }

    public static float violation = 0.0f;
    private static double prevPosX;
    private static double prevPosY;
    private static double prevPosZ;

    private static float yaw;
    private static float pitch;


    public static void onEntitySync(EventSync e) {
        if(mode.getValue() == Mode.GrimFunnyGame) {
            return;
        }
        violation = notMoving() ? (float) (violation - (decreaseRate.getValue() + 0.4)) : violation - (addOnTheMove.getValue() / 10.0f);
        violation = (float) MathHelper.clamp(violation, 0.0, Math.floor(100f / Thunderhack.TICK_TIMER));
        prevPosX = mc.player.getX();
        prevPosY = mc.player.getY();
        prevPosZ = mc.player.getZ();
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
    }

    private static boolean notMoving() {
        return prevPosX == mc.player.getX() && prevPosY == mc.player.getY() && prevPosZ == mc.player.getZ() && yaw == mc.player.getYaw() && pitch == mc.player.getPitch();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e){
        if(mode.getValue() == Mode.GrimFunnyGame) {
            if (System.currentTimeMillis() - cancelTime > 55000) {
                Command.sendMessage("Resetting..");
                cancelTime = System.currentTimeMillis();
                violation = 40f;
                if(pingPacket != null)
                    pingPacket.apply(mc.player.networkHandler);
            }
            if (e.getPacket() instanceof PlayPingS2CPacket) {
                pingPacket = e.getPacket();
                violation -= 0.8f;
                violation = MathHelper.clamp(violation, 0.0f, 100f / speed.getValue());
                e.cancel();
            }
            if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
                violation = 40f;
            }
        }
    }

    @EventHandler
    public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
        if (mode.getValue() == Mode.TICKSHIFT) {
            int status = MathUtility.clamp((int) (100 - Math.min(violation, 100)), 0, 100);

            if (status < 90f) {
                Command.sendMessage("Перед повторным использованием необходимо постоять на месте!");
                disable();
                return;
            }
            event.setCancelled(true);
            event.setIterations(shiftTicks.getValue().intValue());
            violation = 40f;
            disable();
        }
    }


    @Override
    public void onEnable() {
        Thunderhack.TICK_TIMER = 1f;
        if(mode.getValue() == Mode.GrimFunnyGame) {
            violation = 40f;
            cancelTime = System.currentTimeMillis();
        }

    }

    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = 1f;
    }


    public enum Mode {
        NORMAL,
        SMART,
        TICKSHIFT,
        GrimFunnyGame
    }
}

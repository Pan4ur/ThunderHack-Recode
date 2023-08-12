package thunder.hack.modules.movement;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;


public class LongJump extends Module {
    public LongJump() {
        super("LongJump", Category.MOVEMENT);
    }
    public double speedXZ;
    public double distance;
    public int stage = 0;
    private final Setting<ModeEn> Mode = new Setting("Mode", ModeEn.Normal);
    public Setting<Boolean> usetimer = new Setting("Timer", true);
    public Setting<Boolean> reduction = new Setting<>("Reduction", true);
    public Setting<Boolean> jumpDisable = new Setting<>("JumpDisable", true);
    private final Setting<Float> timr = new Setting("TimerSpeed", 1.0F, 0.5F, 3.0F);
    private final Setting<Float> speed = new Setting("Speed", 4.48F, 0.0F, 10.0F);

    
    @EventHandler
    public void onMove(EventMove e) {
        if (Mode.getValue() == ModeEn.Normal) {
            doNormal(e);
        } else if (Mode.getValue() == ModeEn.Pause) {
            doPause(e);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            stage = 0;
            distance = 0.0;
            disable();
        }
    }

    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = 1f;
        speedXZ = 0;
        distance = 0;
        stage = 0;
    }

    @Override
    public void onEnable() {
        Thunderhack.TICK_TIMER = 1f;
        speedXZ = 0;
        distance = 0;
        stage = 0;
    }

    public double isJumpBoost() {
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            return 0.2;
        } else {
            return 0;
        }
    }
    
    public enum ModeEn {
        Normal,
        Pause
    }

    private boolean dropSync = false;

    private void doPause(EventMove eventPlayerMove) {
        if (MovementUtility.isMoving()) {
            if (usetimer.getValue()) {
                Thunderhack.TICK_TIMER = timr.getValue();
            }
            switch (stage) {
                case 0 -> {
                    speedXZ = speed.getValue() * MovementUtility.getBaseMoveSpeed();
                    distance = 0.0;
                    ++stage;
                }
                case 1 -> {
                    eventPlayerMove.cancel();
                    mc.player.setVelocity(mc.player.getVelocity().getX(),0.42 + isJumpBoost(),mc.player.getVelocity().getZ());
                    eventPlayerMove.set_y(0.42 + isJumpBoost());
                    speedXZ *= 2.149;
                    ++stage;
                }
                case 2 -> {
                    double d = 0.66 * (distance - MovementUtility.getBaseMoveSpeed());
                    speedXZ = distance - d;
                    ++stage;
                }
                case 3 -> {
                    if (mc.player.verticalCollision || mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.2, 0.0, -0.2).offset(0.0, mc.player.getVelocity().getY(), 0.0)).iterator().hasNext()) {
                        if(jumpDisable.getValue()){
                            disable();
                        }
                        stage = 0;
                        distance = 0.0;
                    }
                    speedXZ = distance - distance / 159.0;
                }
            }
        }
        speedXZ = Math.max(MovementUtility.getBaseMoveSpeed(), speedXZ);
        eventPlayerMove.cancel();
        MovementUtility.modifyEventSpeed(eventPlayerMove, speedXZ);
    }

    private void doNormal(EventMove eventPlayerMove) {
        if (MovementUtility.isMoving()) {
            if (usetimer.getValue()) {
                Thunderhack.TICK_TIMER = timr.getValue();
            }
            if (stage == 0) {
                speedXZ = speed.getValue() * MovementUtility.getBaseMoveSpeed();
            } else if (stage == 1) {
                eventPlayerMove.cancel();
                mc.player.setVelocity(mc.player.getVelocity().getX(),0.42 + isJumpBoost(),mc.player.getVelocity().getZ());
                eventPlayerMove.set_y(0.42 + isJumpBoost());
                speedXZ *= 2.149;
            } else if (stage == 2) {
                double d = 0.66 * (distance - MovementUtility.getBaseMoveSpeed());
                speedXZ = distance - d;
            } else {
                if (mc.player.verticalCollision || mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.2, 0.0, -0.2).offset(0.0, mc.player.getVelocity().getY(), 0.0)).iterator().hasNext()) {
                    if (!reduction.getValue()) {
                        dropSync = true;
                    } else {
                        stage = 0;
                    }
                    if(jumpDisable.getValue()){
                        disable();
                    }
                }
                speedXZ = distance - distance / 159.0;
            }
            speedXZ = Math.max(MovementUtility.getBaseMoveSpeed(), speedXZ);
            eventPlayerMove.cancel();
            MovementUtility.modifyEventSpeed(eventPlayerMove, speedXZ);
            ++stage;
        }
    }

    @EventHandler
    public void onEntitySync(EventSync eventSync) {
        if(Mode.getValue() == ModeEn.Normal || Mode.getValue() == ModeEn.Pause) {
            if (MovementUtility.isMoving()) {
                double d = mc.player.getX() - mc.player.prevX;
                double d2 = mc.player.getZ() - mc.player.prevZ;
                distance = Math.sqrt(d * d + d2 * d2);
            } else {
                eventSync.cancel();
                stage = 0;
                distance = 0.0;
            }
            if (dropSync) {
                dropSync = false;
                eventSync.cancel();
                stage = 0;
            }
        }
    }
}

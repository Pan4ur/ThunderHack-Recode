package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class LongJump extends Module {
    public LongJump() {
        super("LongJump", Category.MOVEMENT);
    }

    private final Setting<Boolean> useTimer = new Setting<>("Timer", false);
    private final Setting<Boolean> jumpDisable = new Setting<>("JumpDisable", true);
    private final Setting<Float> timerValue = new Setting<>("TimerSpeed", 1.0F, 0.5F, 3.0F, v -> useTimer.getValue());
    private final Setting<Float> speed = new Setting<>("Speed", 1.35F, 0.1F, 10.0F);
    private final Setting<Float> maxDistance = new Setting<>("MaxDistance", 10f, 5f, 40f);

    private float plannedSpeed, realSpeed;
    private int stage = 0;
    private Vec3d prevPosition;

    @EventHandler
    public void onMove(EventMove e) {
        if (prevPosition != null && mc.player.getPos().squaredDistanceTo(prevPosition) > maxDistance.getPow2Value())
            disable(isRu() ? "Прыжок выполнен! Отключаю.." : "Jump complete! Disabling..");

        if (MovementUtility.isMoving()) {
            if (useTimer.getValue())
                ThunderHack.TICK_TIMER = timerValue.getValue();

            switch (stage) {
                case 0 -> {
                    plannedSpeed = (float) (speed.getValue() * MovementUtility.getBaseMoveSpeed());
                    realSpeed = 0f;
                    ++stage;
                }
                case 1 -> {
                    mc.player.setVelocity(mc.player.getVelocity().getX(), 0.42 + isJumpBoost(), mc.player.getVelocity().getZ());
                    e.setY(0.42 + isJumpBoost());
                    plannedSpeed *= 2.149f;
                    ++stage;
                }
                case 2 -> {
                    double d = 0.66f * (realSpeed - MovementUtility.getBaseMoveSpeed());
                    plannedSpeed = (float) (realSpeed - d);
                    ++stage;
                }
                case 3 -> {
                    if (mc.player.verticalCollision || mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.2, 0.0, -0.2).offset(0.0, mc.player.getVelocity().getY(), 0.0)).iterator().hasNext()) {
                        if (jumpDisable.getValue())
                            disable(isRu() ? "Прыжок выполнен! Отключаю.." : "Jump complete! Disabling..");
                        stage = 0;
                        realSpeed = 0f;
                    }
                    plannedSpeed = realSpeed - realSpeed / 159f;
                }
            }
        }
        plannedSpeed = (float) Math.max(MovementUtility.getBaseMoveSpeed(), plannedSpeed);

        MovementUtility.modifyEventSpeed(e, plannedSpeed);
        e.cancel();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket)
            disable(isRu() ? "Тебя флагнуло! Отключаю.." : "You've been flagged! Disabling..");
    }

    @Override
    public void onDisable() {
        resetValues();
    }

    @Override
    public void onEnable() {
        resetValues();
    }

    public void resetValues() {
        prevPosition = mc.player.getPos();
        ThunderHack.TICK_TIMER = 1f;
        plannedSpeed = 0;
        realSpeed = 0;
        stage = 0;
    }

    public float isJumpBoost() {
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) return 0.2f;
        else return 0f;
    }

    @EventHandler
    public void onEntitySync(EventSync eventSync) {
        if (MovementUtility.isMoving())
            realSpeed = (float) Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);
        else resetValues();
    }
}

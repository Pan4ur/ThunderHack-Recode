package thunder.hack.modules.movement;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.MovementUtil;
import net.minecraft.entity.effect.StatusEffects;

public class Speed extends Module {

    public Speed() {
        super("Speed", "спиды", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    public Setting<Boolean> useTimer = new Setting<>("Use Timer", false);

    public boolean flip;
    public double baseSpeed;
    private int stage, ticks;
    public enum Mode {StrictStrafe, MatrixJB, NCP}

    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = 1f;
    }

    @Override
    public void onEnable() {
        stage = 1;
        ticks = 0;
        baseSpeed = 0.2873D;
    }

    @Subscribe
    public void onSync(EventSync event) {
        if (mode.getValue() == Mode.MatrixJB) {
            if (MovementUtil.isMoving() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)).iterator().hasNext() && !flip) {
                mc.player.setOnGround(true);
                mc.player.jump();
            }
            if (mc.player.fallDistance > 0) flip = true;
            if (mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0,  -0.2, 0.0)).iterator().hasNext() && flip) flip = false;
        }
    }

    @Subscribe
    public void onMove(EventMove event) {
        if (mode.getValue() == Mode.MatrixJB) return;
        if (mc.player.getAbilities().flying) return;
        if (mc.player.isFallFlying()) return;
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
        if (event.isCancelled()) return;
        event.setCancelled(true);

        if (MovementUtil.isMoving()) {
            Thunderhack.TICK_TIMER = useTimer.getValue() ? 1.088f : 1f;
            if (stage == 1 && mc.player.isOnGround()) {
                mc.player.setVelocity(mc.player.getVelocity().x, MovementUtil.getJumpSpeed(), mc.player.getVelocity().z);
                event.setY(MovementUtil.getJumpSpeed());
                baseSpeed *= 2.149;
                stage = 2;
            } else if (stage == 2) {
                baseSpeed = Thunderhack.playerManager.currentPlayerSpeed - (0.66 * (Thunderhack.playerManager.currentPlayerSpeed - MovementUtil.getBaseMoveSpeed()));
                stage = 3;
            } else {
                if ((mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().getY(), 0.0)).iterator().hasNext() || mc.player.verticalCollision))
                    stage = 1;
                baseSpeed = Thunderhack.playerManager.currentPlayerSpeed - Thunderhack.playerManager.currentPlayerSpeed / 159.0D;
            }

            baseSpeed = Math.max(baseSpeed, MovementUtil.getBaseMoveSpeed());

            double baseStrictSpeed = mode.getValue() == Mode.StrictStrafe || mc.player.input.movementForward < 1 ? 0.465 : 0.576;
            double baseRestrictedSpeed = mode.getValue() == Mode.StrictStrafe || mc.player.input.movementForward < 1 ? 0.44 : 0.57;

            if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                baseStrictSpeed *= 1 + (0.2 * (amplifier + 1));
                baseRestrictedSpeed *= 1 + (0.2 * (amplifier + 1));
            }

            if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                baseStrictSpeed /= 1 + (0.2 * (amplifier + 1));
                baseRestrictedSpeed /= 1 + (0.2 * (amplifier + 1));
            }

            baseSpeed = Math.min(baseSpeed, ticks > 25 ? baseStrictSpeed : baseRestrictedSpeed);

            if (ticks++ > 50) ticks = 0;

            MovementUtil.modifyEventSpeed(event, baseSpeed);
        } else {
            Thunderhack.TICK_TIMER = 1f;
            event.set_x(0);
            event.set_z(0);
        }
    }
}

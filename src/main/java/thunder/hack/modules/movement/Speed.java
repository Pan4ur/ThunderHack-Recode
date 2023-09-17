package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSprint;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.utility.player.MovementUtility.isMoving;

public class Speed extends Module {

    public Speed() {
        super("Speed", "спиды", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    public Setting<Boolean> useTimer = new Setting<>("Use Timer", false);
    public final Setting<Integer> hurttime = new Setting<>("Hurttime", 0, 0, 10, v-> mode.getValue() == Mode.MatrixDamage);

    public boolean flip;
    public double baseSpeed;
    private int stage, ticks;
    private thunder.hack.utility.Timer elytraDelay = new thunder.hack.utility.Timer();
    private thunder.hack.utility.Timer startDelay = new thunder.hack.utility.Timer();

    public enum Mode {
        StrictStrafe, MatrixJB, NCP, FGLowRider, MatrixDamage
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1f;
    }

    @Override
    public void onEnable() {
        stage = 1;
        ticks = 0;
        baseSpeed = 0.2873D;
        startDelay.reset();
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mode.getValue() == Mode.MatrixJB) {
            if (MovementUtility.isMoving() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)).iterator().hasNext() && !flip) {
                mc.player.setOnGround(true);
                mc.player.jump();
            }
            if (mc.player.fallDistance > 0) flip = true;
            if (mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, -0.2, 0.0)).iterator().hasNext() && flip)
                flip = false;
        }
    }


    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.FGLowRider) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
                return;
            }
            if (mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.29, 0, -0.29).offset(0.0, -3, 0.0f)).iterator().hasNext() && elytraDelay.passedMs(150) && startDelay.passedMs(500)) {
                int elytra = InventoryUtility.getElytra();
                if (elytra == -1) {
                    disable(MainSettings.isRu() ? "Для этого режима нужна элитра!" : "You need elytra for this mode!");
                } else {
                    Strafe.disabler(elytra);
                }
                mc.player.setVelocity(mc.player.getVelocity().getX(), 0f, mc.player.getVelocity().getZ());
                if (isMoving()) {
                    MovementUtility.setMotion(0.85);
                }
                elytraDelay.reset();
            }
        }
        if(mode.getValue() == Mode.MatrixDamage){
            if (MovementUtility.isMoving() && mc.player.hurtTime > hurttime.getValue()) {
                if (mc.player.isOnGround()) {
                    MovementUtility.setMotion(0.387f);
                } else if (mc.player.isTouchingWater()) {
                    MovementUtility.setMotion(0.346f);
                } else if (!mc.player.isOnGround()) {
                    MovementUtility.setMotion(0.448f);
                }
            }
        }
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mode.getValue() == Mode.MatrixJB || mode.getValue() == Mode.FGLowRider || mode.getValue() == Mode.MatrixDamage) return;
        if (mc.player.getAbilities().flying) return;
        if (mc.player.isFallFlying()) return;
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
        if (event.isCancelled()) return;
        event.setCancelled(true);

        if (MovementUtility.isMoving()) {
            ThunderHack.TICK_TIMER = useTimer.getValue() ? 1.088f : 1f;
            if (stage == 1 && mc.player.isOnGround()) {
                mc.player.setVelocity(mc.player.getVelocity().x, MovementUtility.getJumpSpeed(), mc.player.getVelocity().z);
                event.setY(MovementUtility.getJumpSpeed());
                baseSpeed *= 2.149;
                stage = 2;
            } else if (stage == 2) {
                baseSpeed = ThunderHack.playerManager.currentPlayerSpeed - (0.66 * (ThunderHack.playerManager.currentPlayerSpeed - MovementUtility.getBaseMoveSpeed()));
                stage = 3;
            } else {
                if ((mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().getY(), 0.0)).iterator().hasNext() || mc.player.verticalCollision))
                    stage = 1;
                baseSpeed = ThunderHack.playerManager.currentPlayerSpeed - ThunderHack.playerManager.currentPlayerSpeed / 159.0D;
            }

            baseSpeed = Math.max(baseSpeed, MovementUtility.getBaseMoveSpeed());

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

            MovementUtility.modifyEventSpeed(event, baseSpeed);
        } else {
            ThunderHack.TICK_TIMER = 1f;
            event.set_x(0);
            event.set_z(0);
        }
    }
}

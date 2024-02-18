package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.AntiBot;
import thunder.hack.modules.combat.Aura;
import thunder.hack.setting.Setting;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.modules.client.MainSettings.isRu;
import static thunder.hack.modules.movement.Timer.violation;
import static thunder.hack.utility.player.MovementUtility.isMoving;

public class Speed extends Module {

    public Speed() {
        super("Speed", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    public Setting<Boolean> useTimer = new Setting<>("Use Timer", false);
    public final Setting<Integer> hurttime = new Setting<>("HurtTime", 0, 0, 10, v -> mode.getValue() == Mode.MatrixDamage);
    public final Setting<Float> boostFactor = new Setting<>("BoostFactor", 2f, 0f, 10f, v -> mode.getValue() == Mode.MatrixDamage);
    public final Setting<Boolean> allowOffGround = new Setting<>("AllowOffGround", true, v -> mode.getValue() == Mode.MatrixDamage);
    public final Setting<Integer> shiftTicks = new Setting<>("ShiftTicks", 0, 0, 10, v -> mode.getValue() == Mode.MatrixDamage);
    public final Setting<Boolean> withoutBot = new Setting<>("WithoutBot", false, v -> mode.getValue() == Mode.FunTime);

    public double baseSpeed;
    private int stage, ticks;
    private float prevForward = 0;
    private thunder.hack.utility.Timer elytraDelay = new thunder.hack.utility.Timer();
    private thunder.hack.utility.Timer startDelay = new thunder.hack.utility.Timer();

    public enum Mode {
        StrictStrafe, MatrixJB, NCP, ElytraLowHop, MatrixDamage, FunTime
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
            boolean closeToGround = false;

            for (VoxelShape a : mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.0, 0.0)))
                if (a != VoxelShapes.empty()) {
                    closeToGround = true;
                    break;
                }

            if (MovementUtility.isMoving() && closeToGround && mc.player.fallDistance <= 0) {
                ThunderHack.TICK_TIMER = 1f;
                mc.player.setOnGround(true);
                mc.player.jump();
            } else if (mc.player.fallDistance > 0 && useTimer.getValue()) {
                ThunderHack.TICK_TIMER = 1.088f;
                mc.player.addVelocity(0f, -0.003f, 0f);
            }
        }

        if (mode.getValue() == Mode.FunTime) {
            if((ModuleManager.antiBot.isDisabled() || ModuleManager.antiBot.remove.getValue()) && mc.player.age % 8 == 0 && !withoutBot.getValue())
                sendMessage(Formatting.GOLD + "Включи AntiBot и убери чек remove!");

            if (ModuleManager.aura.isDisabled() || Aura.target == null) {
                for (PlayerEntity ent : ThunderHack.asyncManager.getAsyncPlayers())
                    if (ent != mc.player && mc.player.squaredDistanceTo(ent) < 49 && AntiBot.bots.contains(ent) && !withoutBot.getValue()) {
                        mc.player.setPitch(95);
                        mc.player.setYaw(mc.player.getYaw() + MathUtility.random(-0.5f,0.5f));
                        break;
                    }
            }
        }
    }


    // засекайте до обновы wild client (not paste)
    @EventHandler
    public void modifyVelocity(EventPlayerTravel e) {
        if (mode.getValue() == Mode.FunTime && !e.isPre() && ThunderHack.core.getSetBackTime() > 1000) {
            for(PlayerEntity ent : ThunderHack.asyncManager.getAsyncPlayers()) {
                if(ent != mc.player && mc.player.squaredDistanceTo(ent) <= (AntiBot.bots.contains(ent) && !withoutBot.getValue() ? 9f : 2.25)) {
                    float p = mc.world.getBlockState(((IEntity) mc.player).thunderHack_Recode$getVelocityBP()).getBlock().getSlipperiness();
                    float f = mc.player.isOnGround() ? p * 0.91f : 0.91f;
                    float f2 = mc.player.isOnGround() ? p : 0.99f;
                    mc.player.setVelocity(mc.player.getVelocity().getX() / f * f2, mc.player.getVelocity().getY(), mc.player.getVelocity().getZ() / f * f2);
                    break;
                }
            }
        }
    }


    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.ElytraLowHop) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
                return;
            }
            if (mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.29, 0, -0.29).offset(0.0, -3, 0.0f)).iterator().hasNext() && elytraDelay.passedMs(150) && startDelay.passedMs(500)) {
                int elytra = InventoryUtility.getElytra();
                if (elytra == -1) disable(isRu() ? "Для этого режима нужна элитра!" : "You need elytra for this mode!");
                else Strafe.disabler(elytra);

                mc.player.setVelocity(mc.player.getVelocity().getX(), 0f, mc.player.getVelocity().getZ());
                if (isMoving())
                    MovementUtility.setMotion(0.85);
                elytraDelay.reset();
            }
        }
    }

    @EventHandler
    public void onPostPlayerUpdate(PostPlayerUpdateEvent event) {
        if (mode.getValue() == Mode.MatrixDamage) {
            if (MovementUtility.isMoving() && mc.player.hurtTime > hurttime.getValue()) {
                if (mc.player.isOnGround()) {
                    MovementUtility.setMotion(0.387f * boostFactor.getValue());
                } else if (mc.player.isTouchingWater()) {
                    MovementUtility.setMotion(0.346f * boostFactor.getValue());
                } else if (!mc.player.isOnGround() && allowOffGround.getValue()) {
                    MovementUtility.setMotion(0.448f * boostFactor.getValue());
                }

                if (shiftTicks.getValue() > 0 && (MathUtility.clamp((int) (100 - Math.min(violation, 100)), 0, 100) > 90)) {
                    event.cancel();
                    event.setIterations(shiftTicks.getValue());
                }
            }
        }
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mode.getValue() != Mode.NCP && mode.getValue() != Mode.StrictStrafe) return;
        if (mc.player.getAbilities().flying) return;
        if (mc.player.isFallFlying()) return;
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
        if (event.isCancelled()) return;
        event.cancel();

        if (MovementUtility.isMoving()) {
            ThunderHack.TICK_TIMER = useTimer.getValue() ? 1.088f : 1f;
            float currentSpeed = mode.getValue() == Mode.NCP && mc.player.input.movementForward <= 0 && prevForward > 0 ? ThunderHack.playerManager.currentPlayerSpeed * 0.66f : ThunderHack.playerManager.currentPlayerSpeed;
            if (stage == 1 && mc.player.isOnGround()) {
                mc.player.setVelocity(mc.player.getVelocity().x, MovementUtility.getJumpSpeed(), mc.player.getVelocity().z);
                event.setY(MovementUtility.getJumpSpeed());
                baseSpeed *= 2.149;
                stage = 2;
            } else if (stage == 2) {
                baseSpeed = currentSpeed - (0.66 * (currentSpeed - MovementUtility.getBaseMoveSpeed()));
                stage = 3;
            } else {
                if ((mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().getY(), 0.0)).iterator().hasNext() || mc.player.verticalCollision))
                    stage = 1;
                baseSpeed = currentSpeed - currentSpeed / 159.0D;
            }

            baseSpeed = Math.max(baseSpeed, MovementUtility.getBaseMoveSpeed());

            double ncpSpeed = mode.getValue() == Mode.StrictStrafe || mc.player.input.movementForward < 1 ? 0.465 : 0.576;
            double ncpBypassSpeed = mode.getValue() == Mode.StrictStrafe || mc.player.input.movementForward < 1 ? 0.44 : 0.57;

            if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                ncpSpeed *= 1 + (0.2 * (amplifier + 1));
                ncpBypassSpeed *= 1 + (0.2 * (amplifier + 1));
            }

            if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
                double amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
                ncpSpeed /= 1 + (0.2 * (amplifier + 1));
                ncpBypassSpeed /= 1 + (0.2 * (amplifier + 1));
            }

            baseSpeed = Math.min(baseSpeed, ticks > 25 ? ncpSpeed : ncpBypassSpeed);

            if (ticks++ > 50)
                ticks = 0;

            MovementUtility.modifyEventSpeed(event, baseSpeed);
            prevForward = mc.player.input.movementForward;
        } else {
            ThunderHack.TICK_TIMER = 1f;
            event.setX(0);
            event.setZ(0);
        }
    }
}

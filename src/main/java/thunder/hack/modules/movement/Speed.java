package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.modules.client.ClientSettings.isRu;
import static thunder.hack.modules.movement.Timer.violation;
import static thunder.hack.utility.player.MovementUtility.isMoving;

public class Speed extends Module {

    public Speed() {
        super("Speed", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.NCP);
    public Setting<Boolean> useTimer = new Setting<>("Use Timer", false);
    public Setting<Boolean> pauseInLiquids = new Setting<>("PauseInLiquids", false);
    public Setting<Boolean> pauseWhileSneaking = new Setting<>("PauseWhileSneaking", false);
    public final Setting<Integer> hurttime = new Setting<>("HurtTime", 0, 0, 10, v -> mode.is(Mode.MatrixDamage));
    public final Setting<Float> boostFactor = new Setting<>("BoostFactor", 2f, 0f, 10f, v -> mode.is(Mode.MatrixDamage) || mode.is(Mode.Vanilla));
    public final Setting<Boolean> allowOffGround = new Setting<>("AllowOffGround", true, v -> mode.is(Mode.MatrixDamage));
    public final Setting<Integer> shiftTicks = new Setting<>("ShiftTicks", 0, 0, 10, v -> mode.is(Mode.MatrixDamage));
    public final Setting<Integer> fireWorkSlot = new Setting<>("FireSlot", 1, 1, 9, v -> mode.getValue() == Mode.FireWork);
    public final Setting<Integer> delay = new Setting<>("Delay", 8, 1, 20, v -> mode.getValue() == Mode.FireWork);

    public double baseSpeed;
    private int stage, ticks;
    private float prevForward = 0;
    private thunder.hack.utility.Timer elytraDelay = new thunder.hack.utility.Timer();
    private thunder.hack.utility.Timer startDelay = new thunder.hack.utility.Timer();

    public enum Mode {
        StrictStrafe, MatrixJB, NCP, ElytraLowHop, MatrixDamage, GrimEntity, GrimEntity2, FireWork, Vanilla
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
        if (mc.player.isInFluid() && pauseInLiquids.getValue() || mc.player.isSneaking() && pauseWhileSneaking.getValue()) {
            return;
        }
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
    }


    @EventHandler
    public void modifyVelocity(EventPlayerTravel e) {
        if (mode.getValue() == Mode.GrimEntity && !e.isPre() && ThunderHack.core.getSetBackTime() > 1000) {
            for (PlayerEntity ent : ThunderHack.asyncManager.getAsyncPlayers()) {
                if (ent != mc.player && mc.player.squaredDistanceTo(ent) <= 2.25) {
                    float p = mc.world.getBlockState(((IEntity) mc.player).thunderHack_Recode$getVelocityBP()).getBlock().getSlipperiness();
                    float f = mc.player.isOnGround() ? p * 0.91f : 0.91f;
                    float f2 = mc.player.isOnGround() ? p : 0.99f;
                    mc.player.setVelocity(mc.player.getVelocity().getX() / f * f2, mc.player.getVelocity().getY(), mc.player.getVelocity().getZ() / f * f2);
                    break;
                }
            }
        }
        if (mode.getValue() == Mode.GrimEntity2 && !e.isPre() && ThunderHack.core.getSetBackTime() > 1000 && MovementUtility.isMoving()) {
            int collisions = 0;
            for (Entity ent : mc.world.getEntities())
                if (ent != mc.player && ent instanceof LivingEntity && mc.player.getBoundingBox().expand(1.0).intersects(ent.getBoundingBox()))
                    collisions++;

            double[] motion = MovementUtility.forward(0.08 * collisions);
            mc.player.addVelocity(motion[0], 0.0, motion[1]);
        }
    }


    @Override
    public void onUpdate() {
        if (mc.player.isInFluid() && pauseInLiquids.getValue() || mc.player.isSneaking() && pauseWhileSneaking.getValue()) {
            return;
        }

        if (mode.getValue() == Mode.FireWork) {
            ticks--;
            int ellySlot = InventoryUtility.getElytra();
            int fireSlot = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET).slot();
            boolean inOffHand = mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET;
            if (fireSlot == -1) {
                int fireInInv = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET).slot();
                if (fireInInv != -1)
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fireInInv, fireWorkSlot.getValue() - 1, SlotActionType.SWAP, mc.player);
            }

            if (ellySlot != -1 && (fireSlot != -1 || inOffHand) && !mc.player.isOnGround() && mc.player.fallDistance > 0) {
                if (ticks <= 0) {
                    if (ellySlot != -2) {
                        mc.interactionManager.clickSlot(0, ellySlot, 1, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
                    }
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    if (prevSlot != fireSlot && !inOffHand)
                        sendPacket(new UpdateSelectedSlotC2SPacket(fireSlot));
                    mc.interactionManager.interactItem(mc.player, inOffHand ? Hand.OFF_HAND : Hand.MAIN_HAND);
                    if (prevSlot != fireSlot && !inOffHand)
                        sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));

                    if (ellySlot != -2) {
                        mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(0, ellySlot, 1, SlotActionType.PICKUP, mc.player);
                    }
                    mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    ticks = delay.getValue();
                }
            }
        }

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
        if (mc.player.isInFluid() && pauseInLiquids.getValue() || mc.player.isSneaking() && pauseWhileSneaking.getValue()) {
            return;
        }
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

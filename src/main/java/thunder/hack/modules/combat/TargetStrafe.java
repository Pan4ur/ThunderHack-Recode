package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.ThunderHack;
import thunder.hack.core.Core;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.*;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

import static thunder.hack.utility.player.MovementUtility.isMoving;

public class TargetStrafe extends Module {
    public Setting<Boolean> jump = new Setting<>("Jump", true);
    public Setting<Float> distance = new Setting<>("Distance", 1.3F, 0.2F, 7f);

    private final Setting<Boost> boost = new Setting<>("Boost", Boost.None);
    public Setting<Float> setSpeed = new Setting<>("speed", 1.3F, 0.0F, 2f, v -> boost.getValue() == Boost.Elytra);
    private final Setting<Float> velReduction = new Setting<>("Reduction", 6.0f, 0.1f, 10f, v -> boost.getValue() == Boost.Damage);
    private final Setting<Float> maxVelocitySpeed = new Setting<>("MaxVelocity", 0.8f, 0.1f, 2f, v -> boost.getValue() == Boost.Damage);

    public static double oldSpeed, contextFriction, fovval;
    public static boolean needSwap, needSprintState, skip, switchDir, disabled;
    public static int noSlowTicks, jumpTicks, waterTicks;
    static long disableTime;
    private static TargetStrafe instance;

    public TargetStrafe() {
        super("TargetStrafe", Category.COMBAT);
        instance = this;
    }

    public static TargetStrafe getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        oldSpeed = 0;
        fovval = mc.options.getFovEffectScale().getValue();
        mc.options.getFovEffectScale().setValue(0d);
        skip = true;
    }

    public boolean canStrafe() {
        if (mc.player.isSneaking()) return false;
        if (mc.player.isInLava()) return false;
        if (ModuleManager.scaffold.isEnabled()) return false;
        if (ModuleManager.speed.isEnabled()) return false;
        if (mc.player.isSubmergedInWater() || waterTicks > 0) return false;
        return !mc.player.getAbilities().flying;
    }

    public boolean needToSwitch(double x, double z) {
        if (mc.player.horizontalCollision || ((mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed()) && jumpTicks <= 0)) {
            jumpTicks = 10;
            return true;
        }
        for (int i = (int) (mc.player.getY() + 4); i >= 0; --i) {
            BlockPos playerPos = new BlockPos((int) Math.floor(x), (int) Math.floor(i), (int) Math.floor(z));
            blockFIRE:
            {
                blockLAVA:
                {
                    if (mc.world.getBlockState(playerPos).getBlock().equals(Blocks.LAVA))
                        break blockLAVA;
                    if (!mc.world.getBlockState(playerPos).getBlock().equals(Blocks.FIRE))
                        break blockFIRE;
                }
                return true;
            }
            if (mc.world.isAir(playerPos)) continue;
            return false;
        }
        return false;
    }

    @Override
    public void onDisable() {
        mc.options.getFovEffectScale().setValue(fovval);
    }

    public double calculateSpeed(EventMove move) {
        jumpTicks--;
        float speedAttributes = getAIMoveSpeed();
        final float frictionFactor = mc.world.getBlockState(new BlockPos.Mutable().set(mc.player.getX(), getBoundingBox().getMin(Direction.Axis.Y) - move.getY(), mc.player.getZ())).getBlock().getSlipperiness() * 0.91F;
        float n6 = mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && mc.player.isUsingItem() ? 0.88f : (float) (oldSpeed > 0.32 && mc.player.isUsingItem() ? 0.88 : 0.91F);
        if (mc.player.isOnGround()) {
            n6 = frictionFactor;
        }
        float n7 = (float) (0.1631f / Math.pow(n6, 3.0f));
        float n8;
        if (mc.player.isOnGround()) {
            n8 = speedAttributes * n7;
            if (move.getY() > 0) {
                n8 += boost.getValue() == Boost.Elytra && InventoryUtility.getElytra() != -1 && disabled ? 0.65f : 0.2f;
            }
            disabled = false;
        } else {
            n8 = 0.0255f;
        }
        boolean noslow = false;
        double max2 = oldSpeed + n8;
        double max = 0.0;
        if (mc.player.isUsingItem() && move.getY() <= 0) {
            double n10 = oldSpeed + n8 * 0.25;
            double motionY2 = move.getY();
            if (motionY2 != 0.0 && Math.abs(motionY2) < 0.08) {
                n10 += 0.055;
            }
            if (max2 > (max = Math.max(0.043, n10))) {
                noslow = true;
                ++noSlowTicks;
            } else noSlowTicks = Math.max(noSlowTicks - 1, 0);
        } else noSlowTicks = 0;
        if (noSlowTicks > 3) max2 = max - 0.019;
        else max2 = Math.max(noslow ? 0 : 0.25, max2) - (mc.player.age % 2 == 0 ? 0.001 : 0.002);

        contextFriction = n6;
        if (!mc.player.isOnGround()) {
            needSprintState = !mc.player.lastSprinting;
            needSwap = true;
        } else needSprintState = false;
        return max2;
    }

    public Box getBoundingBox() {
        return new Box(mc.player.getX() - 0.1, mc.player.getY(), mc.player.getZ() - 0.1, mc.player.getX() + 0.1, mc.player.getY() + 1, mc.player.getZ() + 0.1);
    }

    public float getAIMoveSpeed() {
        boolean prevSprinting = mc.player.isSprinting();
        mc.player.setSprinting(false);
        float speed = mc.player.getMovementSpeed() * 1.3f;
        mc.player.setSprinting(prevSprinting);
        return speed;
    }

    public static void disabler(int elytra) {
        if (elytra == -1) return;
        if (System.currentTimeMillis() - disableTime > 190L) {
            if (elytra != -2) {
                mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
            }

            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

            if (elytra != -2) {
                mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
            }
            disableTime = System.currentTimeMillis();
        }
        disabled = true;
    }

    private double wrapDS(double x, double z) {
        double diffX = x - mc.player.getX();
        double diffZ = z - mc.player.getZ();
        return Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
    }

    @EventHandler
    public void onMove(EventMove event) {
        int elytraSlot = InventoryUtility.getElytra();

        if (boost.getValue() == Boost.Elytra && elytraSlot != -1) {
            if (isMoving() && !mc.player.isOnGround() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, event.getY(), 0.0f)).iterator().hasNext() && disabled) {
                oldSpeed = setSpeed.getValue();
            }
        }

        if (canStrafe()) {
            if (Aura.target != null && ModuleManager.aura.isEnabled()) {
                double speed = calculateSpeed(event);

                double wrap = Math.atan2(mc.player.getZ() - Aura.target.getZ(), mc.player.getX() - Aura.target.getX());
                wrap += switchDir ? speed / Math.sqrt(mc.player.squaredDistanceTo(Aura.target)) : -(speed / Math.sqrt(mc.player.squaredDistanceTo(Aura.target)));

                double x = Aura.target.getX() + distance.getValue() * Math.cos(wrap);
                double z = Aura.target.getZ() + distance.getValue() * Math.sin(wrap);

                if (needToSwitch(x, z)) {
                    switchDir = !switchDir;
                    wrap += 2 * (switchDir ? speed / Math.sqrt(mc.player.squaredDistanceTo(Aura.target)) : -(speed / Math.sqrt(mc.player.squaredDistanceTo(Aura.target))));
                    x = Aura.target.getX() + distance.getValue() * Math.cos(wrap);
                    z = Aura.target.getZ() + distance.getValue() * Math.sin(wrap);
                }

                event.setX(speed * -Math.sin(Math.toRadians(wrapDS(x, z))));
                event.setZ(speed * Math.cos(Math.toRadians(wrapDS(x, z))));
                event.cancel();

            }
        } else {
            oldSpeed = 0;
        }
    }

    @EventHandler
    public void updateValues(EventSync e) {
        oldSpeed = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ) * contextFriction;

        if (mc.player.isOnGround() && jump.getValue() && Aura.target != null) {
            mc.player.jump();
        }

        if (mc.player.isSubmergedInWater()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if ((boost.getValue() == Boost.Elytra && InventoryUtility.getElytra() != -1 && !mc.player.isOnGround() && mc.player.fallDistance > 0 && !disabled)) {
            disabler(InventoryUtility.getElytra());
        }
    }


    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            oldSpeed = 0;
        }
        EntityVelocityUpdateS2CPacket velocity;
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket && (velocity = e.getPacket()).getId() == mc.player.getId() && boost.getValue() == Boost.Damage) {
            if (mc.player.isOnGround()) return;

            double vX = velocity.getVelocityX();
            double vZ = velocity.getVelocityZ();

            if (vX < 0) vX *= -1;
            if (vZ < 0) vZ *= -1;

            oldSpeed = (vX + vZ) / (velReduction.getValue() * 1000f);
            oldSpeed = Math.min(oldSpeed, maxVelocitySpeed.getValue());

            ((ISPacketEntityVelocity) velocity).setMotionX(0);
            ((ISPacketEntityVelocity) velocity).setMotionY(0);
            ((ISPacketEntityVelocity) velocity).setMotionZ(0);
        }
    }


    @EventHandler
    public void actionEvent(EventSprint eventAction) {
        if (canStrafe()) {
            if (Core.serverSprint != needSprintState) {
                eventAction.setSprintState(!Core.serverSprint);
            }
        }
        if (needSwap) {
            eventAction.setSprintState(!mc.player.lastSprinting);
            needSwap = false;
        }
    }

    private enum Boost {
        None, Elytra, Damage
    }
}

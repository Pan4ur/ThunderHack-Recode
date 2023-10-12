package dev.thunderhack.modules.movement;

import dev.thunderhack.ThunderHack;
import dev.thunderhack.core.Core;
import dev.thunderhack.core.ModuleManager;
import dev.thunderhack.event.events.*;
import dev.thunderhack.mixins.accesors.ISPacketEntityVelocity;
import dev.thunderhack.modules.Module;
import dev.thunderhack.setting.Setting;
import dev.thunderhack.utils.Timer;
import dev.thunderhack.utils.player.InventoryUtility;
import dev.thunderhack.utils.player.MovementUtility;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class Strafe extends Module {
    private final Setting<Boost> boost = new Setting<>("Boost", Boost.None);
    public Setting<Float> setSpeed = new Setting<>("speed", 1.3F, 0.0F, 2f, v -> boost.getValue() == Boost.Elytra);
    private final Setting<Float> velReduction = new Setting<>("Reduction", 6.0f, 0.1f, 10f, v -> boost.getValue() == Boost.Damage);
    private final Setting<Float> maxVelocitySpeed = new Setting<>("MaxVelocity", 0.8f, 0.1f, 2f, v -> boost.getValue() == Boost.Damage);

    public static double oldSpeed, contextFriction, fovval;
    public static boolean needSwap, needSprintState, disabled;
    public static int noSlowTicks, waterTicks, jumpTicks;
    static long disableTime;

    public Strafe() {
        super("Strafe", Category.MOVEMENT);
    }

    public double calculateSpeed(EventMove move) {
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
            } else {
                noSlowTicks = Math.max(noSlowTicks - 1, 0);
            }
        } else {
            noSlowTicks = 0;
        }
        if (noSlowTicks > 3) {
            max2 = max - 0.019;
        } else {
            max2 = Math.max(noslow ? 0 : 0.25, max2) - (mc.player.age % 2 == 0 ? 0.001 : 0.002);
        }
        contextFriction = n6;
        if (!mc.player.isOnGround()) {
            needSprintState = !mc.player.lastSprinting;
            needSwap = true;
        } else {
            needSprintState = false;
        }
        return max2;
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


    @Override
    public void onEnable() {
        oldSpeed = 0.0;
        fovval = mc.options.getFovEffectScale().getValue();
        mc.options.getFovEffectScale().setValue(0d);
    }

    @Override
    public void onDisable() {
        mc.options.getFovEffectScale().setValue(fovval);
    }

    public boolean canStrafe() {
        if (mc.player.isSneaking()) {
            return false;
        }
        if (mc.player.isInLava()) {
            return false;
        }
        if (ModuleManager.scaffold.isEnabled()) {
            return false;
        }
        if (ModuleManager.speed.isEnabled()) {
            return false;
        }
        if (mc.player.isSubmergedInWater() || waterTicks > 0) {
            return false;
        }
        return !mc.player.getAbilities().flying;
    }

    public Box getBoundingBox() {
        return new Box(mc.player.getX() - 0.1, mc.player.getY(), mc.player.getZ() - 0.1, mc.player.getX() + 0.1, mc.player.getY() + 1, mc.player.getZ() + 0.1);
    }

    @EventHandler
    public void onMove(EventMove event) {
        int elytraSlot = InventoryUtility.getElytra();

        if (boost.getValue() == Boost.Elytra && elytraSlot != -1) {
            if (MovementUtility.isMoving() && !mc.player.isOnGround() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, event.getY(), 0.0f)).iterator().hasNext() && disabled) {
                oldSpeed = setSpeed.getValue();
            }
        }
        if (canStrafe()) {
            if (MovementUtility.isMoving()) {
                double[] motions = MovementUtility.forward(calculateSpeed(event));

                event.setX(motions[0]);
                event.setZ(motions[1]);
            } else {
                oldSpeed = 0;
                event.setX(0);
                event.setZ(0);
            }
            event.cancel();
        } else {
            oldSpeed = 0;
        }

    }

    @EventHandler
    public void updateValues(EventSync e) {
        oldSpeed = ThunderHack.playerManager.currentPlayerSpeed * contextFriction;
        if (mc.player.isSubmergedInWater()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
        if (jumpTicks > 0) {
            jumpTicks--;
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

            int vX = velocity.getVelocityX();
            int vZ = velocity.getVelocityZ();

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
            if (Core.serversprint != needSprintState) {
                eventAction.setSprintState(!Core.serversprint);
            }
        }
        if (needSwap) {
            eventAction.setSprintState(!mc.player.lastSprinting);
            needSwap = false;
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if ((boost.getValue() == Boost.Elytra && InventoryUtility.getElytra() != -1 && !mc.player.isOnGround() && mc.player.fallDistance > 0 && !disabled)) {
            disabler(InventoryUtility.getElytra());
        }
        elytraFix();
    }

    private final dev.thunderhack.utils.Timer delay = new Timer();


    public void elytraFix() {
        ItemStack stack = mc.player.currentScreenHandler.getCursorStack();
        if (stack.getItem() instanceof ArmorItem && delay.passedMs(300)) {
            if (((ArmorItem) stack.getItem()).getType() == ArmorItem.Type.CHESTPLATE && mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
                int nullSlot = findEmptySlot();
                boolean needDrop = nullSlot == 999;
                if (needDrop) nullSlot = 9;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, nullSlot, 1, SlotActionType.PICKUP, mc.player);
                if (needDrop)
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, -999, 0, SlotActionType.PICKUP, mc.player);
                delay.reset();
            }
        }
    }

    public static int findEmptySlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return 999;
    }

    private enum Boost {
        None, Elytra, Damage
    }
}

package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import thunder.hack.Thunderhack;
import thunder.hack.core.Core;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSprint;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.modules.Module;
import thunder.hack.modules.movement.Speed;
import thunder.hack.modules.player.Scaffold;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtil;
import thunder.hack.utility.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import thunder.hack.utility.player.MovementUtil;

public class TargetStrafe extends Module {

    public TargetStrafe() {
        super("TargetStrafe", "TargetStrafe", Category.COMBAT);
    }



    public Setting<Boolean> jump = new Setting<>("Jump", true);
    public Setting<Float> distance = new Setting<>("Distance", 1.3F, 0.2F, 7f);

    private final Setting<Boost> boost = new Setting<>("Boost", Boost.None);
    public Setting<Float> setSpeed = new Setting<>("speed", 1.3F, 0.0F, 2f,v-> boost.getValue() == Boost.Elytra);
    private final Setting<Float> velReduction = new Setting<>("Reduction", 6.0f, 0.1f, 10f, v -> boost.getValue() == Boost.Damage);
    private final Setting<Float> maxVelocitySpeed = new Setting<>("MaxVelocity", 0.8f, 0.1f, 2f, v -> boost.getValue() == Boost.Damage);
    public Setting<Boolean> extra = new Setting<>("Extra", false, v -> boost.getValue() == Boost.Elytra);
    public Setting<Boolean> resetExtra = new Setting<>("ResetExtra", false, v -> extra.getValue());
    private final Setting<Float> fdl1 = new Setting<>("Min Falldist", 1f, 0.0f, 3f, v -> extra.getValue());
    private final Setting<Float> fdl2 = new Setting<>("Max Falldist", 2f, 0.0f, 5f, v -> extra.getValue());
    private final Setting<Float> jme = new Setting<>("JumpMotionElytra", 0.65f, 0.1f, 1f, v -> extra.getValue());
    private final Setting<Float> jmd = new Setting<>("JumpMotion", 0.2f, 0.1f, 1f, v -> extra.getValue());
    private final Setting<Float> dpredict = new Setting<>("DisablerPredict", 0.5f, 0.01f, 1f, v -> extra.getValue());
    private final Setting<Float> ogf = new Setting<>("OffGroundFriction", 2.55f, 0.01f, 3f, v -> extra.getValue());
    private final Setting<Float> sprintm = new Setting<>("SprintMultiplier", 1.3f, 0.01f, 3f, v -> extra.getValue());
    private final Setting<Integer> FrictionFactor = new Setting<>("FrictionFactor", 1646, 800, 3000, v -> extra.getValue());


    private float waterTicks = 0;
    public static double oldSpeed, contextFriction;
    public static boolean needSwap, needSprintState;
    public static int noSlowTicks;
    public static float jumpTicks = 0;
    boolean skip = false;
    private boolean switchDir = true;

    private final Timer elytraDelay = new Timer();
    private final Timer startDelay = new Timer();



    @Override
    public void onEnable() {
        oldSpeed = 0;
        fovval = mc.options.getFovEffectScale().getValue();
        mc.options.getFovEffectScale().setValue(0d);
        startDelay.reset();
        skip = true;
    }

    public boolean canStrafe() {
        if (mc.player.isSneaking()) {
            return false;
        }
        if (mc.player.isInLava()) {
            return false;
        }
        if(Thunderhack.moduleManager.get(Scaffold.class).isEnabled()){
            return false;
        }
        if(Thunderhack.moduleManager.get(Speed.class).isEnabled()){
            return false;
        }
        if (mc.player.isSubmergedInWater() || waterTicks > 0) {
            return false;
        }
        return !mc.player.getAbilities().flying;
    }

    public boolean needToSwitch(double x, double z) {

        if (mc.player.horizontalCollision || ((mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed()) && jumpTicks <= 0)) {
            jumpTicks = 10;
            return true;
        }
        for (int i = (int) (mc.player.getY() + 4); i >= 0; --i) {
            BlockPos playerPos = new BlockPos( (int)Math.floor(x), (int)Math.floor(i), (int)Math.floor(z));
            blockFIRE: {
                blockLAVA: {
                    if (mc.world.getBlockState(playerPos).getBlock().equals(Blocks.LAVA))
                        break blockLAVA;
                    if (!mc.world.getBlockState(playerPos).getBlock().equals(Blocks.FIRE))
                        break blockFIRE;
                }
                return true;
            }
            if (mc.world.isAir(playerPos))
                continue;
            return false;
        }
        return false;
    }


    double fovval;


    @Override
    public void onDisable() {
        mc.options.getFovEffectScale().setValue(fovval);
    }

    public double calculateSpeed(EventMove move) {
        jumpTicks--;
        float speedAttributes = getAIMoveSpeed();
        final float frictionFactor = getFrictionFactor();
        float n6 = mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && mc.player.isUsingItem() ? 0.88f : (float) (oldSpeed > 0.32 && mc.player.isUsingItem() ? 0.88 : 0.91F);
        if (mc.player.isOnGround()) {
            n6 = frictionFactor;
        }
        float n7 = (float) (((float) FrictionFactor.getValue() / 10000f ) / Math.pow(n6, 3.0));
        float n8;
        if (mc.player.isOnGround()) {
            n8 = speedAttributes * n7;
            if (move.get_y() > 0) {
                n8 += boost.getValue() == Boost.Elytra && InventoryUtil.getElytra() != -1 && oldSpeed > 0.4 ? jme.getValue() : jmd.getValue(); // хуярим лонг джампами чтоб матрикс не втыкал
            }
        } else {
            n8 = ogf.getValue() / 100f;
        }
        boolean noslow = false;
        double max2 = oldSpeed + n8;
        double max = 0.0;
        if (mc.player.isUsingItem() && move.get_y() <= 0) {
            double n10 = oldSpeed + n8 * 0.25;
            double motionY2 = move.get_y();
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
        float speed = mc.player.getMovementSpeed() * sprintm.getValue();
        mc.player.setSprinting(prevSprinting);
        return speed;
    }

    public static void disabler(int elytra) {
        if (elytra != -2)
        {
            mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, 6, 1,  SlotActionType.PICKUP, mc.player);
        }
        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        if (elytra != -2)
        {
            mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
        }
    }

    private double wrapDS(double x, double z) {
        double diffX = x - mc.player.getX();
        double diffZ = z - mc.player.getZ();
        return Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
    }

    @Subscribe
    public void onMove(EventMove event) {
            int elytraSlot = InventoryUtil.getElytra();

            if (boost.getValue() == Boost.Elytra && elytraSlot != -1) {
                if (MovementUtil.isMoving() && !mc.player.isOnGround() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.29,0,-0.29).offset(0.0,  event.get_y(), 0.0)).iterator().hasNext() && mc.player.fallDistance < fdl2.getValue() && mc.player.fallDistance > fdl1.getValue()) {
                    oldSpeed = setSpeed.getValue();
                }
            }

            if (canStrafe()) {
                if (Aura.target != null && Thunderhack.moduleManager.get(Aura.class).isEnabled()) {
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

                    event.set_x(speed * -Math.sin(Math.toRadians(wrapDS(x, z))));
                    event.set_z(speed * Math.cos(Math.toRadians(wrapDS(x, z))));
                    event.setCancelled(true);

                }
            } else {
                oldSpeed = 0;
            }
    }

    @Subscribe
    public void updateValues(EventSync e) {
        oldSpeed = Math.hypot(mc.player.getX() - mc.player.prevX,mc.player.getZ() - mc.player.prevZ) * contextFriction;
        if (boost.getValue() == Boost.Elytra && MovementUtil.isMoving() && !mc.player.isOnGround() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.29,0,-0.29).offset(0.0, -dpredict.getValue(), 0.0f)).iterator().hasNext()&& mc.player.fallDistance < fdl2.getDefaultValue() && mc.player.fallDistance > fdl1.getValue() && elytraDelay.passedMs(400)) {
            disabler(InventoryUtil.getElytra());
            elytraDelay.reset();
        }

        if(mc.player.isOnGround() && jump.getValue() && Aura.target != null){
            mc.player.jump();
        }

        if (mc.player.isSubmergedInWater()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }

        if(resetExtra.getValue()){
            fdl1.setValue(1f);
            fdl2.setValue(2f);
            jme.setValue(0.65f);
            jmd.setValue(0.2f);
            dpredict.setValue(0.5f);
            ogf.setValue(2.55f);
            sprintm.setValue(1.3f);
            FrictionFactor.setValue(1646);
            resetExtra.setValue(false);
        }
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            oldSpeed = 0;
        }
        EntityVelocityUpdateS2CPacket velocity;
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket  && (velocity = e.getPacket()).getId() == mc.player.getId() && boost.getValue() == Boost.Damage) {
            if(mc.player.isOnGround()) return;

            int vX =  velocity.getVelocityX();
            int vZ =  velocity.getVelocityZ();

            if (vX < 0) vX *= -1;
            if (vZ < 0) vZ *= -1;

            oldSpeed = (vX + vZ) / (velReduction.getValue() * 1000f);
            oldSpeed = Math.min(oldSpeed, maxVelocitySpeed.getValue());

            ((ISPacketEntityVelocity) velocity).setMotionX(0);
            ((ISPacketEntityVelocity) velocity).setMotionY(0);
            ((ISPacketEntityVelocity) velocity).setMotionZ(0);
        }
    }


    @Subscribe
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

    private enum Boost {
        None, Elytra, Damage
    }

    private static float getFrictionFactor() {
        BlockPos.Mutable bp = new BlockPos.Mutable();
        bp.set(mc.player.prevX, mc.player.getBoundingBox().minY - 0.8D, mc.player.prevZ);
        return mc.world.getBlockState(bp).getBlock().getSlipperiness() * 0.91F;
    }
}

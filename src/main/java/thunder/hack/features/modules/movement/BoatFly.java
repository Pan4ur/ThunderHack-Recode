package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.player.MovementUtility;

import java.util.ArrayList;

public class BoatFly extends Module {
    public BoatFly() {
        super("BoatFly", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Packet);

    private final Setting<Boolean> phase = new Setting<>("Phase", false);
    private final Setting<Boolean> gravity = new Setting<>("Gravity", false);
    private final Setting<Boolean> automount = new Setting<>("AutoMount", true);
    public final Setting<Boolean> allowShift = new Setting<>("AllowShift", true);
    private final Setting<Float> speed = new Setting<>("Speed", 2f, 0.0f, 25f);
    private final Setting<Float> yspeed = new Setting<>("YSpeed", 1f, 0.0f, 10f);

    // For pro players
    public final Setting<SettingGroup> advanced = new Setting<>("Advanced", new SettingGroup(false, 0));
    private final Setting<Float> glidespeed = new Setting<>("GlideSpeed", 0f, 0f, 10f).addToGroup(advanced);
    private final Setting<Boolean> slotClick = new Setting<>("ClickSlot", false).addToGroup(advanced);
    private final Setting<Boolean> limit = new Setting<>("Limit", true).addToGroup(advanced);
    private final Setting<Boolean> ongroundpacket = new Setting<>("OnGroundPacket", false).addToGroup(advanced);
    private final Setting<Boolean> spoofpackets = new Setting<>("SpoofPackets", false).addToGroup(advanced);
    private final Setting<Float> jitter = new Setting<>("Jitter", 0.1f, 0.0f, 10f, v -> spoofpackets.getValue()).addToGroup(advanced);
    private final Setting<Boolean> cancelrotations = new Setting<>("CancelRotations", true).addToGroup(advanced);
    private final Setting<Boolean> cancel = new Setting<>("Cancel", true).addToGroup(advanced);
    private final Setting<Boolean> pause = new Setting<>("Pause", false).addToGroup(advanced);
    private final Setting<Integer> enableticks = new Setting<>("EnableTicks", 10, 1, 100, v -> pause.getValue()).addToGroup(advanced);
    private final Setting<Integer> waitTicks = new Setting<>("WaitTicks", 10, 1, 100, v -> pause.getValue()).addToGroup(advanced);
    private final Setting<Boolean> stopunloaded = new Setting<>("StopUnloaded", true).addToGroup(advanced);
    private final Setting<Float> timer = new Setting<>("Timer", 1f, 0.1f, 5f).addToGroup(advanced);
    public final Setting<Boolean> hideBoat = new Setting<>("HideBoat", true).addToGroup(advanced);

    private final ArrayList<VehicleMoveC2SPacket> vehiclePackets = new ArrayList<>();
    private int ticksEnabled = 0;
    private int enableDelay = 0;
    private boolean waitedCooldown = false;
    private boolean returnGravity = false;
    private boolean jitterSwitch = false;

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            disable();
            return;
        }

        if (automount.getValue()) mountToBoat();
    }

    @Override
    public void onDisable() {
        ThunderHack.TICK_TIMER = 1.0f;
        vehiclePackets.clear();
        waitedCooldown = false;

        if (mc.player == null) return;

        if ((phase.getValue()) && mode.getValue() == Mode.Motion) {
            if (mc.player.getControllingVehicle() != null) mc.player.getControllingVehicle().noClip = false;
            mc.player.noClip = false;
        }
        if (mc.player.getControllingVehicle() != null) mc.player.getControllingVehicle().setNoGravity(false);
        mc.player.setNoGravity(false);
    }


    private float randomizeYOffset() {
        jitterSwitch = !jitterSwitch;
        return jitterSwitch ? jitter.getValue() : -jitter.getValue();
    }

    private void sendMovePacket(VehicleMoveC2SPacket pac) {
        vehiclePackets.add(pac);
        sendPacket(pac);
    }

    private void teleportToGround(Entity boat) {
        BlockPos blockPos = BlockPos.ofFloored(boat.getPos());
        for (int i = 0; i < 255; ++i) {
            if (!mc.world.getBlockState(blockPos).isReplaceable() || mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                boat.setPosition(boat.getX(), blockPos.getY() + 1, boat.getZ());
                sendMovePacket(new VehicleMoveC2SPacket(boat));
                boat.setPosition(boat.getX(), boat.getY(), boat.getZ());
                break;
            }
            blockPos = blockPos.down();
        }
    }

    private void mountToBoat() {
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof BoatEntity) || mc.player.squaredDistanceTo(entity) > 25.0f) continue;
            sendPacket(PlayerInteractEntityC2SPacket.interact(entity, false, Hand.MAIN_HAND));
            break;
        }
    }

    @EventHandler
    public void onPlayerTravel(@NotNull EventPlayerTravel ev) {
        if (!ev.isPre()) return;
        if (fullNullCheck()) return;


        if (mc.player.getControllingVehicle() == null) {
            if (automount.getValue())
                mountToBoat();
            return;
        }

        if (phase.getValue() && mode.getValue() == Mode.Motion) {
            mc.player.getControllingVehicle().noClip = true;
            mc.player.getControllingVehicle().setNoGravity(true);
            mc.player.noClip = true;
        }

        if (!returnGravity) {
            mc.player.getControllingVehicle().setNoGravity(!gravity.getValue());
            mc.player.setNoGravity(!gravity.getValue());
        }

        if (pause.getValue()) {
            if (ticksEnabled > enableticks.getValue() && !waitedCooldown) {
                ticksEnabled = 0;
                waitedCooldown = true;
                enableDelay = waitTicks.getValue();
            }

            if (enableDelay > 0 && waitedCooldown) {
                --enableDelay;
                return;
            }

            if (enableDelay <= 0) waitedCooldown = false;
        }

        Entity entity = mc.player.getControllingVehicle();


        if ((!mc.world.isChunkLoaded((int) entity.getPos().getX() >> 4, (int) entity.getPos().getZ() >> 4) || entity.getPos().getY() < -60) && stopunloaded.getValue()) {
            returnGravity = true;
            return;
        }

        if (timer.getValue() != 1.0f) ThunderHack.TICK_TIMER = (timer.getValue());

        entity.setYaw(mc.player.getYaw());

        double[] boatMotion = MovementUtility.forward(speed.getValue());
        double predictedX = entity.getX() + boatMotion[0];
        double predictedZ = entity.getZ() + boatMotion[1];
        double predictedY = entity.getY();

        if ((!mc.world.isChunkLoaded((int) predictedX >> 4, (int) predictedZ >> 4) || entity.getPos().getY() < -60) && stopunloaded.getValue()) {
            returnGravity = true;
            return;
        }

        returnGravity = false;

        entity.setVelocity(entity.getVelocity().getX(), -glidespeed.getValue() / 100.0f, entity.getVelocity().getZ());

        if (mode.getValue() == Mode.Motion)
            entity.setVelocity(boatMotion[0], entity.getVelocity().getY(), boatMotion[1]);

        if (mc.options.jumpKey.isPressed()) {
            if (mode.getValue() == Mode.Motion)
                entity.setVelocity(entity.getVelocity().getX(), entity.getVelocity().getY() + yspeed.getValue(), entity.getVelocity().getZ());
            else predictedY += yspeed.getValue();
        } else if (mc.options.sneakKey.isPressed()) {
            if (mode.getValue() == Mode.Motion)
                entity.setVelocity(entity.getVelocity().getX(), entity.getVelocity().getY() - yspeed.getValue(), entity.getVelocity().getZ());
            else predictedY -= yspeed.getValue();
        }

        if (!MovementUtility.isMoving()) entity.setVelocity(0, entity.getVelocity().getY(), 0);

        if (ongroundpacket.getValue()) teleportToGround(entity);

        if (mode.getValue() == Mode.Packet) {
            entity.setPosition(predictedX, predictedY, predictedZ);
            sendMovePacket(new VehicleMoveC2SPacket(entity));
        }

        if (slotClick.getValue())
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.CLONE, mc.player);

        if (spoofpackets.getValue()) {
            Vec3d vec3d = entity.getPos().add(0.0, randomizeYOffset(), 0.0);
            BoatEntity entityBoat = new BoatEntity(mc.world, vec3d.x, vec3d.y, vec3d.z);
            entityBoat.setYaw(entity.getYaw());
            entityBoat.setPitch(entity.getPitch());
            sendMovePacket(new VehicleMoveC2SPacket(entityBoat));
        }

        ev.cancel();
        ++ticksEnabled;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) return;

        if (event.getPacket() instanceof DisconnectS2CPacket) disable();

        if (!mc.player.isRiding() || returnGravity || waitedCooldown) return;

        if (cancel.getValue()) {
            if (event.getPacket() instanceof VehicleMoveS2CPacket) event.cancel();
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket) event.cancel();
            if (event.getPacket() instanceof EntityS2CPacket) event.cancel();
            if (event.getPacket() instanceof EntityAttachS2CPacket) event.cancel();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (fullNullCheck()) return;

        if ((event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround && (cancelrotations.getValue()) || event.getPacket() instanceof PlayerInputC2SPacket) && mc.player.isRiding())
            event.cancel();

        if (returnGravity && event.getPacket() instanceof VehicleMoveC2SPacket) event.cancel();

        if (event.getPacket() instanceof PlayerInputC2SPacket && allowShift.getValue()) {
            event.cancel();
        }

        if (mc.player.getControllingVehicle() == null || returnGravity || waitedCooldown)
            return;

        Vec3d boatPos = mc.player.getControllingVehicle().getPos();
        if ((!mc.world.isChunkLoaded((int) boatPos.getX() >> 4, (int) boatPos.getZ() >> 4) || boatPos.getY() < -60) && stopunloaded.getValue())
            return;

        if (event.getPacket() instanceof VehicleMoveC2SPacket pac && limit.getValue() && mode.getValue() == Mode.Packet)
            if (vehiclePackets.contains(pac)) vehiclePackets.remove(pac);
            else event.cancel();
    }

    public enum Mode {
        Packet, Motion
    }
}
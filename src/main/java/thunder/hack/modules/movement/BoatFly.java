package thunder.hack.modules.movement;

import io.netty.util.internal.ConcurrentSet;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class BoatFly extends Module {
    private final ConcurrentSet vehiclePackets = new ConcurrentSet();
    public Setting<Boolean> strict = new Setting<>("Strict", false);
    public Setting<Boolean> limit = new Setting<>("Limit", true);
    public Setting<Boolean> phase = new Setting<>("Phase", true);
    public Setting<Boolean> gravity = new Setting<>("Gravity", true);
    public Setting<Boolean> ongroundpacket = new Setting<>("OnGroundPacket", false);
    public Setting<Boolean> spoofpackets = new Setting<>("SpoofPackets", false);
    public Setting<Boolean> cancelrotations = new Setting<>("CancelRotations", true);
    public Setting<Boolean> cancel = new Setting<>("Cancel", true);
    public Setting<Boolean> remount = new Setting<>("Remount", true);
    public Setting<Boolean> stop = new Setting<>("Stop", false);
    public Setting<Boolean> ylimit = new Setting<>("yLimit", false);
    public Setting<Boolean> debug = new Setting<>("Debug", true);
    public Setting<Boolean> automount = new Setting<>("AutoMount", true);
    public Setting<Boolean> stopunloaded = new Setting<>("StopUnloaded", true);
    private final Setting<Mode> mode = new Setting("Mode", Mode.Packet);
    private final Setting<Float> speed = new Setting<>("Speed", 2f, 0.0f, 45f);
    private final Setting<Float> yspeed = new Setting<>("YSpeed", 1f, 0.0f, 10f);
    private final Setting<Float> glidespeed = new Setting<>("GlideSpeed", 1f, 0.0f, 10f);
    private final Setting<Float> timer = new Setting<>("Timer", 1f, 0.0f, 5f);
    private final Setting<Float> height = new Setting<>("Height", 127f, 0.0f, 256f);
    private final Setting<Float> offset = new Setting<>("Offset", 0.1f, 0.0f, 10f);
    private final Setting<Integer> enableticks = new Setting("EnableTicks", 10, 1, 100);
    private final Setting<Integer> waitticks = new Setting("WaitTicks", 10, 1, 100);
    private int Field2264 = 0;
    private int Field2265 = 0;
    private boolean Field2266 = false;
    private boolean Field2267 = false;
    private boolean Field2268 = false;

    public BoatFly() {
        super("BoatFly", "полёт на лодке и мобах", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            disable();
            return;
        }
        if ((automount.getValue())) {
            mountToBoat();
        }
    }

    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = (1.0f);
        vehiclePackets.clear();
        Field2266 = false;
        if (mc.player == null) {
            return;
        }
        if ((phase.getValue()) && mode.getValue() == Mode.Motion) {
            if (mc.player.getControllingVehicle() != null) {
                mc.player.getControllingVehicle().noClip = false;
            }
            mc.player.noClip = false;
        }
        if (mc.player.getControllingVehicle() != null) {
            mc.player.getControllingVehicle().setNoGravity(false);
        }
        mc.player.setNoGravity(false);
    }


    private float Method2874() {
        Field2268 = !Field2268;
        return Field2268 ? (offset.getValue()) : -(offset.getValue());
    }

    private void Method2875(VehicleMoveC2SPacket cPacketVehicleMove) {
        vehiclePackets.add(cPacketVehicleMove);
        mc.player.networkHandler.sendPacket(cPacketVehicleMove);
    }

    private void Method2876(Entity entity) {
        BlockPos blockPos = BlockPos.ofFloored(entity.getPos());
        for (int i = 0; i < 255; ++i) {
            if (mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                entity.setPosition(entity.getX(), blockPos.getY() + 1, entity.getZ());
                if (debug.getValue())
                    sendMessage("GroundY" + entity.getPos().getY());

                Method2875(new VehicleMoveC2SPacket(entity));
                entity.setPosition(entity.getX(), entity.getY(), entity.getZ());
                break;
            }
            blockPos = blockPos.add(0, -1, 0);
        }
    }

    private void mountToBoat() {
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof BoatEntity) || !(mc.player.squaredDistanceTo(entity) < 25.0f)) continue;
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, false, Hand.MAIN_HAND));
            break;
        }
    }

    @EventHandler
    public void onPlayerTravel(@NotNull EventPlayerTravel ev) {
        if (!ev.isPre()) return;
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mc.player.getControllingVehicle() == null) {
            if (automount.getValue()) {
                mountToBoat();
            }
            return;
        }
        if (phase.getValue() && mode.getValue() == Mode.Motion) {
            mc.player.getControllingVehicle().noClip = true;
            mc.player.getControllingVehicle().setNoGravity(true);
            mc.player.noClip = true;
        }
        if (!Field2267) {
            mc.player.getControllingVehicle().setNoGravity(!(gravity.getValue()));
            mc.player.setNoGravity(!(gravity.getValue()));
        }
        if (stop.getValue()) {
            if (Field2264 > enableticks.getValue() && !Field2266) {
                Field2264 = 0;
                Field2266 = true;
                Field2265 = waitticks.getValue();
            }
            if (Field2265 > 0 && Field2266) {
                --Field2265;
                return;
            }
            if (Field2265 <= 0) {
                Field2266 = false;
            }
        }
        Entity entity = mc.player.getControllingVehicle();
        if (debug.getValue()) {
            sendMessage("Y" + entity.getY());
            sendMessage("Fall" + entity.fallDistance);
        }
        if ((!mc.world.isChunkLoaded((int) entity.getPos().getX() >> 4, (int) entity.getPos().getZ() >> 4) || entity.getPos().getY() < 0) && stopunloaded.getValue()) {
            if (debug.getValue())
                sendMessage("Detected unloaded chunk!");
            Field2267 = true;
            return;
        }
        if (timer.getValue() != 1.0f) {
            Thunderhack.TICK_TIMER = (timer.getValue());
        }
        entity.setYaw(mc.player.getYaw());

        double[] dArray = MovementUtility.forward(speed.getValue());
        double d = entity.getX() + dArray[0];
        double d2 = entity.getZ() + dArray[1];
        double d3 = entity.getY();

        if ((!mc.world.isChunkLoaded((int) d >> 4, (int) d2 >> 4) || entity.getPos().getY() < 0) && (stopunloaded.getValue())) {
            if (debug.getValue())
                sendMessage("Detected unloaded chunk!");
            Field2267 = true;
            return;
        }

        Field2267 = false;
        entity.setVelocity(entity.getVelocity().getX(), -((glidespeed.getValue()) / 100.0f), entity.getVelocity().getZ());
        if (mode.getValue() == Mode.Motion) {
            entity.setVelocity(dArray[0], entity.getVelocity().getY(), dArray[1]);
        }
        if (mc.options.jumpKey.isPressed()) {
            if (!(ylimit.getValue()) || entity.getY() <= (height.getValue())) {
                if (mode.getValue() == Mode.Motion) {
                    entity.setVelocity(entity.getVelocity().getX(), entity.getVelocity().getY() + (yspeed.getValue()), entity.getVelocity().getZ());
                } else {
                    d3 += yspeed.getValue();
                }
            }
        } else if (mc.options.sneakKey.isPressed()) {
            if (mode.getValue() == Mode.Motion) {
                entity.setVelocity(entity.getVelocity().getX(), entity.getVelocity().getY() + (-(yspeed.getValue())), entity.getVelocity().getZ());
            } else {
                d3 -= yspeed.getValue();
            }
        }

        if (mc.player.input.movementSideways == 0.0f && mc.player.input.movementForward == 0.0f)
            entity.setVelocity(0, entity.getVelocity().getY(), 0);

        if (ongroundpacket.getValue())
            Method2876(entity);

        if (mode.getValue() != Mode.Motion)
            entity.setPosition(d, d3, d2);

        if (mode.getValue() == Mode.Packet)
            Method2875(new VehicleMoveC2SPacket(entity));

        if (strict.getValue())
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.CLONE, mc.player);

        if (spoofpackets.getValue()) {
            Vec3d vec3d = entity.getPos().add(0.0, Method2874(), 0.0);
            BoatEntity entityBoat = new BoatEntity(mc.world, vec3d.x, vec3d.y, vec3d.z);
            entityBoat.setYaw(entity.getYaw());
            entityBoat.setPitch(entity.getPitch());
            Method2875(new VehicleMoveC2SPacket(entityBoat));
        }
        if ((remount.getValue())) {
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, false, Hand.MAIN_HAND));
        }
        ev.cancel();
        ++Field2264;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) return;

        if (event.getPacket() instanceof DisconnectS2CPacket) disable();

        if (!mc.player.isRiding() || Field2267 || Field2266) return;

        if (event.getPacket() instanceof VehicleMoveS2CPacket && mc.player.isRiding() && cancel.getValue())
            event.cancel();

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket && mc.player.isRiding() && cancel.getValue())
            event.cancel();

        if (event.getPacket() instanceof EntityS2CPacket && cancel.getValue()) event.cancel();

        if (event.getPacket() instanceof EntityAttachS2CPacket && cancel.getValue()) event.cancel();
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if ((event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround && (cancelrotations.getValue()) || event.getPacket() instanceof PlayerInputC2SPacket) && mc.player.isRiding()) {
            event.cancel();
        }
        if (Field2267 && event.getPacket() instanceof VehicleMoveC2SPacket) {
            event.cancel();
        }
        if (!mc.player.isRiding() || Field2267 || Field2266) {
            return;
        }
        Entity entity = mc.player.getControllingVehicle();
        if ((!mc.world.isChunkLoaded((int) entity.getPos().getX() >> 4, (int) entity.getPos().getZ() >> 4) || entity.getPos().getY() < 0) && stopunloaded.getValue()) {
            return;
        }
        if (event.getPacket() instanceof VehicleMoveC2SPacket pac && (limit.getValue()) && mode.getValue() == Mode.Packet) {
            if (vehiclePackets.contains(pac)) {
                vehiclePackets.remove(pac);
            } else {
                event.cancel();
            }
        }
    }

    public enum Mode {
        Packet, Motion
    }
}
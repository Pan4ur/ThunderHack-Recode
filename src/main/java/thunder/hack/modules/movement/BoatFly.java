package thunder.hack.modules.movement;

import com.google.common.eventbus.Subscribe;
import io.netty.util.internal.ConcurrentSet;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.Thunderhack;
import thunder.hack.cmd.Command;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BoatFly extends Module {
    private final ConcurrentSet Field2263 = new ConcurrentSet();
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

    public static double[] Method1330(double d) {
        float f =mc.player.input.movementForward;
        float f2 =mc.player.input.movementSideways;
        float f3 =mc.player.prevYaw + (mc.player.getYaw() -mc.player.prevYaw) *mc.getTickDelta();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += (f > 0.0f ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += (f > 0.0f ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        double d4 = f * d * d3 + f2 * d * d2;
        double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            disable();
            return;
        }
        if ((this.automount.getValue())) {
            this.Method2868();
        }
    }

    @Override
    public void onDisable() {
        Thunderhack.TICK_TIMER = (1.0f);
        this.Field2263.clear();
        this.Field2266 = false;
        if (mc.player == null) {
            return;
        }
        if ((this.phase.getValue()) && this.mode.getValue() == Mode.Motion) {
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
        this.Field2268 = !this.Field2268;
        return this.Field2268 ? (this.offset.getValue()) : -(this.offset.getValue());
    }

    private void Method2875(VehicleMoveC2SPacket cPacketVehicleMove) {
        this.Field2263.add(cPacketVehicleMove);
       mc.player.networkHandler.sendPacket(cPacketVehicleMove);
    }

    private void Method2876(Entity entity) {
        double d = entity.getY();
        BlockPos blockPos = BlockPos.ofFloored(entity.getPos());
        for (int i = 0; i < 255; ++i) {
            if (mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                entity.setPosition(entity.getX(),blockPos.getY() + 1,entity.getZ());
                if (this.debug.getValue()) {
                    Command.sendMessage("GroundY" + entity.getPos().getY());
                }
                this.Method2875(new VehicleMoveC2SPacket(entity));
                entity.setPosition(entity.getX(),d,entity.getZ());
                break;
            }
            blockPos = blockPos.add(0, -1, 0);
        }
    }

    private void Method2868() {
        for (Entity entity :mc.world.getEntities()) {
            if (!(entity instanceof BoatEntity) || !(mc.player.squaredDistanceTo(entity) < 25.0f)) continue;
           mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, false, Hand.MAIN_HAND));
            break;
        }
    }

    @EventHandler
    public void onPlayerTravel(EventPlayerTravel ev) {
        if(!ev.isPre()) return;
        if (mc.player == null ||mc.world == null) {
            return;
        }
        if (mc.player.getControllingVehicle() == null) {
            if (this.automount.getValue()) {
                this.Method2868();
            }
            return;
        }
        if (this.phase.getValue() && this.mode.getValue() == Mode.Motion) {
           mc.player.getControllingVehicle().noClip = true;
           mc.player.getControllingVehicle().setNoGravity(true);
           mc.player.noClip = true;
        }
        if (!this.Field2267) {
           mc.player.getControllingVehicle().setNoGravity(!(this.gravity.getValue()));
           mc.player.setNoGravity(!(this.gravity.getValue()));
        }
        if (this.stop.getValue()) {
            if (this.Field2264 > this.enableticks.getValue() && !this.Field2266) {
                this.Field2264 = 0;
                this.Field2266 = true;
                this.Field2265 = this.waitticks.getValue();
            }
            if (this.Field2265 > 0 && this.Field2266) {
                --this.Field2265;
                return;
            }
            if (this.Field2265 <= 0) {
                this.Field2266 = false;
            }
        }
        Entity entity =mc.player.getControllingVehicle();
        if (this.debug.getValue()) {
            Command.sendMessage("Y" + entity.getY());
            Command.sendMessage("Fall" + entity.fallDistance);
        }
        if ((!mc.world.isChunkLoaded((int)entity.getPos().getX() >> 4, (int) entity.getPos().getZ() >> 4) || entity.getPos().getY() < 0) && this.stopunloaded.getValue()) {
            if (this.debug.getValue()) {
                Command.sendMessage("Detected unloaded chunk!");
            }
            this.Field2267 = true;
            return;
        }
        if (this.timer.getValue() != 1.0f) {
            Thunderhack.TICK_TIMER = (this.timer.getValue());
        }
        entity.setYaw(mc.player.getYaw());
        double[] dArray = Method1330(this.speed.getValue());
        double d = entity.getX() + dArray[0];
        double d2 = entity.getZ() + dArray[1];
        double d3 = entity.getY();
        if ((!mc.world.isChunkLoaded((int) d >> 4, (int) d2 >> 4) || entity.getPos().getY() < 0) && (this.stopunloaded.getValue())) {
            if (this.debug.getValue()) {
                Command.sendMessage("Detected unloaded chunk!");
            }
            this.Field2267 = true;
            return;
        }
        this.Field2267 = false;
        entity.setVelocity(entity.getVelocity().getX(),-((this.glidespeed.getValue()) / 100.0f),entity.getVelocity().getZ());
        if (this.mode.getValue() == Mode.Motion) {
            entity.setVelocity(dArray[0],entity.getVelocity().getY(),dArray[1]);
        }
        if (mc.options.jumpKey.isPressed()) {
            if (!(this.ylimit.getValue()) || entity.getY() <= (this.height.getValue())) {
                if (this.mode.getValue() == Mode.Motion) {
                    entity.setVelocity(entity.getVelocity().getX(),entity.getVelocity().getY() + (this.yspeed.getValue()),entity.getVelocity().getZ());
                } else {
                    d3 += (this.yspeed.getValue());
                }
            }
        } else if (mc.options.sneakKey.isPressed()) {
            if (this.mode.getValue() == Mode.Motion) {
                entity.setVelocity(entity.getVelocity().getX(),entity.getVelocity().getY()  + (-(this.yspeed.getValue())),entity.getVelocity().getZ());
            } else {
                d3 += (-(this.yspeed.getValue()));
            }
        }
        if (mc.player.input.movementSideways == 0.0f &&mc.player.input.movementForward == 0.0f) {
            entity.setVelocity(0,entity.getVelocity().getY(),0);
        }
        if ((this.ongroundpacket.getValue())) {
            this.Method2876(entity);
        }
        if (this.mode.getValue() != Mode.Motion) {
            entity.setPosition(d, d3, d2);
        }
        if (this.mode.getValue() == Mode.Packet) {
            this.Method2875(new VehicleMoveC2SPacket(entity));
        }
        if (this.strict.getValue()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.CLONE, mc.player);
        }
        if ((this.spoofpackets.getValue())) {
            Vec3d vec3d = entity.getPos().add(0.0, this.Method2874(), 0.0);
            BoatEntity entityBoat = new BoatEntity(mc.world, vec3d.x, vec3d.y, vec3d.z);
            entityBoat.setYaw(entity.getYaw());
            entityBoat.setPitch(entity.getPitch());
            this.Method2875(new VehicleMoveC2SPacket(entityBoat));
        }
        if ((this.remount.getValue())) {
           mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(entity, false, Hand.MAIN_HAND));
        }
        ev.cancel();
        ++this.Field2264;
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive eventNetworkPrePacketEvent) {
        if (fullNullCheck()) {
            return;
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof DisconnectS2CPacket) {
            disable();
        }
        if (!mc.player.isRiding() || this.Field2267 || this.Field2266) {
            return;
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof VehicleMoveS2CPacket &&mc.player.isRiding() && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof PlayerPositionLookS2CPacket &&mc.player.isRiding() && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof EntityS2CPacket && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
        if (eventNetworkPrePacketEvent.getPacket() instanceof EntityAttachS2CPacket && (this.cancel.getValue())) {
            eventNetworkPrePacketEvent.cancel();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send eventNetworkPostPacketEvent) {
        if (mc.player == null ||mc.world == null) {
            return;
        }
        if ((eventNetworkPostPacketEvent.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround && (this.cancelrotations.getValue()) || eventNetworkPostPacketEvent.getPacket() instanceof PlayerInputC2SPacket) &&mc.player.isRiding()) {
            eventNetworkPostPacketEvent.cancel();
        }
        if (this.Field2267 && eventNetworkPostPacketEvent.getPacket() instanceof VehicleMoveC2SPacket) {
            eventNetworkPostPacketEvent.cancel();
        }
        if (!mc.player.isRiding() || this.Field2267 || this.Field2266) {
            return;
        }
        Entity entity =mc.player.getControllingVehicle();
        if ((!mc.world.isChunkLoaded((int)entity.getPos().getX() >> 4, (int)entity.getPos().getZ() >> 4) || entity.getPos().getY() < 0) && (this.stopunloaded.getValue())) {
            return;
        }
        if (eventNetworkPostPacketEvent.getPacket() instanceof VehicleMoveC2SPacket && (this.limit.getValue()) && this.mode.getValue() == Mode.Packet) {
            VehicleMoveC2SPacket cPacketVehicleMove = eventNetworkPostPacketEvent.getPacket();
            if (this.Field2263.contains(cPacketVehicleMove)) {
                this.Field2263.remove(cPacketVehicleMove);
            } else {
                eventNetworkPostPacketEvent.cancel();
            }
        }
    }

    public enum Mode {
        Packet, Motion
    }
}
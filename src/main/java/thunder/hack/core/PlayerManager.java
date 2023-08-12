package thunder.hack.core;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.Vec2f;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.combat.Aura;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import thunder.hack.modules.Module;

import static thunder.hack.modules.Module.mc;

public class PlayerManager {
    public float serverYaw, serverPitch;
    private float yaw, pitch;
    public float lastYaw, lastPitch;
    public double currentPlayerSpeed;

    @EventHandler
    public void onSync(EventSync event) {
        if (Module.fullNullCheck()) return;

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        lastYaw = ((IClientPlayerEntity) mc.player).getLastYaw();
        lastPitch = ((IClientPlayerEntity) mc.player).getLastPitch();

        double d2 = mc.player.getX() - mc.player.prevX;
        double d3 = mc.player.getZ() - mc.player.prevZ;
        double d4 = d2 * d2 + d3 * d3;
        currentPlayerSpeed = Math.sqrt(d4);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void postSync(EventPostSync event) {
        if (Module.fullNullCheck()) return;
        mc.player.setYaw(this.yaw);
        mc.player.setPitch(this.pitch);
    }

    @EventHandler
    public void onSyncWithServer(PacketEvent.Send event){
        if(event.getPacket() instanceof PlayerMoveC2SPacket){
            serverYaw = ((PlayerMoveC2SPacket) event.getPacket()).getYaw(serverYaw);
            serverPitch = ((PlayerMoveC2SPacket) event.getPacket()).getPitch(serverPitch);
        }
        if(event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround){
            serverYaw = ((PlayerMoveC2SPacket) event.getPacket()).getYaw(serverYaw);
            serverPitch = ((PlayerMoveC2SPacket) event.getPacket()).getPitch(serverPitch);
        }
        if(event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround){
            serverYaw = ((PlayerMoveC2SPacket) event.getPacket()).getYaw(serverYaw);
            serverPitch = ((PlayerMoveC2SPacket) event.getPacket()).getPitch(serverPitch);
        }
        if(event.getPacket() instanceof PlayerMoveC2SPacket.Full){
            serverYaw = ((PlayerMoveC2SPacket) event.getPacket()).getYaw(serverYaw);
            serverPitch = ((PlayerMoveC2SPacket) event.getPacket()).getPitch(serverPitch);
        }
    }

    @EventHandler
    public void getServerPosLook(PacketEvent.Receive event){
        if(event.getPacket() instanceof PlayerPositionLookS2CPacket){
            serverYaw = ((PlayerPositionLookS2CPacket) event.getPacket()).getYaw();
            serverPitch = ((PlayerPositionLookS2CPacket) event.getPacket()).getPitch();
        }
    }

    public boolean checkRtx(float yaw, float pitch, float distance, boolean ignoreWalls) {
        Entity targetedEntity = null;
        HitResult result = ignoreWalls ? null : rayTrace(distance, yaw, pitch);
        Vec3d vec3d = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()),0);
        double distancePow2 = Math.pow(distance,2);
        if (result != null) distancePow2 = result.getPos().squaredDistanceTo(vec3d);
        Vec3d vec3d2 = getRotationVector(pitch,yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = mc.player.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, vec3d, vec3d3, box, (entity) -> !entity.isSpectator(), distancePow2);
        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3d vec3d4 = entityHitResult.getPos();
            double g = vec3d.squaredDistanceTo(vec3d4);
            if (g < distancePow2 || result == null) {
                if (entity2 instanceof LivingEntity) {
                    targetedEntity = entity2;
                    if(targetedEntity == Aura.target) return true;
                }
            }
        }
        return false;
    }

    public Entity getRtxTarger(float yaw, float pitch, float distance, boolean ignoreWalls) {
        Entity targetedEntity = null;
        HitResult result = ignoreWalls ? null : rayTrace(distance, yaw, pitch);
        Vec3d vec3d = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()),0);
        double distancePow2 = Math.pow(distance,2);
        if (result != null) distancePow2 = result.getPos().squaredDistanceTo(vec3d);
        Vec3d vec3d2 = getRotationVector(pitch,yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = mc.player.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, vec3d, vec3d3, box, (entity) -> !entity.isSpectator(), distancePow2);
        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3d vec3d4 = entityHitResult.getPos();
            double g = vec3d.squaredDistanceTo(vec3d4);
            if (g < distancePow2 || result == null) {
                if (entity2 instanceof LivingEntity) {
                    targetedEntity = entity2;
                    return targetedEntity;
                }
            }
        }
        return targetedEntity;
    }

    public HitResult rayTrace(double dst, float yaw, float pitch) {
        Vec3d vec3d = mc.player.getCameraPosVec(mc.getTickDelta());
        Vec3d vec3d2 = getRotationVector( pitch,  yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
        return mc.world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
    }

    public static float[] calcAngle(Vec3d to) {
        if (to == null) return null;
        double difX = to.x - mc.player.getEyePos().x;
        double difY = (to.y - mc.player.getEyePos().y) * -1.0;
        double difZ = to.z - mc.player.getEyePos().z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))};
    }

    public static Vec2f calcAngleVec(Vec3d to) {
        if (to == null) return null;
        double difX = to.x - mc.player.getEyePos().x;
        double difY = (to.y - mc.player.getEyePos().y) * -1.0;
        double difZ = to.z - mc.player.getEyePos().z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
        return new Vec2f((float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))));
    }

    private Vec3d getRotationVector(float yaw, float pitch){
        return new Vec3d(MathHelper.sin(-pitch * 0.017453292F) * MathHelper.cos(yaw * 0.017453292F), -MathHelper.sin(yaw * 0.017453292F), MathHelper.cos(-pitch * 0.017453292F) * MathHelper.cos(yaw * 0.017453292F));
    }

    public static float[] calcAngle(Vec3d from, Vec3d to) {
        if (to == null) return null;
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))};
    }

}

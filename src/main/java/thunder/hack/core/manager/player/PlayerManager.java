package thunder.hack.core.manager.player;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.IManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.*;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.utility.Timer;
import thunder.hack.utility.world.ExplosionUtility;
import thunder.hack.utility.math.MathUtility;

import java.util.ArrayDeque;

import static net.minecraft.util.math.MathHelper.clamp;

public class PlayerManager implements IManager {
    public float yaw, pitch, lastYaw, lastPitch, currentPlayerSpeed, averagePlayerSpeed;
    public int ticksElytraFlying, serverSideSlot;
    public final Timer switchTimer = new Timer();

    private final ArrayDeque<Float> speedResult = new ArrayDeque<>(20);

    public float bodyYaw, prevBodyYaw;

    // Мы можем зайти в инвентарь, и сервер этого не узнает, пока мы не начнем кликать
    // Юзать везде!
    // We can go into inventory and the server won't know until we start clicking
    // Use everywhere!

    public boolean inInventory;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSync(EventSync event) {
        if (Module.fullNullCheck()) return;

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        lastYaw = ((IClientPlayerEntity) mc.player).getLastYaw();
        lastPitch = ((IClientPlayerEntity) mc.player).getLastPitch();
        if (mc.currentScreen == null) inInventory = false;
        if (mc.player.isFallFlying() && mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            ticksElytraFlying++;
        } else ticksElytraFlying = 0;
    }

    @EventHandler
    public void onTick(EventTick e) {
        currentPlayerSpeed = (float) Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);

        if (speedResult.size() > 20)
            speedResult.poll();

        speedResult.add(currentPlayerSpeed);

        float average = 0.0f;

        for (Float value : speedResult) average += MathUtility.clamp(value, 0f, 20f);

        averagePlayerSpeed = average / (float) speedResult.size();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void postSync(EventPostSync event) {
        if (mc.player == null) return;

        prevBodyYaw = bodyYaw;
        bodyYaw = getBodyYaw();

        if (!ModuleManager.rotations.clientLook.getValue()) {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }

        ModuleManager.rotations.fixRotation = Float.NaN;
    }

    @EventHandler
    public void onJump(EventPlayerJump e) {
        ModuleManager.rotations.onJump(e);
    }

    @EventHandler
    public void onPlayerMove(EventFixVelocity e) {
        ModuleManager.rotations.onPlayerMove(e);
    }

    @EventHandler
    public void modifyVelocity(EventPlayerTravel e) {
        ModuleManager.rotations.modifyVelocity(e);
    }

    @EventHandler
    public void onKeyInput(EventKeyboardInput e) {
        ModuleManager.rotations.onKeyInput(e);
    }

    @EventHandler
    public void onSyncWithServer(PacketEvent.@NotNull Send event) {
        if (event.getPacket() instanceof ClickSlotC2SPacket) {
            inInventory = true;
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket slot) {
            switchTimer.reset();
            serverSideSlot = slot.getSelectedSlot();
        }
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket) {
            inInventory = false;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.@NotNull Receive event) {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket slot) {
            switchTimer.reset();
            serverSideSlot = slot.getSlot();
        }
    }

    private float getBodyYaw() {
        double x = mc.player.getX() - mc.player.prevX;
        double z = mc.player.getZ() - mc.player.prevZ;
        float offset = bodyYaw;
        if ((x * x + z * z) > 0.0025000002f) offset = (float) (MathHelper.atan2(z, x) * 57.295776f - 90.0f);
        if (mc.player.handSwingProgress > 0.0f)
            offset = ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw();
        float deltaBodyYaw = clamp(MathHelper.wrapDegrees((((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw()) - (bodyYaw + MathHelper.wrapDegrees(offset - bodyYaw) * 0.3f)), -45.0f, 75.0f);
        return (deltaBodyYaw > 50f ? deltaBodyYaw * 0.2f : 0) + ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw() - deltaBodyYaw;
    }

    public boolean checkRtx(float yaw, float pitch, float distance, float wallDistance, Aura.RayTrace rt) {
        if (rt == Aura.RayTrace.OFF)
            return true;

        HitResult result = rayTrace(distance, yaw, pitch);
        Vec3d startPoint = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        double distancePow2 = Math.pow(distance, 2);

        if (result != null)
            distancePow2 = startPoint.squaredDistanceTo(result.getPos());

        Vec3d rotationVector = getRotationVector(pitch, yaw).multiply(distance);
        Vec3d endPoint = startPoint.add(rotationVector);

        Box entityArea = mc.player.getBoundingBox().stretch(rotationVector).expand(1.0, 1.0, 1.0);

        EntityHitResult ehr;

        double maxDistance = Math.max(distancePow2, Math.pow(wallDistance, 2));

        if (rt == Aura.RayTrace.OnlyTarget && Aura.target != null)
            ehr = ProjectileUtil.raycast(mc.player, startPoint, endPoint, entityArea, e -> !e.isSpectator() && e.canHit() && e == Aura.target, maxDistance);
        else
            ehr = ProjectileUtil.raycast(mc.player, startPoint, endPoint, entityArea, e -> !e.isSpectator() && e.canHit(), maxDistance);

        if (ehr != null) {
            boolean allowedWallDistance = startPoint.squaredDistanceTo(ehr.getPos()) <= Math.pow(wallDistance, 2);
            boolean wallMissing = result == null;
            boolean wallBehindEntity = startPoint.squaredDistanceTo(ehr.getPos()) < distancePow2;
            boolean allowWallHit = wallMissing || allowedWallDistance || wallBehindEntity;

            if (allowWallHit && startPoint.squaredDistanceTo(ehr.getPos()) <= Math.pow(distance, 2))
                return ehr.getEntity() == Aura.target || Aura.target == null || rt == Aura.RayTrace.OnlyTarget;
        }

        return false;
    }

    public boolean checkRtx(float yaw, float pitch, float distance, float wallDistance, Entity entity) {
        HitResult result = rayTrace(distance, yaw, pitch);
        Vec3d startPoint = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        double distancePow2 = Math.pow(distance, 2);

        if (result != null)
            distancePow2 = startPoint.squaredDistanceTo(result.getPos());

        Vec3d rotationVector = getRotationVector(pitch, yaw).multiply(distance);
        Vec3d endPoint = startPoint.add(rotationVector);

        Box entityArea = mc.player.getBoundingBox().stretch(rotationVector).expand(1.0, 1.0, 1.0);

        EntityHitResult ehr;

        double maxDistance = Math.max(distancePow2, Math.pow(wallDistance, 2));

        ehr = ProjectileUtil.raycast(mc.player, startPoint, endPoint, entityArea, e -> !e.isSpectator() && e.canHit() && e == entity, maxDistance);

        if (ehr != null) {
            boolean allowedWallDistance = startPoint.squaredDistanceTo(ehr.getPos()) <= Math.pow(wallDistance, 2);
            boolean wallMissing = result == null;
            boolean wallBehindEntity = startPoint.squaredDistanceTo(ehr.getPos()) < distancePow2;
            boolean allowWallHit = wallMissing || allowedWallDistance || wallBehindEntity;

            if (allowWallHit && startPoint.squaredDistanceTo(ehr.getPos()) <= Math.pow(distance, 2))
                return ehr.getEntity() == entity;
        }

        return false;
    }


    public Entity getRtxTarget(float yaw, float pitch, float distance, boolean ignoreWalls) {
        Entity targetedEntity = null;
        HitResult result = ignoreWalls ? null : rayTrace(distance, yaw, pitch);
        Vec3d vec3d = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        double distancePow2 = Math.pow(distance, 2);
        if (result != null) distancePow2 = result.getPos().squaredDistanceTo(vec3d);
        Vec3d vec3d2 = getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = mc.player.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, vec3d, vec3d3, box, (entity) -> !entity.isSpectator() && entity.canHit(), distancePow2);
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

    public Vec3d getRtxPoint(float yaw, float pitch, float distance) {
        Vec3d vec3d = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        double distancePow2 = Math.pow(distance, 2);
        Vec3d vec3d2 = getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = mc.player.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, vec3d, vec3d3, box, (entity) -> !entity.isSpectator() && entity.canHit(), distancePow2);
        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3d vec3d4 = entityHitResult.getPos();
            if (entity2 instanceof LivingEntity) {
                return vec3d4;
            }
        }
        return null;
    }

    public boolean isLookingAtBox(float yaw, float pitch, BlockPos blockPos) {
        Vec3d vec3d = mc.player.getCameraPosVec(1f);
        Vec3d vec3d2 = getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * 7, vec3d2.y * 7, vec3d2.z * 7);
        BlockHitResult result = ExplosionUtility.rayCastBlock(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player), blockPos);
        return result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos);
    }

    public HitResult rayTrace(double dst, float yaw, float pitch) {
        Vec3d vec3d = mc.player.getCameraPosVec(1f);
        Vec3d vec3d2 = getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
        return mc.world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
    }

    public HitResult getRtxTarget(float yaw, float pitch, double x, double y, double z) {
        HitResult result = rayTrace(5, yaw, pitch, x, y, z);
        Vec3d vec3d = new Vec3d(x, y, z).add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        double distancePow2 = 25;
        if (result != null)
            distancePow2 = result.getPos().squaredDistanceTo(vec3d);
        Vec3d vec3d2 = getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * 5, vec3d2.y * 5, vec3d2.z * 5);
        Box box = new Box(x - .3, y, z - .3, x + .3, y + 1.8, z + .3).stretch(vec3d2.multiply(5)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, vec3d, vec3d3, box, (entity) -> !entity.isSpectator() && entity.canHit(), distancePow2);
        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3d vec3d4 = entityHitResult.getPos();
            double g = vec3d.squaredDistanceTo(vec3d4);
            if (g < distancePow2 || result == null) {
                if (entity2 instanceof LivingEntity) {
                    return entityHitResult;
                }
            }
        }
        return result;
    }

    public boolean isInWeb() {
        Box pBox = mc.player.getBoundingBox();
        BlockPos pBlockPos = BlockPos.ofFloored(mc.player.getPos());

        for (int x = pBlockPos.getX() - 2; x <= pBlockPos.getX() + 2; x++) {
            for (int y = pBlockPos.getY() - 1; y <= pBlockPos.getY() + 4; y++) {
                for (int z = pBlockPos.getZ() - 2; z <= pBlockPos.getZ() + 2; z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (pBox.intersects(new Box(bp)) && mc.world.getBlockState(bp).getBlock() == Blocks.COBWEB)
                        return true;
                }
            }
        }

        return false;
    }

    public HitResult rayTrace(double dst, float yaw, float pitch, double x, double y, double z) {
        Vec3d vec3d = new Vec3d(x, y, z);
        Vec3d vec3d2 = getRotationVector(pitch, yaw);
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

    public @NotNull Vec3d getRotationVector(float yaw, float pitch) {
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

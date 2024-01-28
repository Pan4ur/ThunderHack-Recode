package thunder.hack.core.impl;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;
import thunder.hack.ThunderHack;
import thunder.hack.cmd.Command;
import thunder.hack.core.IManager;
import thunder.hack.events.impl.*;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.modules.Module;
import thunder.hack.modules.combat.Aura;
import thunder.hack.modules.movement.NoSlow;
import thunder.hack.utility.Timer;

import static net.minecraft.util.math.MathHelper.clamp;
import static thunder.hack.modules.Module.mc;

public class PlayerManager implements IManager {
    public float yaw, pitch, lastYaw, lastPitch, currentPlayerSpeed;
    public int ticksElytraFlying, serverSideSlot;
    public final Timer switchTimer = new Timer();

    public float bodyYaw, prevBodyYaw;

    // Мы можем зайти в инвентарь, и сервер этого не узнает, пока мы не начнем кликать
    // Юзать везде!
    public boolean inInventory;

    @EventHandler
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
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void postSync(EventPostSync event) {
        if (mc.player == null) return;

        prevBodyYaw = bodyYaw;
        bodyYaw = getBodyYaw();

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    @EventHandler
    public void onSyncWithServer(PacketEvent.@NotNull Send event) {
        if (event.getPacket() instanceof ClickSlotC2SPacket) {
            inInventory = true;
        }
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket slot) {
            if (serverSideSlot == slot.getSelectedSlot() && !(ModuleManager.noSlow.isEnabled() && NoSlow.mode.getValue() == NoSlow.Mode.StrictNCP)) {
                event.cancel();
                ModuleManager.mainSettings.debug("Double slot packet!");
            }

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
        if (mc.player.handSwingProgress > 0.0f) offset = ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw();
        float deltaBodyYaw = clamp(MathHelper.wrapDegrees((((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw()) - (bodyYaw + MathHelper.wrapDegrees(offset - bodyYaw) * 0.3f)), -45.0f, 75.0f);
        return (deltaBodyYaw > 50f ? deltaBodyYaw * 0.2f : 0)  + ((IClientPlayerEntity) MinecraftClient.getInstance().player).getLastYaw() - deltaBodyYaw;
    }

    public boolean checkRtx(float yaw, float pitch, float distance, boolean ignoreWalls, Aura.RayTrace rt) {
        if (rt == Aura.RayTrace.OFF)
            return true;

        Entity targetedEntity;
        HitResult result = ignoreWalls ? null : rayTrace(distance, yaw, pitch);
        Vec3d vec3d = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        double distancePow2 = Math.pow(distance, 2);
        if (result != null) distancePow2 = result.getPos().squaredDistanceTo(vec3d);
        Vec3d vec3d2 = getRotationVector(pitch, yaw);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * distance, vec3d2.y * distance, vec3d2.z * distance);
        Box box = mc.player.getBoundingBox().stretch(vec3d2.multiply(distance)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, vec3d, vec3d3, box, (entity) -> !entity.isSpectator() && entity.canHit(), distancePow2);
        if (entityHitResult != null) {
            if (entityHitResult.getEntity() instanceof FireworkRocketEntity)
                return false;
            Entity entity2 = entityHitResult.getEntity();
            if (vec3d.squaredDistanceTo(entityHitResult.getPos()) < distancePow2 || result == null) {
                targetedEntity = entity2;
                return targetedEntity == Aura.target || Aura.target == null || rt != Aura.RayTrace.AllEntities;
            }
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

    public HitResult rayTrace(double dst, float yaw, float pitch) {
        Vec3d vec3d = mc.player.getCameraPosVec(mc.getTickDelta());
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
        Box box = new Box(x - .3, y, z - .3, x + .3, y + mc.player.getEyeHeight(mc.player.getPose()), z + .3).stretch(vec3d2.multiply(5)).expand(1.0, 1.0, 1.0);
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

    private @NotNull Vec3d getRotationVector(float yaw, float pitch) {
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

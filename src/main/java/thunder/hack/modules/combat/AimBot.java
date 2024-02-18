package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.math.PredictUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Math.abs;
import static net.minecraft.util.hit.HitResult.Type.ENTITY;
import static net.minecraft.util.math.MathHelper.wrapDegrees;
import static thunder.hack.core.impl.PlayerManager.calcAngleVec;

public final class AimBot extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.BowAim);
    private final Setting<Rotation> rotation = new Setting<>("Rotation", Rotation.Silent, v -> mode.getValue() != Mode.AimAssist);
    public final Setting<Float> aimRange = new Setting<>("Range", 20f, 1f, 30f, v -> mode.getValue() != Mode.AimAssist);
    public final Setting<Integer> aimStrength = new Setting<>("AimStrength", 30, 1, 100, v -> mode.getValue() == Mode.AimAssist);
    public final Setting<Boolean> ignoreWalls = new Setting<>("Ignore Walls", true, v -> mode.getValue() == Mode.CSAim);
    public final Setting<Boolean> ignoreInvisible = new Setting<>("IgnoreInvis", false);
    public Setting<Float> rotYawRandom = new Setting<>("Yaw Random", 0f, 0f, 3f, v -> mode.getValue() == Mode.CSAim);
    public Setting<Float> rotPitchRandom = new Setting<>("Pitch Random", 0f, 0f, 3f, v -> mode.getValue() == Mode.CSAim);
    public Setting<Float> predict = new Setting<>("Aim Predict", 0.5f, 0.5f, 2f, v -> mode.getValue() == Mode.CSAim);
    public Setting<Integer> delay = new Setting<>("Shoot delay", 5, 0, 10, v -> mode.getValue() == Mode.CSAim);
    public Setting<Integer> fov = new Setting<>("FOV", 65, 10, 360, v -> mode.getValue() == Mode.CSAim);
    public Setting<Integer> predictTicks = new Setting<>("PredictTicks", 2, 0, 20, v -> mode.getValue() == Mode.BowAim);
    private final Setting<Part> part = new Setting<>("Part Mode", Part.Chest, v -> mode.getValue() == Mode.CSAim);

    public static Entity target;
    public static float ppx, ppy, ppz, pmx, pmy, pmz;

    private float rotationYaw, rotationPitch;
    private Box debug_box;
    private float assistAcceleration;
    private static AimBot instance;

    public AimBot() {
        super("AimBot", Category.COMBAT);
        instance = this;
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (mode.getValue() == Mode.BowAim) {
            if (!(mc.player.getActiveItem().getItem() instanceof BowItem)) return;

            PlayerEntity nearestTarget = ThunderHack.combatManager.getTargetByFOV(128);

            if (nearestTarget == null) return;

            float currentDuration = (float) (mc.player.getActiveItem().getMaxUseTime() - mc.player.getItemUseTime()) / 20.0f;

            currentDuration = (currentDuration * currentDuration + currentDuration * 2.0f) / 3.0f;

            if (currentDuration >= 1.0f) currentDuration = 1.0f;

            float pitch = (float) (-Math.toDegrees(calculateArc(nearestTarget, currentDuration * 3.0f)));

            if (Float.isNaN(pitch)) return;

            PlayerEntity predictedEntity = PredictUtility.predictPlayer(nearestTarget, predictTicks.getValue());
            double iX = predictedEntity.getX() - predictedEntity.prevX;
            double iZ = predictedEntity.getZ() - predictedEntity.prevZ;
            double distance = mc.player.distanceTo(predictedEntity);
            distance -= distance % 2.0;
            iX = distance / 2.0 * iX * (mc.player.isSprinting() ? 1.3 : 1.1);
            iZ = distance / 2.0 * iZ * (mc.player.isSprinting() ? 1.3 : 1.1);
            rotationYaw = (float) Math.toDegrees(Math.atan2(predictedEntity.getZ() + iZ - mc.player.getZ(), predictedEntity.getX() + iX - mc.player.getX())) - 90.0f;
            rotationPitch = pitch;
        } else if (mode.getValue() == Mode.CSAim) {
            calcThread();
            if (target != null && (mc.player.canSee(target) || ignoreWalls.getValue()))
                if (mc.player.age % delay.getValue() == 0)
                    sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, PlayerUtility.getWorldActionId(mc.world)));
        } else {
            if (mc.crosshairTarget.getType() == ENTITY) {
                assistAcceleration = 0;
                return;
            }
            rotationYaw = Float.NaN;
            PlayerEntity nearestTarget = ThunderHack.combatManager.getNearestTarget(5);
            assistAcceleration += aimStrength.getValue() / 10000f;

            if (nearestTarget != null) {
                rotationYaw = calcAngleVec(ModuleManager.aura.getLegitLook(nearestTarget)).x;
                return;
            }
        }

        if (target != null || (mode.getValue() == Mode.BowAim && mc.player.getActiveItem().getItem() instanceof BowItem)) {
            if (rotation.getValue() == Rotation.Silent) {
                mc.player.setYaw(rotationYaw);
                mc.player.setPitch(rotationPitch);
            }
        } else {
            if (mode.getValue() == Mode.CSAim) {
                rotationYaw = mc.player.getYaw();
                rotationPitch = mc.player.getPitch();
            }
        }
    }

    @Override
    public void onEnable() {
        target = null;
        debug_box = null;
        ppx = ppy = ppz = pmx = pmz = pmy = 0;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
    }

    public void onRender3D(MatrixStack stack) {
        if (debug_box != null) Render3DEngine.drawFilledBox(stack, debug_box, new Color(0x3400FF41, true));

        if (mode.getValue() == Mode.AimAssist) {
            if (Float.isNaN(rotationYaw)) return;
            double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
            rotationYaw = (float) (rotationYaw - (rotationYaw - mc.player.getYaw()) % gcdFix);

            if (abs((mc.player.getYaw() - rotationYaw)) > 170) {
                mc.player.setYaw(rotationYaw);
                assistAcceleration = 1;
                return;
            }
            mc.player.setYaw((float) Render2DEngine.interpolate(mc.player.getYaw(), rotationYaw, assistAcceleration));
            return;
        }

        if (target != null && (mc.player.canSee(target) || ignoreWalls.getValue())) {
            if (rotation.getValue() == Rotation.Client) {
                mc.player.setYaw(rotationYaw);
                mc.player.setPitch(rotationPitch);
            }
        } else {
            if (mode.getValue() == Mode.CSAim) {
                rotationYaw = mc.player.getYaw();
                rotationPitch = mc.player.getPitch();
            }
        }

        if (rotation.getValue() == Rotation.Client && mode.getValue() == Mode.BowAim && mc.player.getActiveItem().getItem() instanceof BowItem) {
            mc.player.setYaw((float) Render2DEngine.interpolate(mc.player.prevYaw, rotationYaw, mc.getTickDelta()));
            mc.player.setPitch((float) Render2DEngine.interpolate(mc.player.prevPitch, rotationPitch, mc.getTickDelta()));
        }
    }


    private float calculateArc(@NotNull PlayerEntity target, double duration) {
        double yArc = target.getY() + (double) (target.getEyeHeight(target.getPose())) - (mc.player.getY() + (double) mc.player.getEyeHeight(mc.player.getPose()));
        double dX = target.getX() - mc.player.getX();
        double dZ = target.getZ() - mc.player.getZ();
        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);
        return calculateArc(duration, dirRoot, yArc);
    }

    private float calculateArc(double d, double dr, double y) {
        y = 2.0 * y * (d * d);
        y = 0.05000000074505806 * ((0.05000000074505806 * (dr * dr)) + y);
        y = Math.sqrt(d * d * d * d - y);
        d = d * d - y;
        y = Math.atan2(d * d + y, 0.05000000074505806 * dr);
        d = Math.atan2(d, 0.05000000074505806 * dr);
        return (float) Math.min(y, d);
    }

    private void calcThread() {
        if (target == null) {
            findTarget();
            return;
        }
        if (skipEntity(target)) {
            target = null;
            return;
        }

        Vec3d targetVec = getMatrix4Vec(target);
        if (targetVec == null) return;

        float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(targetVec.z - mc.player.getZ(), (targetVec.x - mc.player.getX()))) - 90) - rotationYaw);
        float delta_pitch = ((float) (-Math.toDegrees(Math.atan2(targetVec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((targetVec.x - mc.player.getX()), 2) + Math.pow(targetVec.z - mc.player.getZ(), 2))))) - rotationPitch);

        if (delta_yaw > 180) {
            delta_yaw = delta_yaw - 180;
        }

        float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), MathUtility.random(-40.0F, -60.0F), MathUtility.random(40.0F, 60.0F));

        float newYaw = rotationYaw + (delta_yaw > 0 ? deltaYaw : -deltaYaw) + MathUtility.random(-rotYawRandom.getValue(), rotYawRandom.getValue());
        float newPitch = MathHelper.clamp(rotationPitch + MathHelper.clamp(delta_pitch, MathUtility.random(-10.0F, -20.0F), MathUtility.random(10, 20)), -90.0F, 90.0F) + MathUtility.random(-rotPitchRandom.getValue(), rotPitchRandom.getValue());

        double gcdFix1 = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        double gcdFix2 = Math.pow(gcdFix1, 3.0) * 8.0;
        double gcdFix = gcdFix2 * 0.15000000596046448;

        rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
        rotationPitch = (float) (newPitch - (newPitch - rotationPitch) % gcdFix);
    }

    public Vec3d getMatrix4Vec(Entity target) {
        Vec3d cuteTargetPos = getResolvedPos(target);
        float aimPoint = switch (part.getValue()) {
            case Head -> 0.05f;
            case Neck -> 0.3f;
            case Chest -> 0.5f;
            case Leggings -> 0.9f;
            case Boots -> 1.3f;
        };

        aimPoint = 1.6f - aimPoint;

        Vec3d v1 = new Vec3d(cuteTargetPos.getX(), cuteTargetPos.getY() + aimPoint, cuteTargetPos.getZ());
        if (mc.player.canSee(target)) {
            return v1;
        }
        return null;
    }

    public void findTarget() {
        List<Entity> first_stage = new CopyOnWriteArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (skipEntity(entity)) continue;
            first_stage.add(entity);
        }

        float best_distance = 144;
        Entity best_entity = null;

        for (Entity ent : first_stage) {
            float temp_dst = (float) Math.sqrt(mc.player.squaredDistanceTo(getResolvedPos(ent)));
            if (temp_dst < best_distance) {
                best_entity = ent;
                best_distance = temp_dst;
            }
        }
        target = best_entity;
    }

    private boolean skipEntity(Entity entity) {
        if (!(entity instanceof LivingEntity ent)) return true;
        if (ent.isDead()) return true;
        if (!entity.isAlive()) return true;
        if (entity instanceof ArmorStandEntity) return true;
        if (ModuleManager.antiBot.isEnabled() && AntiBot.bots.contains(entity)) return true;
        if (!(entity instanceof PlayerEntity)) return true;
        if (entity == mc.player) return true;
        if (entity.isInvisible() && ignoreInvisible.getValue()) return true;
        if (ThunderHack.friendManager.isFriend((PlayerEntity) entity)) return true;
        if (Math.abs(getYawToEntityNew(entity)) > fov.getValue()) return true;
        return mc.player.squaredDistanceTo(getResolvedPos(entity)) > aimRange.getPow2Value();
    }

    public float getYawToEntityNew(@NotNull Entity entity) {
        return getYawBetween(mc.player.getYaw(), mc.player.getX(), mc.player.getZ(), entity.getX(), entity.getZ());
    }

    public float getYawBetween(float yaw, double srcX, double srcZ, double destX, double destZ) {
        double xDist = destX - srcX;
        double zDist = destZ - srcZ;
        float yaw1 = (float) (StrictMath.atan2(zDist, xDist) * 180.0 / 3.141592653589793) - 90.0f;
        return yaw + MathHelper.wrapDegrees(yaw1 - yaw);
    }

    private Vec3d getResolvedPos(@NotNull Entity pl) {
        return new Vec3d(pl.getX() + pl.getVelocity().x * predict.getValue(), pl.getY(), pl.getZ() + pl.getVelocity().z * predict.getValue());
    }


    private enum Part {
        Chest, Head, Neck, Leggings, Boots
    }

    private enum Rotation {
        Client, Silent
    }

    private enum Mode {
        CSAim, AimAssist, BowAim
    }

    public static AimBot getInstance() {
        return instance;
    }
}

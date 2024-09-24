package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.math.PredictUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.util.hit.HitResult.Type.ENTITY;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public final class AimBot extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.BowAim);
    private final Setting<Rotation> rotation = new Setting<>("Rotation", Rotation.Silent, v -> mode.getValue() != Mode.AimAssist);
    private final Setting<Float> aimRange = new Setting<>("Range", 20f, 1f, 30f, v -> mode.getValue() != Mode.AimAssist);
    private final Setting<Integer> aimStrength = new Setting<>("AimStrength", 30, 1, 100, v -> mode.getValue() == Mode.AimAssist);
    private final Setting<Integer> aimSmooth = new Setting<>("AimSmooth", 45, 1, 180, v -> mode.getValue() == Mode.AimAssist);
    private final Setting<Integer> aimtime = new Setting<>("AimTime", 2, 1, 10, v -> mode.getValue() == Mode.AimAssist);
    private final Setting<Boolean> ignoreWalls = new Setting<>("IgnoreWalls", true, v -> mode.getValue() == Mode.CSAim || mode.is(Mode.AimAssist));
    private final Setting<Boolean> ignoreTeam = new Setting<>("IgnoreTeam", true, v -> mode.getValue() == Mode.CSAim || mode.is(Mode.AimAssist));
    private final Setting<Integer> reactionTime = new Setting<>("ReactionTime", 80, 1, 500, v -> mode.getValue() == Mode.AimAssist && !ignoreWalls.getValue());
    private final Setting<Boolean> ignoreInvisible = new Setting<>("IgnoreInvis", false, v -> mode.is(Mode.AimAssist));
    private final Setting<Float> rotYawRandom = new Setting<>("YawRandom", 0f, 0f, 3f, v -> mode.getValue() == Mode.CSAim);
    private final Setting<Float> rotPitchRandom = new Setting<>("PitchRandom", 0f, 0f, 3f, v -> mode.getValue() == Mode.CSAim);
    private final Setting<Float> predict = new Setting<>("AimPredict", 0.5f, 0.5f, 8f, v -> mode.getValue() == Mode.CSAim);
    private final Setting<Integer> delay = new Setting<>("Shoot delay", 5, 0, 10, v -> mode.getValue() == Mode.CSAim);
    private final Setting<Integer> fov = new Setting<>("FOV", 65, 10, 360, v -> mode.getValue() == Mode.CSAim);
    private final Setting<Integer> predictTicks = new Setting<>("PredictTicks", 2, 0, 20, v -> mode.getValue() == Mode.BowAim);
    private final Setting<Bone> part = new Setting<>("Bone", Bone.Head, v -> mode.getValue() == Mode.CSAim);

    private Entity target;

    private float rotationYaw, rotationPitch, assistAcceleration;
    private int aimTicks = 0;
    private Timer visibleTime = new Timer();

    public AimBot() {
        super("AimBot", Category.COMBAT);
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mode.getValue() == Mode.BowAim) {
            if (!(mc.player.getActiveItem().getItem() instanceof BowItem)) return;

            PlayerEntity nearestTarget = Managers.COMBAT.getTargetByFOV(128);

            if (nearestTarget == null) return;

            float currentDuration = (float) (mc.player.getActiveItem().getMaxUseTime(mc.player) - mc.player.getItemUseTime()) / 20.0f;

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
        } else {
            if (mc.crosshairTarget.getType() == ENTITY)
                aimTicks++;
            else
                aimTicks = 0;

            if (aimTicks >= aimtime.getValue()) {
                assistAcceleration = 0;
                return;
            }

            PlayerEntity nearestTarget = Managers.COMBAT.getNearestTarget(5);
            assistAcceleration += aimStrength.getValue() / 10000f;

            if (nearestTarget != null) {
                if (!mc.player.canSee(nearestTarget)) {
                    if (!ignoreWalls.getValue())
                        visibleTime.reset();
                }

                if (!visibleTime.passedMs(reactionTime.getValue())) {
                    rotationYaw = Float.NaN;
                    return;
                }

                if (Float.isNaN(rotationYaw))
                    rotationYaw = mc.player.getYaw();

                float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(nearestTarget.getEyePos().z - mc.player.getZ(), (nearestTarget.getEyePos().x - mc.player.getX()))) - 90) - rotationYaw);
                if (delta_yaw > 180)
                    delta_yaw = delta_yaw - 180;
                float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), -aimSmooth.getValue(), aimSmooth.getValue());
                float newYaw = rotationYaw + (delta_yaw > 0 ? deltaYaw : -deltaYaw);
                double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0)) * 1.2;
                rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
            } else rotationYaw = Float.NaN;
        }

        if (!Float.isNaN(rotationYaw))
            ModuleManager.rotations.fixRotation = rotationYaw;
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (mode.is(Mode.AimAssist))
            return;

        if (mode.is(Mode.CSAim)) {
            if (target != null && (mc.player.canSee(target) || ignoreWalls.getValue())) {
                if (mc.player.age % delay.getValue() == 0) {
                    event.addPostAction(() -> sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch())));
                }
            } else {
                rotationYaw = mc.player.getYaw();
                rotationPitch = mc.player.getPitch();
            }
        }

        if (target != null || (mode.getValue() == Mode.BowAim && mc.player.getActiveItem().getItem() instanceof BowItem)) {
            if (rotation.getValue() == Rotation.Silent) {
                mc.player.setYaw(rotationYaw);
                mc.player.setPitch(rotationPitch);
            }
        }
    }

    @Override
    public void onEnable() {
        target = null;
        rotationYaw = mc.player.getYaw();
        rotationPitch = mc.player.getPitch();
    }

    public void onRender3D(MatrixStack stack) {
        if (mode.getValue() == Mode.AimAssist) {
            if (Float.isNaN(rotationYaw)) return;
            mc.player.setYaw((float) Render2DEngine.interpolate(mc.player.getYaw(), rotationYaw, assistAcceleration));
            return;
        }

        if (target != null && (mc.player.canSee(target) || ignoreWalls.getValue())) {
            if (rotation.getValue() == Rotation.Client) {
                mc.player.setYaw((float) Render2DEngine.interpolate(mc.player.prevYaw, rotationYaw, Render3DEngine.getTickDelta()));
                mc.player.setPitch((float) Render2DEngine.interpolate(mc.player.prevPitch, rotationPitch, Render3DEngine.getTickDelta()));
            }
        } else {
            if (mode.getValue() == Mode.CSAim) {
                rotationYaw = mc.player.getYaw();
                rotationPitch = mc.player.getPitch();
            }
        }

        if (rotation.getValue() == Rotation.Client && mode.getValue() == Mode.BowAim && mc.player.getActiveItem().getItem() instanceof BowItem) {
            mc.player.setYaw((float) Render2DEngine.interpolate(mc.player.prevYaw, rotationYaw, Render3DEngine.getTickDelta()));
            mc.player.setPitch((float) Render2DEngine.interpolate(mc.player.prevPitch, rotationPitch, Render3DEngine.getTickDelta()));
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

        Vec3d targetVec = getResolvedPos(target).add(0, part.getValue().getH(), 0);

        if (targetVec == null)
            return;

        float delta_yaw = wrapDegrees((float) wrapDegrees(Math.toDegrees(Math.atan2(targetVec.z - mc.player.getZ(), (targetVec.x - mc.player.getX()))) - 90) - rotationYaw);
        float delta_pitch = ((float) (-Math.toDegrees(Math.atan2(targetVec.y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose())), Math.sqrt(Math.pow((targetVec.x - mc.player.getX()), 2) + Math.pow(targetVec.z - mc.player.getZ(), 2))))) - rotationPitch);

        if (delta_yaw > 180)
            delta_yaw = delta_yaw - 180;

        float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), MathUtility.random(-40.0F, -60.0F), MathUtility.random(40.0F, 60.0F));

        float newYaw = rotationYaw + (delta_yaw > 0 ? deltaYaw : -deltaYaw) + MathUtility.random(-rotYawRandom.getValue(), rotYawRandom.getValue());
        float newPitch = MathHelper.clamp(rotationPitch + MathHelper.clamp(delta_pitch, MathUtility.random(-10.0F, -20.0F), MathUtility.random(10, 20)), -90.0F, 90.0F) + MathUtility.random(-rotPitchRandom.getValue(), rotPitchRandom.getValue());

        double gcdFix = (Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 8.0) * 0.15000000596046448;
        rotationYaw = (float) (newYaw - (newYaw - rotationYaw) % gcdFix);
        rotationPitch = (float) (newPitch - (newPitch - rotationPitch) % gcdFix);
    }

    public void findTarget() {
        List<Entity> first_stage = new CopyOnWriteArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (skipEntity(entity)) continue;
            first_stage.add(entity);
        }

        float best_fov = fov.getValue();
        Entity best_entity = null;

        for (Entity ent : first_stage) {
            float temp_fov = Math.abs(((float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(ent.getZ() - mc.player.getZ(), ent.getX() - mc.player.getX())) - 90.0)) - MathHelper.wrapDegrees(mc.player.getYaw()));
            if (temp_fov < best_fov) {
                best_entity = ent;
                best_fov = temp_fov;
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
        if (!(entity instanceof PlayerEntity pl)) return true;
        if (entity == mc.player) return true;
        if (entity.isInvisible() && ignoreInvisible.getValue()) return true;
        if (Managers.FRIEND.isFriend(pl)) return true;
        if (Math.abs(getYawToEntityNew(entity)) > fov.getValue()) return true;
        if (pl.getTeamColorValue() == mc.player.getTeamColorValue() && ignoreTeam.getValue() && mc.player.getTeamColorValue() != 16777215)
            return true;
        return mc.player.squaredDistanceTo(getResolvedPos(entity)) > aimRange.getPow2Value();
    }

    public float getYawToEntityNew(@NotNull Entity entity) {
        return getYawBetween(mc.player.getYaw(), mc.player.getX(), mc.player.getZ(), entity.getX(), entity.getZ());
    }

    public float getYawBetween(float yaw, double srcX, double srcZ, double destX, double destZ) {
        double xDist = destX - srcX;
        double zDist = destZ - srcZ;
        float yaw1 = (float) (StrictMath.atan2(zDist, xDist) * 180.0 / 3.141592653589793) - 90.0f;
        return yaw + wrapDegrees(yaw1 - yaw);
    }

    private Vec3d getResolvedPos(@NotNull Entity pl) {
        return new Vec3d(pl.getX() + (pl.getX() - pl.prevX) * predict.getValue(), pl.getY(), pl.getZ() + (pl.getZ() - pl.prevZ) * predict.getValue());
    }

    private enum Bone {
        Head(1.7f),
        Neck(1.5f),
        Torso(1.f),
        Tights(0.8f),
        Feet(0.25f);

        private final float h;

        Bone(float h) {
            this.h = h;
        }

        public float getH() {
            return h;
        }
    }

    private enum Rotation {
        Client, Silent
    }

    private enum Mode {
        CSAim, AimAssist, BowAim
    }
}
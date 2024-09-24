package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.events.impl.EventSync;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.MovementUtility;

import java.util.Comparator;
import java.util.HashMap;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class PearlChaser extends Module {
    public PearlChaser() { //todo better targeting?..
        super("PearlChaser", Category.MISC);
    }

    private final Setting<BooleanSettingGroup> stopMotion = new Setting<>("StopMotion", new BooleanSettingGroup(false));
    private final Setting<Boolean> legitStop = new Setting<>("LegitStop", false).addToGroup(stopMotion);
    private final Setting<Boolean> pauseAura = new Setting<>("PauseAura", false);
    private final Setting<Boolean> onlyOnGround = new Setting<>("OnlyOnGround", false);
    private final Setting<Boolean> noMove = new Setting<>("NoMove", false);
    private final Setting<Boolean> onlyTarget = new Setting<>("OnlyTarget", false);

    private Runnable postSyncAction;
    private final Timer delayTimer = new Timer();
    private BlockPos targetBlock;
    private int lastPearlId;
    private int lastOurPearlId;
    private HashMap<PlayerEntity, Long> targets = new HashMap<>();

    @EventHandler
    public void onEntitySpawn(EventEntitySpawn e) {
        if (e.getEntity() instanceof EnderPearlEntity)
            mc.world.getPlayers().stream()
                    .min(Comparator.comparingDouble((p) -> p.squaredDistanceTo(e.getEntity().getPos())))
                    .ifPresent((player) -> {
                        if (player.equals(mc.player))
                            lastOurPearlId = e.getEntity().getId();
                    });

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSync(EventSync event) {
        if (onlyTarget.getValue()) {
            if (Aura.target != null && ModuleManager.aura.isEnabled() && Aura.target instanceof PlayerEntity pl && !targets.containsKey(pl))
                targets.put(pl, System.currentTimeMillis());

            if (AutoCrystal.target != null && ModuleManager.autoCrystal.isEnabled() && AutoCrystal.target instanceof PlayerEntity pl && !targets.containsKey(pl))
                targets.put(pl, System.currentTimeMillis());

            new HashMap<>(targets).forEach((k, v) -> {
                if (System.currentTimeMillis() - v > 10000)
                    targets.remove(k);
            });
        }

        // Анти селфкилл
        if (mc.player.getHealth() < 5)
            return;

        // Антиспам
        if (!delayTimer.passedMs(1000))
            return;

        for (Entity ent : mc.world.getEntities()) {
            if (!(ent instanceof EnderPearlEntity)) continue;
            if (ent.getId() == lastPearlId || ent.getId() == lastOurPearlId) continue;
            mc.world.getPlayers().stream()
                    .filter(e -> targets.containsKey(e) || !onlyTarget.getValue())
                    .min(Comparator.comparingDouble((p) -> p.squaredDistanceTo(ent.getPos())))
                    .ifPresent((player) -> {
                        if (!player.equals(mc.player)) {
                            targetBlock = calcTrajectory(ent);
                            lastPearlId = ent.getId();
                        }
                    });
        }

        // Анти NPE
        if (targetBlock == null)
            return;

        // Нет смысла кидать если кидают в нас
        if (mc.player.squaredDistanceTo(targetBlock.toCenterPos()) < 49)
            return;

        float rotationPitch = (float) (-Math.toDegrees(calcTrajectory(targetBlock)));
        float rotationYaw = (float) Math.toDegrees(Math.atan2(targetBlock.getZ() + 0.5f - mc.player.getZ(), targetBlock.getX() + 0.5f - mc.player.getX())) - 90.0f;
        BlockPos tracedBP = checkTrajectory(rotationYaw, rotationPitch);

        if (tracedBP == null || targetBlock.getSquaredDistance(tracedBP.toCenterPos()) > 36)
            return;

        if(pauseAura.getValue() && ModuleManager.aura.isEnabled())
            ModuleManager.aura.pause();

        if(onlyOnGround.getValue() && !mc.player.isOnGround())
            return;

        if(noMove.getValue() && MovementUtility.isMoving())
            return;

        if(stopMotion.getValue().isEnabled()) {
            if(!legitStop.getValue())
                mc.player.setVelocity(0,0,0);
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            mc.player.input.movementForward = 0;
            mc.player.input.movementSideways = 0;
            return;
        }

        sendMessage(isRu() ?
                ("Догоняем перл! Позиция X:" + tracedBP.getX() + " Y:" + tracedBP.getY() + " Z:" + tracedBP.getZ() + " Углы Y:" + rotationYaw + " P:" + rotationPitch) :
                ("Chasing pearl on X:" + tracedBP.getX() + " Y:" + tracedBP.getY() + " Z:" + tracedBP.getZ() + " Angle Y:" + rotationYaw + " P:" + rotationPitch));

        mc.player.setYaw(rotationYaw);
        mc.player.setPitch(MathUtility.clamp(rotationPitch, -89, 89));

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        postSyncAction = () -> {
            int epSlot = findEPSlot();
            int originalSlot = mc.player.getInventory().selectedSlot;
            if (epSlot != -1) {
                mc.player.getInventory().selectedSlot = epSlot;
                sendPacket(new UpdateSelectedSlotC2SPacket(epSlot));
                sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, yaw, pitch));
                sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                mc.player.getInventory().selectedSlot = originalSlot;
                sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
            }
        };

        targetBlock = null;
        delayTimer.reset();
    }

    @EventHandler
    public void onPostSync(EventPostSync event) {
        if (postSyncAction != null) {
            postSyncAction.run();
            postSyncAction = null;
        }
    }

    private int findEPSlot() {
        int epSlot = -1;
        if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL)
            epSlot = mc.player.getInventory().selectedSlot;
        if (epSlot == -1)
            for (int l = 0; l < 9; ++l)
                if (mc.player.getInventory().getStack(l).getItem() == Items.ENDER_PEARL) {
                    epSlot = l;
                    break;
                }
        return epSlot;
    }

    private float calcTrajectory(@NotNull BlockPos bp) {
        double a = Math.hypot(bp.getX() + 0.5f - mc.player.getX(), bp.getZ() + 0.5f - mc.player.getZ());
        double y = 6.125 * ((bp.getY() + 1f) - (mc.player.getY() + (double) mc.player.getEyeHeight(mc.player.getPose())));
        y = 0.05000000074505806 * ((0.05000000074505806 * (a * a)) + y);
        y = Math.sqrt(9.37890625 - y);
        double d = 3.0625 - y;
        y = Math.atan2(d * d + y, 0.05000000074505806 * a);
        d = Math.atan2(d, 0.05000000074505806 * a);
        return (float) Math.min(y, d);
    }

    private BlockPos calcTrajectory(Entity e) {
        return traceTrajectory(e.getX(), e.getY(), e.getZ(), e.getVelocity().x, e.getVelocity().y, e.getVelocity().z);
    }

    private BlockPos checkTrajectory(float yaw, float pitch) {
        if (Float.isNaN(pitch))
            return null;
        float yawRad = yaw / 180.0f * 3.1415927f;
        float pitchRad = pitch / 180.0f * 3.1415927f;
        double x = mc.player.getX() - MathHelper.cos(yawRad) * 0.16f;
        double y = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;
        double z = mc.player.getZ() - MathHelper.sin(yawRad) * 0.16f;
        double motionX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * 0.4f;
        double motionY = -MathHelper.sin(pitchRad) * 0.4f;
        double motionZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * 0.4f;
        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;
        motionX *= 1.5f;
        motionY *= 1.5f;
        motionZ *= 1.5f;
        if (!mc.player.isOnGround()) motionY += mc.player.getVelocity().getY();
        return traceTrajectory(x, y, z, motionX, motionY, motionZ);
    }

    private BlockPos traceTrajectory(double x, double y, double z, double mx, double my, double mz) {
        Vec3d lastPos;
        for (int i = 0; i < 300; i++) {
            lastPos = new Vec3d(x, y, z);
            x += mx;
            y += my;
            z += mz;
            mx *= 0.99;
            my *= 0.99;
            mz *= 0.99;
            my -= 0.03f;
            Vec3d pos = new Vec3d(x, y, z);
            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK) return bhr.getBlockPos();

            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof ArrowEntity || ent == mc.player || ent instanceof EnderPearlEntity) continue;
                if (ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.2)))
                    return null;
            }

            if (y <= -65) break;
        }
        return null;
    }
}

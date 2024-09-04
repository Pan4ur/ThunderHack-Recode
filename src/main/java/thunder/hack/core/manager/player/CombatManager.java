package thunder.hack.core.manager.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.IManager;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventPostTick;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.AntiBot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CombatManager implements IManager {
    public HashMap<String, Integer> popList = new HashMap<>();

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (Module.fullNullCheck()) return;

        if (event.getPacket() instanceof EntityStatusS2CPacket pac) {
            if (pac.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity ent = pac.getEntity(mc.world);
                if (!(ent instanceof PlayerEntity)) return;
                if (popList == null) {
                    popList = new HashMap<>();
                }
                if (popList.get(ent.getName().getString()) == null) {
                    popList.put(ent.getName().getString(), 1);
                } else if (popList.get(ent.getName().getString()) != null) {
                    popList.put(ent.getName().getString(), popList.get(ent.getName().getString()) + 1);
                }
                ThunderHack.EVENT_BUS.post(new TotemPopEvent((PlayerEntity) ent, popList.get(ent.getName().getString())));
            }
        }
    }

    @EventHandler
    public void onPostTick(EventPostTick event) {
        if (Module.fullNullCheck())
            return;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (AntiBot.bots.contains(player)) continue;

            if (player.getHealth() <= 0 && popList.containsKey(player.getName().getString()))
                popList.remove(player.getName().getString(), popList.get(player.getName().getString()));
        }
    }

    public int getPops(@NotNull PlayerEntity entity) {
        if (popList.get(entity.getName().getString()) == null) return 0;
        return popList.get(entity.getName().getString());
    }

    public List<PlayerEntity> getTargets(float range) {
        return mc.world.getPlayers().stream()
                .filter(e -> !e.isDead())
                .filter(entityPlayer -> !Managers.FRIEND.isFriend(entityPlayer.getName().getString()))
                .filter(entityPlayer -> entityPlayer != mc.player)
                .filter(entityPlayer -> mc.player.squaredDistanceTo(entityPlayer) < range * range)
                .sorted(Comparator.comparing(e -> mc.player.squaredDistanceTo(e)))
                .collect(Collectors.toList());
    }

    public @Nullable PlayerEntity getTarget(float range, @NotNull TargetBy targetBy) {
        PlayerEntity target = null;

        switch (targetBy) {
            case FOV -> target = getTargetByFOV(range);
            case Health -> target = getTargetByHealth(range);
            case Distance -> target = getNearestTarget(range);
        }

        return target;
    }

    public @Nullable PlayerEntity getNearestTarget(float range) {
        return getTargets(range).stream().min(Comparator.comparing(t -> mc.player.distanceTo(t))).orElse(null);
    }

    public PlayerEntity getTargetByHealth(float range) {
        return getTargets(range).stream().min(Comparator.comparing(t -> (t.getHealth() + t.getAbsorptionAmount()))).orElse(null);
    }

    public PlayerEntity getTargetByFOV(float range) {
        return getTargets(range).stream().min(Comparator.comparing(this::getFOVAngle)).orElse(null);
    }

    public PlayerEntity getTargetByFOV(float range, float fov) {
        return getTargets(range).stream()
                .filter(entityPlayer -> getFOVAngle(entityPlayer) < fov)
                .min(Comparator.comparing(this::getFOVAngle)).orElse(null);
    }

    public @Nullable PlayerEntity getTarget(float range, @NotNull TargetBy targetBy, @NotNull Predicate<PlayerEntity> predicate) {
        PlayerEntity target = null;

        switch (targetBy) {
            case FOV -> target = getTargetByFOV(range, predicate);
            case Health -> target = getTargetByHealth(range, predicate);
            case Distance -> target = getNearestTarget(range, predicate);
        }

        return target;
    }

    public @Nullable PlayerEntity getNearestTarget(float range, Predicate<PlayerEntity> predicate) {
        return getTargets(range).stream()
                .filter(predicate)
                .min(Comparator.comparing(t -> mc.player.distanceTo(t)))
                .orElse(null);
    }

    public PlayerEntity getTargetByHealth(float range, Predicate<PlayerEntity> predicate) {
        return getTargets(range).stream()
                .filter(predicate)
                .min(Comparator.comparing(t -> (t.getHealth() + t.getAbsorptionAmount())))
                .orElse(null);
    }

    public PlayerEntity getTargetByFOV(float range, Predicate<PlayerEntity> predicate) {
        return getTargets(range).stream()
                .filter(predicate)
                .min(Comparator.comparing(this::getFOVAngle))
                .orElse(null);
    }

    private float getFOVAngle(@NotNull LivingEntity e) {
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(e.getZ() - mc.player.getZ(), e.getX() - mc.player.getX())) - 90.0);
        return Math.abs(yaw - MathHelper.wrapDegrees(mc.player.getYaw()));
    }

    public enum TargetBy {
        Distance,
        FOV,
        Health
    }
}

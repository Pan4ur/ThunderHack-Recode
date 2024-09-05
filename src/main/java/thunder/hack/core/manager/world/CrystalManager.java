package thunder.hack.core.manager.world;

import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.IManager;
import thunder.hack.core.manager.client.ModuleManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalManager implements IManager {
    private final Map<Integer, Long> deadCrystals = new ConcurrentHashMap<>();
    private final Map<Integer, Attempt> attackedCrystals = new ConcurrentHashMap<>();
    private final Map<BlockPos, Attempt> awaitingPositions = new ConcurrentHashMap<>();

    public void onAttack(EndCrystalEntity crystal) {
        setDead(crystal.getId(), System.currentTimeMillis());
        addAttack(crystal);
    }

    public void reset() {
        deadCrystals.clear();
        attackedCrystals.clear();
        awaitingPositions.clear();
    }

    public void update() {
        long time = System.currentTimeMillis();
        deadCrystals.entrySet().removeIf(entry -> time - entry.getValue() > Managers.SERVER.getPing() * 2L);
        attackedCrystals.entrySet().removeIf(entry -> entry.getValue().shouldRemove());
        awaitingPositions.entrySet().removeIf(entry -> entry.getValue().shouldRemove());
    }

    public boolean isDead(Integer id) {
        return deadCrystals.containsKey(id);
    }

    public void setDead(Integer id, long deathTime) {
        deadCrystals.putIfAbsent(id, deathTime);
    }

    public boolean isBlocked(Integer id) {
        return attackedCrystals.containsKey(id) && attackedCrystals.get(id).canSetPosBlocked();
    }

    public void addAttack(EndCrystalEntity entity) {
        attackedCrystals.compute(entity.getId(), (pos, attempt) -> {
            if (attempt == null) {
                return new Attempt(System.currentTimeMillis(), 1, entity.getPos());
            } else {
                if (ModuleManager.autoCrystal.breakFailsafe.getValue())
                    attempt.addAttempt();
                return attempt;
            }
        });
    }

    public Map<BlockPos, Attempt> getAwaitingPositions() {
        return awaitingPositions;
    }

    public void confirmSpawn(BlockPos bp) {
        awaitingPositions.remove(bp);
    }

    public void addAwaitingPos(BlockPos blockPos) {
        boolean blocked = ModuleManager.autoCrystal.isPositionBlockedByCrystal(blockPos.up());

        awaitingPositions.compute(blockPos, (pos, attempt) -> {
            if (attempt == null) {
                return new Attempt(System.currentTimeMillis(), 1, blockPos.toCenterPos());
            } else {
                if (!blocked && ModuleManager.autoCrystal.placeFailsafe.getValue())
                    attempt.addAttempt();
                return attempt;
            }
        });
    }

    public boolean isPositionBlocked(BlockPos bp) {
        return awaitingPositions.containsKey(bp) && awaitingPositions.get(bp).canSetPosBlocked();
    }

    public class Attempt {
        long time;
        int attempts;
        float distance;
        public Vec3d pos;

        Attempt(long time, int attempts, Vec3d pos) {
            this.time = time;
            this.pos = pos;
            this.attempts = attempts;
            distance = (float) mc.player.squaredDistanceTo(pos);
        }

        public Vec3d getPos() {
            return pos;
        }

        public long getTime() {
            return time;
        }

        public float getDistance() {
            return distance;
        }

        public boolean shouldRemove() {
            return Math.abs(distance - mc.player.squaredDistanceTo(pos)) >= 1f;
        }

        public void addAttempt() {
            attempts++;
        }

        public boolean canSetPosBlocked() {
            return attempts >= Math.max(ModuleManager.autoCrystal.attempts.getValue(), Managers.SERVER.getPing() / 25f);
        }
    }
}

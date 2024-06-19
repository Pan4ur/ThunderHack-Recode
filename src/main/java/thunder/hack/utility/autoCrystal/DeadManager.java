package thunder.hack.utility.autoCrystal;

import thunder.hack.core.impl.ServerManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeadManager {
    private final Map<Integer, Long> deadCrystals = new ConcurrentHashMap<>();

    public void reset() {
        deadCrystals.clear();
    }

    public void update() {
        Map<Integer, Long> cache = new ConcurrentHashMap<>(deadCrystals);

        if (!cache.isEmpty()) {
            cache.forEach((crystal, deathTime) -> {
                if (System.currentTimeMillis() - deathTime > ServerManager.getPing() * 3L) {
                    deadCrystals.remove(crystal);
                }
            });
        }
    }

    public boolean isDead(Integer id) {
        return deadCrystals.containsKey(id);
    }

    public void setDead(Integer id, long deathTime) {
        if (!deadCrystals.containsKey(id))
            deadCrystals.put(id, deathTime);
    }
}

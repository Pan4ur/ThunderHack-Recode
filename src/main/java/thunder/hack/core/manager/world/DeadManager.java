package thunder.hack.core.manager.world;

import thunder.hack.core.Managers;
import thunder.hack.core.manager.IManager;
import thunder.hack.core.manager.client.ServerManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeadManager implements IManager {
    private final Map<Integer, Long> deadCrystals = new ConcurrentHashMap<>();

    public void reset() {
        deadCrystals.clear();
    }

    public void update() {
        Map<Integer, Long> cache = new ConcurrentHashMap<>(deadCrystals);

        if (!cache.isEmpty()) {
            cache.forEach((crystal, deathTime) -> {
                if (System.currentTimeMillis() - deathTime > Managers.SERVER.getPing() * 2L) {
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

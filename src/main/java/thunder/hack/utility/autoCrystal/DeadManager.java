package thunder.hack.utility.autoCrystal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import thunder.hack.ThunderHack;
import thunder.hack.core.impl.ServerManager;
import thunder.hack.gui.hud.impl.PingHud;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeadManager {
    private final Map<EndCrystalEntity, Long> deadCrystals = new HashMap<>();


    public void reset() {
        deadCrystals.clear();
    }

    public void update(boolean dangerous, int delay) {
        if(dangerous)
            removeFromWorld(delay);

        if(!deadCrystals.isEmpty()) {
            Map<EndCrystalEntity, Long> cache = new HashMap<>(deadCrystals);
            cache.forEach((crystal, deathTime) -> {
                if (System.currentTimeMillis() - deathTime > ServerManager.getPing() * 3L) {
                    deadCrystals.remove(crystal);
                }
            });
        }
    }

    public boolean isDead(EndCrystalEntity crystal) {
        return deadCrystals.containsKey(crystal);
    }

    public void setDead(EndCrystalEntity crystal, long deathTime) {
        deadCrystals.put(crystal, deathTime);
    }

    public void removeFromWorld(int removeDelay) {
        Map<EndCrystalEntity, Long> cache = new HashMap<>(deadCrystals);
        cache.forEach((c, time) -> {
            if (System.currentTimeMillis() - time >= removeDelay) {
                c.kill();
                c.setRemoved(Entity.RemovalReason.KILLED);
                c.onRemoved();
                deadCrystals.remove(c);
            }
        });
    }
}

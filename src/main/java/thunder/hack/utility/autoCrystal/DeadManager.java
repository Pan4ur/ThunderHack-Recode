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

import static thunder.hack.core.IManager.mc;

public class DeadManager {
    private final Map<Integer, Long> deadCrystals = new ConcurrentHashMap<>();

    public void reset() {
        deadCrystals.clear();
    }

    public void update(boolean dangerous, int delay) {
        if(dangerous)
            removeFromWorld(delay);

        Map<Integer, Long> cache = new ConcurrentHashMap<>(deadCrystals);

        if(!cache.isEmpty()) {
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
        deadCrystals.put(id, deathTime);
    }

    public void removeFromWorld(int removeDelay) {
        Map<Integer, Long> cache = new ConcurrentHashMap<>(deadCrystals);
        cache.forEach((id, time) -> {
            if (System.currentTimeMillis() - time >= removeDelay) {
                Entity e = mc.world.getEntityById(id);
                if(e instanceof EndCrystalEntity c) {
                    c.kill();
                    c.setRemoved(Entity.RemovalReason.KILLED);
                    c.onRemoved();
                }
                deadCrystals.remove(id);
            }
        });
    }
}

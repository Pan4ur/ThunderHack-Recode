package dev.thunderhack.event.events;

import dev.thunderhack.event.Event;
import net.minecraft.entity.Entity;

public class EventEntitySpawn extends Event {
    private final Entity entity;
    public EventEntitySpawn(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}

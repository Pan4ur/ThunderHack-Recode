package dev.thunderhack.event.events;

import dev.thunderhack.event.Event;
import net.minecraft.entity.Entity;

public class EventEntityRemoved extends Event {
    public Entity entity;

    public EventEntityRemoved(Entity entity) {
        this.entity = entity;
    }
}

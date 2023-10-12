package dev.thunderhack.event.events;

import net.minecraft.entity.Entity;
import dev.thunderhack.event.Event;

public class EventAttack extends Event {
    private Entity entity;

    public EventAttack(Entity entity){
        this.entity = entity;
    }

    public Entity getEntity(){
        return  entity;
    }
}

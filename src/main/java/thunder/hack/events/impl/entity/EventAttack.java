package thunder.hack.events.impl.entity;

import net.minecraft.entity.Entity;
import thunder.hack.events.Event;

public class EventAttack extends Event {
    private Entity entity;

    public EventAttack(Entity entity){
        this.entity = entity;
    }

    public Entity getEntity(){
        return  entity;
    }
}

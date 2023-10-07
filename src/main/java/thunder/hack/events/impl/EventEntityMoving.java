package thunder.hack.events.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.Event;

public class EventEntityMoving extends Event {
    private final Entity entity;
    private final MovementType movementType;
    private final Vec3d movement;

    public EventEntityMoving(Entity entity, MovementType movementType, Vec3d movement) {
        this.entity = entity;
        this.movementType = movementType;
        this.movement = movement;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public Vec3d getMovement() {
        return movement;
    }

    public Entity getEntity() {
        return entity;
    }
}

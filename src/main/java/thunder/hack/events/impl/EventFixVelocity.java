package thunder.hack.events.impl;

import net.minecraft.util.math.Vec3d;
import thunder.hack.events.Event;

public class EventFixVelocity extends Event {
    Vec3d movementInput;
    float speed;
    float yaw;
    Vec3d velocity;

    public EventFixVelocity(Vec3d movementInput, float speed, float yaw, Vec3d velocity) {
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }

    public Vec3d getMovementInput() {
        return this.movementInput;
    }

    public float getSpeed() {
        return this.speed;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vec3d velocity) {
        this.velocity = velocity;
    }
}

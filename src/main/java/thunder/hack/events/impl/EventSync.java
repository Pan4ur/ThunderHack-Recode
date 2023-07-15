package thunder.hack.events.impl;


import thunder.hack.events.Event;

public class EventSync extends Event {

    public float getYaw() {
        return yaw;
    }

    float yaw;

    public float getPitch() {
        return pitch;
    }

    float pitch;
    public EventSync(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
package dev.thunderhack.event.events;

import dev.thunderhack.event.Event;

public class EventSync extends Event {
    public EventSync(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    float yaw;
    float pitch;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}